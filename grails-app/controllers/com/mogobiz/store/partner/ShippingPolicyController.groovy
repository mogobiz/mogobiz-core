/**
 * 
 */
package com.mogobiz.store.partner

import com.mogobiz.store.domain.Shipping;
import com.mogobiz.store.domain.Product;

import grails.converters.JSON
import grails.converters.XML

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class ShippingPolicyController {

	def ajaxResponseService

	def authenticationService

	def show = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def shippings = Shipping.findAllByCompany(seller.company)
		if(!shippings){
			shippings = []
		}
		withFormat {
			html shippings:shippings
			xml {  render shippings as XML }
			json { render shippings as JSON }
		}
	}

	def save = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def company = seller?.company
		def shipping = new Shipping(params['shipping'])
		// amount as float
		def amount = params['shipping']?.amount
		if(amount){
			shipping.amount = Float.parseFloat(amount)
		}
		shipping.free = (shipping.amount == 0)
		shipping.company = company
		if(shipping.validate()){
			shipping.save()
		}
		withFormat {
			html shipping:shipping
			xml {  render shipping as XML }
			json { render shipping as JSON }
		}
	}

	def update = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def company = seller?.company
		def shipping = params['shipping']?.id?Shipping.get(params['shipping']?.id):null
		if(shipping && shipping.company == company){
			shipping.properties = params['shipping']
			// amount as float
			def amount = params['shipping']?.amount
			if(amount){
				shipping.amount = Float.parseFloat(amount)
			}
			shipping.free = (shipping.amount == 0)
			if(shipping.validate()){
				shipping.save()
			}
		}
		withFormat {
			html shipping:shipping
			xml {  render shipping as XML }
			json { render shipping as JSON }
		}
	}

	def delete = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def company = seller?.company
		def shipping = params['shipping']?.id?Shipping.get(params['shipping']?.id):null
		if(shipping && shipping.company == company){
			def products = Product.executeQuery('FROM Product p JOIN p.shipping s WHERE s=:shipping', [shipping:shipping])
			if(products.isEmpty()){
				shipping.delete()
			}
		}
		withFormat {
			xml {  render [:] as XML }
			json { render [:] as JSON }
		}
	}

	def bindProductToShippingPolicy = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def company = seller.company
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def shipping = params['shipping']?.id?Shipping.get(params['shipping']?.id):null
		if(product && shipping && product.company==company && shipping.company==company){
			product.shipping = shipping
			product.save()
		}
		withFormat {
			html product:product
			xml {  render product as XML }
			json { render product as JSON }
		}
	}

	def unbindProductToShippingPolicy = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def company = seller.company
		def product = params['product']?.id?Product.get(params['product']?.id):null
		if(product && product.company==company){
			product.shipping = null
			product.save()
		}
		withFormat {
			html product:product
			xml {  render product as XML }
			json { render product as JSON }
		}
	}

}
