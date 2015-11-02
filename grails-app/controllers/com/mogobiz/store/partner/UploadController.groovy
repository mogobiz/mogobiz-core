/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 *
 */
package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.facebook.FBClient
import com.mogobiz.google.PicasaClient
import com.mogobiz.google.YouTubeClient
import com.mogobiz.json.RenderUtil
import com.mogobiz.service.ResService
import com.mogobiz.service.SanitizeUrlService
import com.mogobiz.store.domain.*
import grails.converters.JSON
import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartHttpServletRequest

import static com.mogobiz.constant.IperConstant.QUEUE_NS
import static com.mogobiz.constant.IperConstant.QUEUE_SOCIAL


/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class UploadController {

    AjaxResponseService ajaxResponseService
    def grailsApplication
    SanitizeUrlService sanitizeUrlService
    ResService resService
    AuthenticationService authenticationService

    @Transactional(readOnly = true)
    def retrievePictures() {
        def pictures = []
        Album album = params['album']?.id ? Album.get(params['album']?.id) : null
        List<Resource> dftPictures
        if (album) {
            dftPictures = Resource.findAllByAlbum(album)
        } else {
            dftPictures = Resource.findAll('FROM Resource r WHERE r.album is null AND r.xtype=:xtype', [xtype: ResourceType.PICTURE])
        }
        if (dftPictures) {
            dftPictures.each { dftPicture ->
                def picture = [:]
                picture['id'] = dftPicture.id
                picture['name'] = dftPicture.name
                picture['description'] = dftPicture.description
                RenderUtil.extractResourceUrl(dftPicture)
                picture['url'] = RenderUtil.extractResourceUrl(dftPicture)
                picture['smallPicture'] = RenderUtil.extractResourceSmallPicture(dftPicture)
                pictures.add(picture)
            }
        }
        withFormat {
            json { render pictures as JSON }
        }
    }

    @Transactional(readOnly = true)
    def retrieveAlbums() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        def company = seller?.company
        def albums = []
        def resourceAccountType = params['resourceAccountType'] ? ResourceAccountType.valueOf(params['resourceAccountType']) : null
        switch (resourceAccountType) {
            case ResourceAccountType.FACEBOOK:
                // on recherche le compte externe
                def externalAccount = ExternalAccount.findByUserAndAccountType(seller, AccountType.FACEBOOK)
                if (externalAccount) {
                    FBClient client = new FBClient(externalAccount.token)
                    def fbAlbums = client.fetchAlbums(0, 0)
                    if (fbAlbums) {
                        fbAlbums.each { fbAlbum ->
                            def album = [:]
                            album['id'] = fbAlbum.id
                            album['name'] = fbAlbum.name
                            albums.add(album)
                        }
                    }
                }
                break
            case ResourceAccountType.PICASA:
                // on recherche le compte externe
                ExternalAccount externalAccount = ExternalAccount.findByUserAndAccountType(seller, AccountType.GOOGLE)
                if (externalAccount) {
                    PicasaClient client = new PicasaClient(externalAccount.token, externalAccount.tokenSecret)
                    def pAlbums = client.fetchAlbums()
                    if (pAlbums) {
                        pAlbums.each { pAlbum ->
                            def album = [:]
                            album['id'] = pAlbum.id
                            album['name'] = pAlbum.title.plainText
                            albums.add(album)
                        }
                    }
                }
                break
            case ResourceAccountType.FLICKR:
                // TODO
                break
            case ResourceAccountType.YOUTUBE:
                // on recherche le compte externe
                def externalAccount = ExternalAccount.findByUserAndAccountType(seller, AccountType.GOOGLE)
                if (externalAccount) {
                    YouTubeClient client = new YouTubeClient(externalAccount.token, externalAccount.tokenSecret)
                    // TODO - how to retrieve album using YouTube ?
                }
                break
            case ResourceAccountType.DAILYMOTION:
                // TODO
                break
            case ResourceAccountType.VIMEO:
                // TODO
                break
            default:
                def dftAlbums = []
                if (company) {
                    dftAlbums = Album.findByCompany(company)
                } else {
                    dftAlbums = Album.executeQuery('from Album a where a.company is null')
                }
                if (dftAlbums) {
                    dftAlbums.each { dftAlbum ->
                        def album = [:]
                        album['id'] = dftAlbum.id
                        album['name'] = dftAlbum.name
                        albums.add(album)
                    }
                } else {
                    def album = [:]
                    album['id'] = -1
                    album['name'] = 'Mogobiz'
                    albums.add(album)
                }
        }
        withFormat {
            json { render albums as JSON }
        }
    }

    @Transactional(readOnly = true)
    def retrieveExternalAccounts() {
        def accountTypes = [AccountType.STANDARD]
        User user = authenticationService.retrieveAuthenticatedUser()
        List<ExternalAccount> externalAccounts = ExternalAccount.findAllByUser(user)
        if (externalAccounts) {
            externalAccounts.each { externalAccount ->
                externalAccount
                accountTypes.add(externalAccount.accountType)
            }
        }
        withFormat {
            json { render accountTypes as JSON }
        }
    }

    @Transactional
    def uploadResources () {
        def files = request.getFileMap()
        def resources = []
        if (files) {
            files.values().each { file ->
                if (!file.empty) {
                    def resource = processUploadResource(request, params, file)
                    if (resource) {
                        resources.add(resource.asMapForJSON())
                    }
                }
            }
        }
        withFormat {
            json { render resources as JSON }
        }
    }

    def uploadResource = {
        def file = (request as MultipartHttpServletRequest).getFile('file')
        if (file && !file.empty) {
            def resource = processUploadResource(request, params, file)
            if (params["product.id"]) {
                String productId = params["product.id"]
                String resourceId = resource.id
                chain(controller: 'productResource', action: 'bindResourcesToProduct', params: ["product.id": productId, "resource.id": resourceId]);
            } else {
                def resourceVO = resource ? resource.asMapForJSON() : []
                withFormat {
                    json { render ajaxResponseService.prepareResponse(resource, resourceVO).asMap() as JSON }
                }

            }
        }
    }


    private List<Variation> getVariations(Category category) {
        List<Variation> variations = Variation.findAllByCategory(category,[sort:'position',order:'asc'])
        Category parent = category.parent
        while (parent != null) {
            variations.addAll(Variation.findAllByCategory(parent,[sort:'position',order:'asc']))
            parent = parent.parent
        }
        return variations
    }


    private processUploadResource(request, params, file) {
        log.debug("Processing Upload")
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        def user = seller

        File tmpFile = File.createTempFile("resource", ".tmp")

        file.transferTo(tmpFile)
        String contentType = file.contentType
        if (!user) {
            user = authenticationService.retrieveAuthenticatedUser()
        }
        def company = seller?.company
        Resource resource = params['resource']?.id ? Resource.get(params['resource']?.id) : null
        if (resource == null) {
            resource = new Resource(params['resource'] as Map)
        } else {
            resource.properties = params['resource']
        }
        def name = file.originalFilename
        if (!resource.name) {
            resource.name = name
        }
        // TODO should be sent by the client
        resource.active = true
        resource.deleted = false

        List<String> variations = []
        variations << (params['variation1.id'] ? VariationValue.get(params['variation1.id'] as Long)?.value : "x")
        variations << (params['variation2.id'] ? VariationValue.get(params['variation2.id'] as Long)?.value : "x")
        variations << (params['variation3.id'] ? VariationValue.get(params['variation3.id'] as Long)?.value : "x")

        final variationsAsString = variations.join("")
        if(!variationsAsString.equals("xxx")){
            log.info(variationsAsString)
            def productId = params.product?.id as Long
            def category = productId ? Product.get(productId)?.category : null
            def nbVariations = category ? getVariations(category)?.size() : 0
            if(nbVariations > 0){
                def sb = new StringBuilder("__")
                (0..nbVariations-1).each {
                    sb.append(variations.get(it) as String).append("_")
                }
                sb.append("_")
                resource.name = sb.toString()
                log.info("variation -> ${resource.name}")
            }
        }

        resService.uploadResource(seller.company, resource, tmpFile, contentType)

        def map = [:]
        map.put('userId', user.id)
        map.put('resourceId', resource.id)
        map.put('action', "addResource")
        map.put("productId", params["product.id"])
        log.debug("Upload Controller Sending message" + map)
        event(namespace: QUEUE_NS, topic: QUEUE_SOCIAL, data: map)
        log.debug("Upload Controller message sent " + map)
        return resource
    }
}
