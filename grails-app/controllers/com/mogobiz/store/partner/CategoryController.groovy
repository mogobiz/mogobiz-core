package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.service.SanitizeUrlService
import com.mogobiz.store.domain.*
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

/**
 * Controller utilisé pour gérer les categories 
 */
class CategoryController {
    AuthenticationService authenticationService
    SanitizeUrlService sanitizeUrlService

    @Transactional(readOnly = true)
    def show() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        Long catalogId = params['catalog']?.id?.toLong()
        def allCategories = params['allCategories']
        Long id = params['category']?.id?.toLong()
        Long par = params['category']?.parentId?.toLong()
        if (id != null) {
            def category = Category.get(id)
            if (category && category.company == seller.company) {
                withFormat {
                    html category: category
                    xml { render category as XML }
                    json { render category as JSON }
                }
            } else {
                response.sendError 404
            }
        } else if (par != null) {
            List<Category> categories = Category.withCriteria {
                eq('deleted', false)
                company {
                    eq('id', seller.company.id)
                }
                parent { eq('id', par) }
                order("position", "asc")
            }
            withFormat {
                html categories: categories
                xml { render categories as XML }
                json { render categories as JSON }
            }
        } else if (catalogId) {
            List<Category> categories = Category.withCriteria {
                eq('deleted', false)
                company {
                    eq('id', seller.company.id)
                }
                catalog { eq('id', catalogId) }
                if (allCategories.equals("false"))
                    isNull('parent')
                order("position", "asc")
            }
            withFormat {
                html categories: categories
                xml { render categories as XML }
                json { render categories as JSON }
            }
        } else {
            List<Category> categories = Category.withCriteria {
                eq('deleted', false)
                company {
                    eq('id', seller.company.id)
                }
                if (params['name']) {
                    ilike('name', '%' + params['name'] + '%')
                }
            }
            render categories as JSON
        }
    }

    @Transactional
    def save() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def name = params['category']?.name
        def parentId = params['category']?.parentId
        def catalogId = params['category']?.catalogId
        if (name) {
            def category = Category.withCriteria {
                eq('name', name)
                company {
                    eq('id', seller.company.id)
                }
                if (parentId) {
                    parent {
                        eq('id', parentId.toLong())
                    }
                } else {
                    isNull('parent')
                }
            }
            if (!category) {
                category = new Category(params['category'])
                category.company = seller.company
                category.uuid = UUID.randomUUID().toString()
                category.parent = parentId ? Category.get(parentId.toLong()) : null
                category.catalog = catalogId ? Catalog.get(catalogId.toLong()) : category.parent?.catalog
                category.sanitizedName = sanitizeUrlService.sanitizeWithDashes(category.name)
                // Ibeacon
                if (params["category"]?.ibeaconId) {
                    if (params["category"]?.ibeaconId == -1) {
                        category.ibeacon = null
                    } else {
                        category.ibeacon = Ibeacon.get(params["category"]?.ibeaconId)
                    }
                }

                if (category.validate()) {
                    category.save(flush: true)
                }

                List<Category> categories = Category.withCriteria {
                    parent { eq("id", parentId.toLong()) }
                    order("position", "asc")
                }
                int pos = 0
                categories.each {
                    pos += 10
                    it.position = pos
                    it.save(flush: true)
                }

                withFormat {
                    xml { render category as XML }
                    json { render category as JSON }
                }
            } else {
                response.sendError 403
            }
        }
    }

    @Transactional
    def update() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        def category = params['category']?.id ? Category.get(params['category']?.id) : null
        def par = params['category']?.parentId
        if (category && category.company == company) {
            long oldParentId = category.parent ? category.parent.id : -1
            long newParentId = par ? par.toLong() : -1
            int position = category.position
            category.sanitizedName = null
            category.properties = params['category']
            if (category.sanitizedName == null)
                category.sanitizedName = sanitizeUrlService.sanitizeWithDashes(category.name)
            category.parent = par ? Category.get(par.toLong()) : null
            if (category.parent) {
                category.catalog = category.parent.catalog
            }
            // Ibeacon
            if (params["category"]?.ibeaconId) {
                if (params["category"]?.ibeaconId == -1) {
                    category.ibeacon = null
                } else {
                    category.ibeacon = Ibeacon.get(params["category"]?.ibeaconId)
                }
            }
            if (category.validate()) {
                category.save(flush: true)
            }
            List<Category> categories = Category.withCriteria {
                parent { eq("id", oldParentId) }
                order("position", "asc")
            }
            int pos = 0
            categories.each {
                pos += 10
                it.position = pos
                it.save(flush: true)
            }
            if (oldParentId != newParentId) {
                categories = Category.withCriteria {
                    parent { eq("id", newParentId) }
                    order("position", "asc")
                }
                pos = 0
                categories.each {
                    pos += 10
                    it.position = pos
                    it.save(flush: true)
                }
            }
        }
        withFormat {
            xml { render category as XML }
            json { render category as JSON }
        }
    }

    @Transactional
    def delete() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        Company company = seller.company
        Category category = params['category']?.id ? Category.get(params['category']?.id) : null
        if (category && category.company == company) {
            def products = Product.executeQuery('FROM Product p JOIN p.category c WHERE c=:category', [category: category])
            def variations = Variation.executeQuery('FROM Variation v JOIN v.category c WHERE c=:category', [category: category])
            def features = Feature.executeQuery('FROM Feature f JOIN f.category c WHERE c=:category', [category: category])
            def categories = Category.executeQuery('FROM Category c WHERE c.parent=:category', [category: category])
            if (products.isEmpty() && variations.isEmpty() && features.isEmpty() && categories.isEmpty()) {
                category.delete()
                withFormat {
                    xml { render [:] as XML }
                    json { render [:] as JSON }
                }
            } else {
                response.sendError 403
            }
        } else {
            response.sendError 403
        }
    }

    @Transactional
    def markDeleted() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        Long id = params['category']?.id?.toLong()
        if (id != null) {
            Category category = Category.get(id)
            if (category && category.company == seller.company) {
                category.setDeleted(true)
                category.save(flush: true)
                withFormat {
                    html category: category
                    xml { render category as XML }
                    json { render category as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            response.sendError 403
        }
    }
}
