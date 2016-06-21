/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.Translation
import com.mogobiz.store.domain.Variation
import com.mogobiz.store.domain.VariationValue
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

class VariationController {
    def authenticationService
	def ajaxResponseService
	def categoryService

	private List<Variation> getVariations(Category category) {
		List<Variation> variations = Variation.findAllByCategory(category,[sort:'position',order:'asc'])
		Category parent = category.parent
		while (parent != null) {
			variations.addAll(Variation.findAllByCategory(parent,[sort:'position',order:'asc']))
			parent = parent.parent
		}
		return variations
	}


	@Transactional(readOnly = true)
	def show() {
		Category category = params['category']?.id?Category.get(params['category']?.id):null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		boolean isAdmin = authenticationService.isAdministrator()
		if (seller?(seller.company!=category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		if(category) {
			List<Variation> variations = getVariations(category).take(3)
			variations.eachWithIndex { obj, i -> obj.position = i+1 }
			List<Map> listVariations = []
			if(!variations){
				variations = []
			}
			variations.each { 
				listVariations.add(it.asMapForJSON())
			}
			withFormat {
				html variations:variations
				xml {  render variations as XML }
				json { render listVariations as JSON }
			}
		}
		else {
			response.sendError 404
		}
	}


	@Transactional
	def updatePosition() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def tagids = params.tagids.split(",")
		def company = seller.company
		Long categoryId = params["category"]?.id?.toLong()
		if (categoryId) {
			Category cat = Category.get(categoryId)
			if(cat && cat.company==company){
				List<Variation> variations = Variation.findAllByCategory(cat,[sort:'position',order:'asc'])
				variations.each {
					it.position = tagids.findIndexOf { tagid ->
						tagid.toInteger() == it.id
					}
					it.save(flush:true)
				}
				withFormat {
					json { render variations as JSON }
				}
			}
			else{
				response.sendError 404
			}
		}
		else{
				response.sendError 404
		}
	}

	@Transactional
	def save() {
		Category category = params['category']?.id?Category.get(params['category']?.id):null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller?(seller.company!= category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		String name = params['variation']?.name
		if (category){
			def variationsCount = Variation.findAllByCategory(category).size()
			
			if(variationsCount < 3){
				Variation variation = name?Variation.findByCategoryAndName(category, name):null
				if(!variation){
					variation = new Variation(params['variation'])
					variation.position = 1000000
					variation.uuid = UUID.randomUUID().toString()
					variation.category = category
					if(variation.validate()){
						variation.save(flush:true)
						List<Variation> variations = Variation.findAllByCategory(category,[sort:'position',order:'asc'])
						int pos = 0
						variations.each {
							it.position = ++pos
							it.save(flush:true)
						}
					}
					else {
						log.error(variation.errors)
					}
					withFormat {
						html variation:variation
						xml {
							if(!variation.hasErrors()) {
								render variation as XML
							}
							else{
								render variation.errors as XML
							}
						}
						json { render variation as JSON }
					}
				}
				else {
					response.sendError 403
				}
			}
			else{
				response.sendError 401
			}
		}
		else {
			response.sendError 404
		}
	}

	@Transactional
	def update() {
		Category category = params['category']?.id ? Category.get(params['category']?.id) : null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller?(seller.company!=category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		Variation variation = params['variation']?.id?Variation.get (params['variation']?.id):null

		if(variation && categoryService.isChildOf(category, variation.category)) {
			String name = params['variation']?.name
			variation.properties = params['variation']
			if(variation.validate()) {
				variation.save()
			}
			else {
				log.error(variation.errors)
			}
			withFormat {
				html variation:variation
				xml {
					if(!variation.hasErrors()){
						render variation as XML
					}
					else{
						render variation.errors as XML
					}
				}
				json { render ajaxResponseService.prepareResponse(variation, variation.asMapForJSON()).asMap() as JSON }
			}
		}
		else{
			response.sendError 404
		}
	}

	@Transactional
	def delete() {
		Category category = params['category']?.id ? Category.get(params['category']?.id) : null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		seller.company;
		category.company
		if (seller?(seller.company!=category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		def variation = params['variation']?.id?Variation.get (params['variation']?.id):null
		if(variation){
            try {
                Translation.findAllByTarget(variation.id).each { it.delete() }
                variation.delete(flush: true)
                withFormat {
                    xml {  render [:] as XML }
                    json { render [:] as JSON }
                }
            }
            catch (Exception){
                response.sendError 403
            }
		}
		else{
			response.sendError 404
		}
	}

	@Transactional
	def addVariationValue() {
		Category category = params['category']?.id ? Category.get(params['category']?.id) : null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		boolean isAdmin = authenticationService.isAdministrator()
		if (seller?(seller.company!=category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		def variation = params['variation']?.id?Variation.get (params['variation']?.id):null
		def value = params['variationValue']?.value
		def position = params['variationValue']?.position
		if(variation && value && position){
			VariationValue variationValue = variation.variationValues.find { val->
				val.value == value
			}
			if (!variationValue){
				variationValue = new VariationValue(value:value, position:position, variation:variation)
				if(variationValue.validate()) {
					variationValue.save()
				}
				variation.addToVariationValues(variationValue)
				withFormat {
					html variation:variation
					xml {
						if(!variation.hasErrors()){
							render variation as XML
						}
						else{
							render variation.errors as XML
						}
					}
					json { render ajaxResponseService.prepareResponse(variation, variation.asMapForJSON()).asMap() as JSON }
				}
			}
		}
		else{
			response.sendError 404
		}
	}

	@Transactional
	def removeVariationValue() {
		Category category = params['category']?.id ? Category.get(params['category']?.id) : null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller?(seller.company!=category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		Variation variation = params['variation']?.id ? Variation.get (params['variation']?.id) : null
		String value = params['variationValue']?.value
		if(variation && value){
			VariationValue variationValue = variation.variationValues.find { val->
				val.value == value
			}
			if (variationValue){
				variation.removeFromVariationValues(variationValue)
                Translation.findAllByTarget(variationValue.id).each { it.delete() }
				variationValue.delete(flush:true)
				variation.save(flush:true)
				withFormat {
					html variation:variation
					xml {
						if(!variation.hasErrors()){
							render variation as XML
						}
						else{
							render variation.errors as XML
						}
					}
					json { render ajaxResponseService.prepareResponse(variation, variation.asMapForJSON()).asMap() as JSON }
				}
			}
		}
		else {
			response.sendError 404
		}
	}

	@Transactional
	def updateVariationValue() {
		Category category = params['category']?.id ? Category.get(params['category']?.id) : null
		Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		boolean isAdmin = authenticationService.isAdministrator()
		if (seller?(seller.company!=category.company):!authenticationService.isAdministrator()) {
			response.sendError 401
			return
		}
		Variation variation = params['variation']?.id?Variation.get (params['variation']?.id):null
		String value = params['variationValue']?.value
		Long valueId = Long.parseLong(params['variationValue']?.id)
		if(variation && value){
			VariationValue variationValue = variation.variationValues.find { val->
				val.id == valueId
			}
			if (variationValue){
                variationValue.variation = variation
				variationValue.value = value
				variationValue.save()
				withFormat {
					html variation:variation
					xml {
						if(!variation.hasErrors()){
							render variation as XML
						}
						else{
							render variation.errors as XML
						}
					}
					json { render ajaxResponseService.prepareResponse(variation, variation.asMapForJSON()).asMap() as JSON }
				}
			}
		}
		else {
			response.sendError 404
		}
	}
}
