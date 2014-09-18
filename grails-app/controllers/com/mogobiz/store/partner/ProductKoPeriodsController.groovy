package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponseService
import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.DatePeriod
import com.mogobiz.store.domain.Product
import com.mogobiz.constant.IperConstant;
import com.mogobiz.json.RenderUtil;

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class ProductKoPeriodsController {

	AjaxResponseService ajaxResponseService

	def authenticationService

	def index = {
		redirect(action: "show", params: params)
	}

	def show = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def koPeriod = params['koPeriod']?.id?DatePeriod.get(params['koPeriod']?.id):null
		def product = params['product']?.id?Product.get(params['product']?.id):koPeriod?.product
		if(product && product.company==company){
			if(koPeriod){
				withFormat {
					html koPeriod:koPeriod
					xml {  render koPeriod as XML }
					json { render  ajaxResponseService.prepareResponse(koPeriod, koPeriod.asMapForJSON()).asMap() as JSON }
				}
			}
			else{
				def koPeriods = []
				DatePeriod.findAllByProduct(product)?.each { period ->
					koPeriods.add (period.asMapForJSON())
				}
				withFormat {
					html koPeriods:koPeriods
					xml {  render koPeriods as XML }
					json { render koPeriods as JSON }
				}
			}
		}
		else{
			response.sendError 404
		}
	}

	def save = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def product = params['product']?.id?Product.get(params['product']?.id):null
		if(product && product.company==company && params['koPeriod']){
			def koPeriod = new DatePeriod()
			koPeriod.startDate = RenderUtil.translateDateTimeToCalendar(params['koPeriod']?.startDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR)
			koPeriod.endDate = RenderUtil.translateDateTimeToCalendar(params['koPeriod']?.endDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR)
			koPeriod.product = product

			if(koPeriod.validate()){
				koPeriod.save(flush:true)
			}

			withFormat {
				html koPeriod:koPeriod
				xml {
					if(!koPeriod.hasErrors()){
						render koPeriod as XML
					}
					else{
						render koPeriod.errors as XML
					}
				}
				json { render ajaxResponseService.prepareResponse(koPeriod, koPeriod.asMapForJSON()).asMap() as JSON }
			}
		}
		else{
			response.sendError 404
		}
	}
	
	def update = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def koPeriod = params['koPeriod']?.id?DatePeriod.get(params['koPeriod']?.id):null
		def product = params['product']?.id?Product.get(params['product']?.id):koPeriod?.product
		if(koPeriod && product && koPeriod.product==product && product.company==company){
			koPeriod.startDate = RenderUtil.translateDateTimeToCalendar(params['koPeriod']?.startDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR)
			koPeriod.endDate = RenderUtil.translateDateTimeToCalendar(params['koPeriod']?.endDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR)

			if(koPeriod.validate()){
				koPeriod.save(flush:true)
			}

			withFormat {
				html koPeriod:koPeriod
				xml {
					if(!koPeriod.hasErrors()){
						render koPeriod as XML
					}
					else{
						render koPeriod.errors as XML
					}
				}
				json { render ajaxResponseService.prepareResponse(koPeriod, koPeriod.asMapForJSON()).asMap() as JSON }
			}
		}
		else{
			response.sendError 404
		}
	}

	def delete = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def koPeriod = params['koPeriod']?.id?DatePeriod.get(params['koPeriod']?.id):null
		def product = params['product']?.id?Product.get(params['product']?.id):koPeriod?.product
		if(koPeriod && product && koPeriod.product==product && product.company==company){
			koPeriod.delete()
			withFormat {
				html redirect(action:'show', params:['product.id':product.id, format:'html'])
				xml {  redirect(action:'show', params:['product.id':product.id, format:'xml']) }
				json { redirect(action:'show', params:['product.id':product.id, format:'json']) }
			}
		}
		else{
			response.sendError 404
		}
	}
}
