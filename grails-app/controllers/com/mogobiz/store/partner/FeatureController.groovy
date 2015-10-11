/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.service.FeatureService
import com.mogobiz.store.domain.FeatureValue
import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.Product
import grails.transaction.Transactional

/**
 * Controller utilisé pour gérer les produits
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class FeatureController {
	AuthenticationService authenticationService
	FeatureService featureService

	@Transactional(readOnly = true)
	def show() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		Company company = seller.company
		Long productId = params["product"]?.id?.toLong()
		Long categoryId = params["category"]?.id?.toLong()
		if (categoryId) {
			List<Feature> features = featureService.getCategoryFeatures(categoryId, true)
			def result =[features:features]
			withFormat {
				html properties:result
				xml { render result as XML }
				json { render result as JSON }
			}
		}
		else if (productId) {
			List<Feature> features = featureService.getProductFeatures(productId, true)
			def result =[features:features]
			withFormat {
				html properties:result
				xml { render result as XML }
				json { render result as JSON }
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
		Long productId = params["product"]?.id?.toLong()
		if (categoryId) {
			Category cat = Category.get(categoryId)
			if(cat && cat.company==company){
				List<Feature> features = Feature.withCriteria {
					category { eq('id', categoryId) }
				}
				features.each {
					it.position = tagids.findIndexOf { tagid ->
						tagid.toInteger() == it.id
					}
					it.save(flush:true)
				}
				withFormat {
					json { render features as JSON }
				}
			}
			else{
				response.sendError 404
			}
		}
		else if (productId) {
			Product prod = Product.get(productId)
			if(prod && prod.company==company){
				List<Feature> features = Feature.withCriteria {
					product { eq('id', productId) }
				}
				features.each {
					it.position = tagids.findIndexOf { tagid ->
						tagid.toInteger() == it.id
					}
					it.save(flush:true)
				}
				withFormat {
					json { render features as JSON }
				}
			}
			else{
				response.sendError 404
			}
		}
	}

	@Transactional
	def save() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def categoryId = params["category"]?.id?.toLong()
		def productId = params["product"]?.id?.toLong()
		String featureName = params["feature.name"]
		if (categoryId) {
			Category cat = Category.get(categoryId)
			if(cat && cat.company==company){
				if (!featureService.existFeatureInCategory(featureName, -1, categoryId, true)) {
					def max = Feature.createCriteria().get {
						category { eq('id', categoryId) }
						projections { max "position" }
					}
					Feature f = new Feature(params["feature"])
					f.position = max? (max+1) : 0
					f.uuid = UUID.randomUUID().toString()
					f.category = cat
					f.save(flush:true)
					withFormat {
						json { render f as JSON }
					}
				}
				else{
					response.sendError 404
				}
			}
		}
		else if (productId) {
			Product prod = Product.get(productId)
			if(prod && prod.company==company){
				String featureUuid = params["feature.uuid"]
				if (!featureService.existFeatureInProduct(featureName, -1, productId, true)) {
					Category owner;
					if (featureUuid) {
						Feature feature = Feature.findByUuidAndCategoryIsNotNullAndProductIsNull(featureUuid)
						owner = feature ? feature.category : null
					}
					def max = Feature.createCriteria().get {
						product { eq('id', productId) }
						projections { max "position" }
					}
					int position = max? (max+1) : 0
					Feature f = new Feature(params["feature"])
					f.position = max? (max+1) : 0
					f.uuid = featureUuid ?: UUID.randomUUID().toString()
					f.product = prod
					f.category = owner
					f.save(flush:true)
					withFormat {
						json { render f as JSON }
					}
				}
				else {
					response.sendError 404
				}
			}
		}
	}

	@Transactional
	def update() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def categoryId = params["category"]?.id?.toLong()
		def productId = params["product"]?.id?.toLong()
		def featureId = params["feature"]?.id?.toLong()
		def featureName = params["feature.name"]
		if (categoryId && !featureService.existFeatureInCategory(featureName, featureId, categoryId, true)) {
			Category category = Category.get(categoryId)
			if(category && category.company==company){
				Feature feature = Feature.get(featureId)
				if (feature) {
					feature.properties = params["feature"]
					feature.save(flush:true)
					withFormat {
						json { render feature as JSON }
					}
				}
				else {
					response.sendError 404
				}
			}
		}
		else if (productId && !featureService.existFeatureInProduct(featureName,featureId, productId, true)) {
			Product product = Product.get(productId)
			if(product && product.company==company){
				Feature feature = Feature.get(featureId)
				if (feature) {
					if (feature.category) {
						FeatureValue featureValue = FeatureValue.findByFeatureAndProduct(feature, product)
						if (featureValue == null) {
							featureValue = new FeatureValue()
							featureValue.product = product
							featureValue.feature = feature
						}
						featureValue.value = params["feature.value"]
						featureValue.save(flush: true)

						feature.value = feature.value + "||||" + featureValue.value
						feature.discard()
					}
					else {
						feature.properties = params["feature"]
						feature.save(flush: true)
					}
					withFormat {
						json { render feature as JSON }
					}
				}
				else {
					response.sendError 404
				}
			}
		}
	}

	@Transactional
	def delete() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		Long categoryId = params["category"]?.id?.toLong()
		Long productId = params["product"]?.id?.toLong()
		Long featureId = params["feature"]?.id?.toLong()
		if (categoryId) {
			Category category = Category.get(categoryId)
			if(category && category.company==company){
				Feature feature = Feature.get(featureId)
				if (feature) {
					Feature.executeUpdate("delete Feature f where f.uuid = :uuid", [uuid:feature.uuid])
					withFormat {
						json { render category as JSON }
					}
				}
				else {
					response.sendError 404
				}
			}
		}
		else if (productId) {
			Product product = Product.get(productId)
			if(product && product.company==company){
				Feature feature = Feature.get(featureId)
				if (feature) {
					if (feature.category) {
						List<FeatureValue> list = FeatureValue.findAllByFeatureAndProduct(feature, product)
						FeatureValue.deleteAll(list)
					}
					else {
						feature.delete(flush: true)
					}
					withFormat {
						json { render product as JSON }
					}
				}
				else {
					response.sendError 404
				}
			}
		}
	}
}
