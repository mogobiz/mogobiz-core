/**
 *
 */
package com.mogobiz.store.partner


import grails.converters.JSON

import org.apache.shiro.SecurityUtils

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductState;
import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.Suggestion

/**
 * @author hayssam.saleh@ebiznext.com
 * @author stephane.manciot@ebiznext.com
 *
 */
class SuggestionsController {

	def authenticationService

	def retrieveProductSuggestions = {
		def suggestions = []
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def authorized = canAccessProduct(product)
		if(authorized){
			def results = Suggestion.executeQuery("select distinct sg from Suggestion sg where sg.pack=:product order by sg.position asc", [product:product])
			results?.each { s ->
				suggestions.add(s.asMapForJSON())
			}
		}
		else{
			response.sendError 401
			return
		}
		withFormat {
			json{ render suggestions as JSON }
		}
	}

	def bindSuggestionsToProduct = {
		def suggestions = []
		def admin = authenticationService.canAdminAllStores()
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def authorized = admin || canAccessProduct(product)
		if(authorized){
			def results = Suggestion.executeQuery("select distinct sg from Suggestion sg where sg.pack=:product", [product:product])
			results?.each { s ->
				s.delete(flush:true)
			}
			def pos = 0
			// suggestion.required, suggestion.discount, suggestion.product.id
			def _idProducts = []
			if(params['suggestion']?.product?.id){
				_idProducts.addAll(params['suggestion']?.product?.id)
			}
			
			_idProducts?.each { _idProduct ->
				def suggestion = new Suggestion()
				suggestion.required = (_idProducts.size() == 1) ? Boolean.parseBoolean(params['suggestion']?.required) : Boolean.parseBoolean(params['suggestion']?.required[pos])
				suggestion.discount = (_idProducts.size() == 1) ? params['suggestion']?.discount : params['suggestion']?.discount[pos]
				suggestion.product = _idProduct ? Product.get(new Long(_idProduct)):null
				suggestion.pack = product
				if(admin || canAccessProduct(suggestion.product) ){
					suggestion.position = pos++
					if(suggestion.validate()){
						suggestion.save(flush:true)
						suggestions.add(suggestion.asMapForJSON())
					}
				}
			}
			product.save()
		}
		else{
			response.sendError 401
			return
		}
		withFormat {
			json{ render suggestions as JSON }
		}
	}

	def listProductsForSuggestions = {
		def products = []
		Company company = params['company']?.id?Company.get(params['company']?.id):null
		if(!authenticationService.canAdminAllStores()){
			def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
			if(seller == null || company == null || seller.company.id != company.id){
				response.sendError 401
				return
			}
			company = seller.company
		}
		Product.createCriteria().list {
			eq('company', company)
			eq('deleted', false)
			//eq('state', ProductState.ACTIVE )
			order("name", "asc")
		}.each { product ->
			products.add(product.asMapForJSON())
			}
		withFormat {
			json{ render products as JSON }
		}
	}

	def listCompaniesForSuggestions = {
		def companies = []
		if(!authenticationService.canAdminAllStores()){
			def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
			if(seller == null){
				response.sendError 401
				return
			}
			companies.add(seller.company?.asMapForJSON())
		}
		else{
			Company.createCriteria().list {}.each { comp ->
				companies.add(comp.asMapForJSON())
			}
		}
		withFormat {
			json{ render companies as JSON }
		}
	}

	private boolean canAccessProduct(Product product) {
		return authenticationService.canAccessStore(product?.company)
	}

}
