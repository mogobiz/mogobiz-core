package com.mogobiz.store.partner

import com.mogobiz.store.domain.EsEnv
import com.mogobiz.elasticsearch.client.ESClient
import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Seller

class CatalogController {

    def authenticationService

    static client = ESClient.instance

    def show = {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        Long id = params['catalog']?.id?.toLong()
        if(id != null){
            def catalog = Catalog.get(id)
            if(catalog && catalog.company == seller.company){
                withFormat {
                    html catalog:catalog
                    xml { render catalog as XML }
                    json { render catalog as JSON }
                }
            }
            else{
                response.sendError 404
            }
        }
        else {
            List<Catalog> catalogs = Catalog.findAllByCompanyAndDeleted(seller.company, false)
            withFormat {
                html catalogs:catalogs
                xml { render catalogs as XML }
                json { render catalogs as JSON }
            }
        }
    }

    def save = {
        def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(!seller){
            response.sendError 401
            return
        }
        def name = params['catalog']?.name
        if(name){
            def catalog = Catalog.withCriteria {
                eq('name', name)
                company {
                    eq('id', seller.company.id)
                }
            }
            if(!catalog){
                catalog = new Catalog(params['catalog'])
                catalog.company = seller.company
                catalog.uuid = UUID.randomUUID().toString()
                if(catalog.validate()){
                    catalog.save(flush:true)
                }
                else {
                    System.out.println(catalog.errors)
                }
                withFormat {
                    xml { render catalog as XML }
                    json { render catalog as JSON }
                }
            }
            else{
                response.sendError 404
            }
        }
    }

    def update = {
        def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(!seller){
            response.sendError 401
            return
        }
        def company = seller.company
        def catalog = params['catalog']?.id ? Catalog.get(params['catalog']?.id):null
        def parent = params['catalog']?.parent
        if(catalog && catalog.company == company){
            catalog.properties = params['catalog']
            if(catalog.validate()){
                catalog.save()
            }
        }
        withFormat {
            xml { render catalog as XML }
            json { render catalog as JSON }
        }
    }

    def delete = {
        def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(!seller){
            response.sendError 401
            return
        }
        def company = seller.company
        Catalog catalog = params['catalog']?.id ? Catalog.get(params['catalog']?.id):null
        if(catalog && catalog.company == company){
            List<Category> categories = Category.executeQuery('FROM Category c JOIN c.catalog d WHERE d=:catalog', [catalog:catalog])
            if(categories.isEmpty()) {
                catalog.delete()
            }
            else{
                response.sendError 401
                return
            }
        }
        withFormat {
            xml { render [:] as XML }
            json { render [:] as JSON }
        }
    }
    def markDeleted = {
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