/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 *
 */
package com.mogobiz.store.partner

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Suggestion
import grails.converters.JSON
import grails.transaction.Transactional

class SuggestionsController {

    def authenticationService

    @Transactional(readOnly = true)
    def retrieveProductSuggestions() {
        def suggestions = []
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        def authorized = canAccessProduct(product)
        if (authorized) {
            def results = Suggestion.executeQuery("select distinct sg from Suggestion sg where sg.pack=:product order by sg.position asc", [product: product])
            results?.each { s ->
                suggestions.add(s.asMapForJSON())
            }
        } else {
            response.sendError 401
            return
        }
        withFormat {
            json { render suggestions as JSON }
        }
    }

    @Transactional
    def bindSuggestionsToProduct() {
        def suggestions = []
        def admin = authenticationService.canAdminAllStores()
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        def authorized = admin || canAccessProduct(product)
        if (authorized) {
            def results = Suggestion.executeQuery("select distinct sg from Suggestion sg where sg.pack=:product", [product: product])
            results?.each { s ->
                s.delete(flush: true)
            }
            def pos = 0
            // suggestion.required, suggestion.discount, suggestion.product.id
            def _idProducts = []
            if (params['suggestion']?.product?.id) {
                _idProducts.addAll(params['suggestion']?.product?.id)
            }

            _idProducts?.each { _idProduct ->
                def suggestion = new Suggestion()
                suggestion.required = (_idProducts.size() == 1) ? Boolean.parseBoolean(params['suggestion']?.required) : Boolean.parseBoolean(params['suggestion']?.required[pos])
                suggestion.discount = (_idProducts.size() == 1) ? params['suggestion']?.discount : params['suggestion']?.discount[pos]
                suggestion.product = _idProduct ? Product.get(new Long(_idProduct)) : null
                suggestion.pack = product
                if (admin || canAccessProduct(suggestion.product)) {
                    suggestion.position = pos++
                    if (suggestion.validate()) {
                        suggestion.save(flush: true)
                        suggestions.add(suggestion.asMapForJSON())
                    }
                }
            }
            product.save()
        } else {
            response.sendError 401
            return
        }
        withFormat {
            json { render suggestions as JSON }
        }
    }

    @Transactional(readOnly = true)
    def listProductsForSuggestions() {
        def products = []
        Company company = params['company']?.id ? Company.get(params['company'].id as long) : null
        long cid = params.long("catalog.id")
        String fullSearch = "%" + params['fullSearch'] + "%"
        if (!authenticationService.canAdminAllStores()) {
            def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
            if (seller == null || company == null || seller.company.id != company.id) {
                response.sendError 401
                return
            }
            company = seller.company
        }
        List<Product> dbProducts = Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and p.deleted = false and p.company = :company and d.id = :cid and lower(p.name) like lower(:fullSearch) order by p.name asc", [company: company, cid: cid, fullSearch: fullSearch], [max: 100])
        dbProducts.each {
            println(it)
        }

        dbProducts.each { product ->
            products.add(product.asMapForJSON())
        }
        withFormat {
            json { render products as JSON }
        }
    }

    @Transactional(readOnly = true)
    def listCompaniesForSuggestions() {
        def companies = []
        if (!authenticationService.canAdminAllStores()) {
            def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
            if (seller == null) {
                response.sendError 401
                return
            }
            companies.add(seller.company?.asMapForJSON())
        } else {
            Company.createCriteria().list {}.each { comp ->
                companies.add(comp.asMapForJSON())
            }
        }
        withFormat {
            json { render companies as JSON }
        }
    }

    private boolean canAccessProduct(Product product) {
        return authenticationService.canAccessStore(product?.company)
    }

}
