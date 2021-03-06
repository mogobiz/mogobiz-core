/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 *
 */
package com.mogobiz.store.partner

import com.mogobiz.store.domain.*
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import org.apache.commons.io.FileUtils

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class BrandController {

    def ajaxResponseService

    def authenticationService

    static final int BUFFER_SIZE = 2048

    @Transactional(readOnly = true)
    def show() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        Long id = params['brand']?.id?.toLong()
        if (id != null) {
            def brand = Brand.get(id)
            if (brand && brand.company == seller.company) {
                withFormat {
                    html brand: brand
                    xml { render brand as XML }
                    json { render brand as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            List<Brand> brands = Brand.findAllByCompany(seller.company)
            if (!brands) {
                brands = []
            }
            def mc = [compare: { a, b -> a.name.compareTo(b.name) }] as Comparator
            Collections.sort(brands, mc)
            withFormat {
                html brands: brands
                xml { render brands as XML }
                json { render brands as JSON }
            }
        }
    }

    @Transactional
    def save() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def brand = new Brand(params['brand'])
        brand.company = company
        def parentId = params['brand']?.parentId
        brand.parent = parentId ? Brand.get(parentId.toLong()) : null
        // Ibeacon
        if (params["brand"]?.ibeaconId) {
            if (params["brand"]?.ibeaconId == -1) {
                brand.ibeacon = null
            } else {
                brand.ibeacon = Ibeacon.get(params["brand"]?.ibeaconId)
            }
        }
        if (brand.validate()) {
            brand.save()
        }
        withFormat {
            html brand: brand
            xml { render brand as XML }
            json { render brand as JSON }
        }
    }

    @Transactional
    def update() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def brand = params['brand']?.id ? Brand.get(params['brand']?.id) : null
        if (brand && brand.company == company) {
            brand.properties = params['brand']
            def parentId = params['brand']?.parentId
            brand.parent = parentId ? Brand.get(parentId.toLong()) : null
            // Ibeacon
            if (params["brand"]?.ibeaconId) {
                if (params["brand"]?.ibeaconId == -1) {
                    brand.ibeacon = null
                } else {
                    brand.ibeacon = Ibeacon.get(params["brand"]?.ibeaconId)
                }
            }
            if (brand.validate()) {
                brand.save()
            }
        }
        withFormat {
            html brand: brand
            xml { render brand as XML }
            json { render brand as JSON }
        }
    }

    @Transactional
    def delete() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def brand = params['brand']?.id ? Brand.get(params['brand']?.id) : null
        if (brand && brand.company == company) {
            def products = Product.executeQuery('FROM Product p JOIN p.brand b WHERE b=:brand', [brand: brand])
            if (products.isEmpty()) {
                Translation.findAllByTarget(brand.id).each { it.delete() }
                brand.delete()
            }
        }
        withFormat {
            xml { render [:] as XML }
            json { render [:] as JSON }
        }
    }

    @Transactional
    def bindProductToBrand() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        def brand = params['brand']?.id ? Brand.get(params['brand']?.id) : null
        if (product && brand && product.company == company && brand.company == company) {
            product.brand = brand
            product.save()
        }
        withFormat {
            html product: product
            xml { render product as XML }
            json { render product as JSON }
        }
    }

    @Transactional
    def unbindProductToBrand() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (product && product.company == company) {
            product.brand = null
            product.save()
        }
        withFormat {
            html product: product
            xml { render product as XML }
            json { render product as JSON }
        }
    }

    @Transactional
    def removeLogo() {
        long brandId = params.long("brand.id")
        Seller user = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!user) {
            user = authenticationService.retrieveAuthenticatedUser() as Seller
        }
        String logoName = brandId.toString()
        final String resourcesPath = grailsApplication.config.resources.path
        final String companyCode = user.company.code
        String dir = "$resourcesPath/brands/logos/$companyCode/"
        String resourcesDir = "$resourcesPath/resources/$companyCode/"
        [dir, resourcesDir].each {
            new File(it).listFiles(new FilenameFilter() {
                @Override
                boolean accept(File f, String name) {
                    return name.startsWith(logoName/* + "."*/)
                }
            }).each { it.delete() }
        }
        render "true"
    }

    @Transactional
    def uploadLogo() {
        Seller user = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!user) {
            user = authenticationService.retrieveAuthenticatedUser() as Seller
        }
        def file = request.getFile('file')
        if (file && !file.empty) {
            long brandId = params.long("brand.id")
            String logoName = processUploadLogo(user, brandId, file)
            render logoName
        }
    }

    private String processUploadLogo(Seller user, long brandId, file) {
        log.debug("Processing Upload Logo")
        def name = file.originalFilename
        def extension = ''
        def index = name.lastIndexOf('.')
        if (index > 0) {
            extension = name.substring(index)
        }
        final resourcesPath = grailsApplication.config.resources.path
        final companyCode = user.company.code
        String dir = "$resourcesPath/brands/logos/$companyCode/"
        String logoName = brandId.toString()
        File d = new File(dir)
        d.mkdirs()
        String url = dir + logoName + extension
        File logoFile = new File(url)
        logoFile.delete()
        log.debug("Processing Upload Logo  url = " + url)
        file.transferTo(logoFile)
        String resourcesDir = "$resourcesPath/resources/$companyCode"
        FileUtils.copyFile(logoFile, new File("${resourcesDir}/$logoName"))
        return logoName
    }


    @Transactional(readOnly = true)
    def displayLogo() {
        Seller user = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        long brandId = params.long("brand.id")
        if (!user) {
            user = authenticationService.retrieveAuthenticatedUser()
        }
        String logoName = brandId.toString()
        File dir = new File(grailsApplication.config.resources.path + '/brands/logos/' + user.company.code)
        File[] files = dir.listFiles(
                new FilenameFilter() {
                    @Override
                    boolean accept(File d, String name) {
                        return name.startsWith(logoName + '.')
                    }
                }
        )
        if (files && files.length > 0) {
            File file = files.first()
            response.contentType = "image/" + file.getName().substring(file.getName().lastIndexOf('.') + 1)
            def out = response.outputStream
            def bytes = new byte[BUFFER_SIZE]
            file.withInputStream { inp ->
                while (inp.read(bytes) != -1) {
                    out.write(bytes)
                    out.flush()
                }
            }
        }
    }

    @Transactional(readOnly = true)
    def hasLogo() {
        Seller user = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        long brandId = params.long("brand.id")
        if (!user) {
            user = authenticationService.retrieveAuthenticatedUser()
        }
        String logoName = brandId.toString()
        File dir = new File(grailsApplication.config.resources.path + '/brands/logos/' + user.company.code)
        File[] files = dir.listFiles(
                new FilenameFilter() {
                    @Override
                    boolean accept(File d, String name) {
                        return name.startsWith(logoName + '.')
                    }
                }
        )
        if (files && files.length > 0) {
            render true
        } else {
            render false
        }
    }

    @Transactional
    def saveProperty(Long brand_id, String name, String value) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        Brand brand = Brand.get(brand_id)
        if (brand && brand.company == seller.company) {
            BrandProperty property = BrandProperty.findByBrandAndName(brand, name)
            if (!property) {
                property = new BrandProperty(brand: brand, name: name, value: value)
            } else {
                property.value = value
            }
            property.validate()
            if (!property.hasErrors()) {
                property = property.save(flush: true)
            }
            render ajaxResponseService.prepareResponse(property, property?.asMapForJSON()).asMap() as JSON
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def updateProperty(Long brand_id, String name, String value) {
        Brand brand = Brand.get(brand_id)
        BrandProperty property = BrandProperty.findByBrandAndName(brand, name)
        if (property) {
            property.name = name
            property.value = value
            property.save(flush: true)
        }
        withFormat {
            json { property ? true : false }
        }
    }

    @Transactional
    def deleteProperty(Long id) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }

        BrandProperty property = BrandProperty.get(id)
        if (property && property.brand.company == seller.company) {
            property.delete(flush: true)
            render([success: true] as Map) as JSON
            return
        } else {
            response.sendError 404
        }
    }
}
