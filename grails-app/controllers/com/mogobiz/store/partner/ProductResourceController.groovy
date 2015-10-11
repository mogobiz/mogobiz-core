/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 *
 */
package com.mogobiz.store.partner

import com.mogobiz.tools.ImageTools
import com.mogobiz.utils.IperUtil
import grails.converters.JSON

import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Product2Resource
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import grails.transaction.Transactional

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class ProductResourceController {
	
	def authenticationService
	
	def grailsUrlMappingsHolder

	@Transactional(readOnly = true)
	def retrieveProductResources() {
		def resources = []
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def company = seller.company
		if(product && product.company==company){
			def xtype = params['resource']?.xtype
			def params = [:]
			params['product'] = product
			if(xtype){
				params['xtype'] = ResourceType.valueOf (xtype)
			}
			def results = Product2Resource.executeQuery("\
			select distinct pr from Product2Resource pr join pr.resource as r\
			where pr.product=:product "
					+(xtype?"and r.xtype=:xtype ":"")
					+ " order by pr.position asc\
			", params)
			results?.each { r ->
				resources.add(r.asMapForJSON())
			}
		}
		withFormat {
			json{ render resources as JSON }
		}
	}

	@Transactional
	def bindResourcesToProduct() {
		def resources = []
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def company = seller.company
		if(product && product.company==company){
			def idResources = []
			def pos = product.product2Resources.size()
			idResources.addAll (params['resource']?.id)
			idResources?.each { idResource ->
				def resource = Resource.get(idResource)
				if(resource){
					def product2Resource = Product2Resource.findByProductAndResource(product, resource)
					if(!product2Resource){
						product2Resource = new Product2Resource(
								montant : 0,
								product : product,
								resource : resource,
								position : pos++
								)
						if(product2Resource.validate()){
							product2Resource.save()
						}
					}
					resources.add (product2Resource)
				}
			}
		}
		withFormat {
			json{ render resources as JSON }
		}
	}

	@Transactional
	def updateResourceToProduct() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def product2Resource = params['product2resource']?.id?Product2Resource.get(params['product2resource']?.id):null
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def company = seller.company
		if(product && product.company==company && product2Resource){
			product2Resource.properties = params['product2resource']
			// montant as float
			def montant = params['product2resource'].montant
			if(montant){
				product2Resource.montant = Float.parseFloat(montant)
			}
			if(product2Resource.validate()){
				product2Resource.save()
			}
		}
		else{
			product2Resource = []
		}
		withFormat {
			json{ render product2Resource as JSON }
		}
	}

	@Transactional
	def unbindResourceToProduct() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def company = seller.company
		if(product && product.company==company){
			def id = params['resource']?.id
			def resource = id?Resource.get(id):null
			if(resource){
				def product2Resource = Product2Resource.findByProductAndResource(product, resource)
				if(product2Resource){
					product.removeFromProduct2Resources(product2Resource)
					product2Resource.delete()
					if (params['delete']) {
                        String resourcesPath = grailsApplication.config.resources.path
						ImageTools.deleteAll(new File(resourcesPath + (IperUtil.normalizeSeparator(resource.url) - resourcesPath)))
						resource.delete();
					}
				}
			}
			else {
				//gestion d'une liste de resources � unbinder
				def resources = params['product']?.resourcesList
				if(resources) {
					def resourcesList = JSON.parse(resources)
					for(int i=0; i<resourcesList?.size(); i++) {
						id = resourcesList[i]?.id
						resource = id?Resource.get(id):null
						def product2Resource = Product2Resource.findByProductAndResource(product, resource)
						if(product2Resource){
							product.removeFromProduct2Resources(product2Resource)
							product2Resource.delete()
						}
					}
				}
			}
			product.save()
			withFormat {
				json{ render product as JSON }
			}
		}
	}
}
