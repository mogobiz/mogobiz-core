/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.authentication.ProfileService
import com.mogobiz.store.domain.Company
import com.mogobiz.utils.PermissionType
import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Seller
import grails.transaction.Transactional

class CatalogController {

    AuthenticationService authenticationService

    ProfileService profileService

    @Transactional(readOnly = true)
    def show() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        Long id = params['catalog']?.id?.toLong()
        if (id != null) {
            def catalog = Catalog.get(id)
            if (catalog && catalog.company == seller.company) {
                withFormat {
                    html catalog: catalog
                    xml { render catalog as XML }
                    json { render catalog as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            List<Catalog> catalogs = Catalog.findAllByCompanyAndDeleted(seller.company, false)
            withFormat {
                html catalogs: catalogs
                xml { render catalogs as XML }
                json { render catalogs as JSON }
            }
        }
    }

    @Transactional
    def save() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        String name = params['catalog']?.name
        Company company = seller.company
        if (name) {
            def catalog = Catalog.findByNameAndDeletedAndCompany(name, false, company)
            if (!catalog) {
                catalog = new Catalog(params['catalog'] as Map)
                catalog.company = company
                catalog.uuid = UUID.randomUUID().toString()
                catalog.validate()
                if (!catalog.hasErrors()) {
                    catalog.save(flush: true)
                    profileService.saveUserPermission(
                            seller,
                            true,
                            PermissionType.UPDATE_STORE_CATALOG,
                            company.id as String,
                            catalog.id as String
                    )
                } else {
                    catalog.errors.allErrors.each { log.error(it) }
                }
                withFormat {
                    xml { render catalog as XML }
                    json { render catalog as JSON }
                }
            } else {
                response.sendError 404
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
        def catalog = params['catalog']?.id ? Catalog.get(params['catalog']?.id) : null
        def parent = params['catalog']?.parent
        if (catalog && catalog.company == company) {
            catalog.properties = params['catalog']
            catalog.activationDate = new Date(params.int("catalog.activationDate_year") - 1900, params.int("catalog.activationDate_month") - 1, params.int("catalog.activationDate_day"), 12, 0, 0)
        }
        if (catalog.validate()) {
            catalog.save()
        }
        withFormat {
            xml { render catalog as XML }
            json { render catalog as JSON }
        }
    }

    @Transactional
    def delete() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        Catalog catalog = params['catalog']?.id ? Catalog.get(params['catalog']?.id) : null
        if (catalog && catalog.company == company) {
            List<Category> categories = Category.executeQuery('FROM Category c JOIN c.catalog d WHERE d=:catalog', [catalog: catalog])
            if (categories.isEmpty()) {
                catalog.delete()
            } else {
                response.sendError 401
                return
            }
        }
        withFormat {
            xml { render [:] as XML }
            json { render [:] as JSON }
        }
    }

    @Transactional
    def markDeleted() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        Long id = params['catalog']?.id?.toLong()
        if (id != null) {
            Catalog catalog = Catalog.get(id)
            if (catalog && catalog.company == seller.company) {
                catalog.setDeleted(true)
                catalog.save(flush: true)
                withFormat {
                    html category: catalog
                    xml { render catalog as XML }
                    json { render catalog as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            response.sendError 403
        }
    }
}
