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
import com.mogobiz.service.SanitizeUrlService
import com.mogobiz.store.domain.*
import com.mogobiz.utils.ImageSize
import com.mogobiz.utils.ImageUtil
import grails.converters.JSON

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

    AuthenticationService authenticationService

    def retrievePictures = {
        def pictures = []
        def album = params['album']?.id ? Album.get(params['album']?.id) : null
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

    def retrieveAlbums = {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        def company = seller?.company
        def albums = []
        def resourceAccountType = params['resourceAccountType'] ? ResourceAccountType.valueOf(params['resourceAccountType']) : null
        switch (resourceAccountType) {
            case ResourceAccountType.FACEBOOK:
                // on recherche le compte externe
                def externalAccount = retrieveExternalAccount(seller?.id, AccountType.FACEBOOK)
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
                    album['name'] = 'IPER2010'
                    albums.add(album)
                }
        }
        withFormat {
            json { render albums as JSON }
        }
    }

    def retrieveExternalAccounts = {
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

    def uploadResources = {
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
        def file = request.getFile('file')
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

    private processUploadResource(request, params, file) {
        log.debug("Processing Upload")
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        def user = seller
        if (!user) {
            user = authenticationService.retrieveAuthenticatedUser()
        }
        def company = seller?.company
        def resource = params['resource']?.id ? Resource.get(params['resource']?.id) : null
        if (resource == null) {
            resource = new Resource(params['resource'])
        } else {
            resource.properties = params['resource']
        }

        // TODO should be sent by the client
        resource.active = true
        resource.deleted = false

        def dir = grailsApplication.config.rootPath + '/resources/'
        if (company != null) {
            resource.company = company
            dir += company.code + '/'
        } else {
            dir += user.login + '/'
        }
        def name = file.originalFilename
        def extension = ''
        def index = name.lastIndexOf('.')
        if (index > 0) {
            extension = name.substring(index)
        }
        if (!resource.name) {
            resource.name = name
        }
        log.debug("Processing Upload resource name = " + resource.name)
        resource.sanitizedName = sanitizeUrlService.sanitizeWithDashes(resource.name)
        String contentType = file.contentType
        resource.contentType = contentType
        if (contentType != null) {
            if (contentType.toLowerCase().contains('audio')) {
                resource.xtype = ResourceType.AUDIO
                dir += 'audio/'
            } else if (contentType.toLowerCase().contains('video')) {
                resource.xtype = ResourceType.VIDEO
                dir += 'video/'
            } else if (contentType.toLowerCase().contains('image')) {
                resource.xtype = ResourceType.PICTURE
                dir += 'image/'
            } else if (contentType.toLowerCase().contains('text')) {
                resource.xtype = ResourceType.TEXT
                dir += 'text/'
            }
        }
        resource.url = dir + name
        def albumName = 'PASSNGUIDE'
        def albumDescription = 'PASSNGUIDE'
        Album album
        log.debug("Processing Upload resource rul = " + resource.url)
        if (company != null) {
            album = Album.findByNameAndCompany(albumName, company)
            if (!album) {
                album = new Album(
                        name: albumName,
                        description: albumDescription,
                        company: company
                )
                if (album.validate()) {
                    album.save()
                }
            }
        }
        resource.album = album
        if (resource.validate()) {
            resource.save()
            log.debug("Processing Upload resource validated = " + resource)
        }
        def d = new File(dir)
        d.mkdirs()
        resource.url = dir + resource.id + extension
        file.transferTo(new File(resource.url))
        if (resource.xtype == ResourceType.PICTURE) {
            resource.smallPicture = ImageUtil.getFile(new File(resource.url), ImageSize.SMALL, true)
        }
        resource.uploaded = true
        if (resource.validate()) {
            resource.save(flush: true)
            log.debug("Processing Upload resource validated 2= " + resource)
        } else {
            log.debug("Processing Upload resource not validated 2= " + resource)
            resource.errors.each { log.debug it }
            return null
        }
        def map = [:]
        map.put('userId', user.id)
        map.put('resourceId', resource.id)
        map.put('action', "addResource")
        map.put("productId", params["product.id"])
        log.debug("Upload Controller Sending message" + map)
        event(namespace:QUEUE_NS, topic:QUEUE_SOCIAL, data:map)
        log.debug("Upload Controller message sent " + map)
        return resource
    }
}
