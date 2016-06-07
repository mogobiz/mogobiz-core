/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 * 
 */
package com.mogobiz.store.partner

import com.mogobiz.constant.IperConstant
import com.mogobiz.json.RenderUtil
import com.mogobiz.store.domain.IntraDayPeriod
import com.mogobiz.store.domain.Product
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class IntraDayPeriodController {
	
	def ajaxResponseService
	
	def authenticationService

	@Transactional(readOnly = true)
	def show() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def product = params['product']?.id?Product.get(params['product']?.id):null
		if(product && product.company==company){
			def periods = []
			IntraDayPeriod.findAllByProduct(product)?.each { period ->
				periods.add (period.asMapForJSON())
			}
			withFormat {
				html periods:periods
				xml {  render periods as XML }
				json { render periods as JSON }
			}
		}
		else{
			response.sendError 404
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
		def product = params['product']?.id?Product.get(params['product']?.id):null
		if(product && product.company==company && params['period']){
			def period = new IntraDayPeriod()

			period.startDate = RenderUtil.translateDateTimeToCalendar(params['period']?.startDate, IperConstant.DATE_FORMAT)
			period.endDate = RenderUtil.translateDateTimeToCalendar(params['period']?.endDate, IperConstant.DATE_FORMAT)
			period.weekday1 = Boolean.parseBoolean(params['period']?.weekday1);
			period.weekday2 = Boolean.parseBoolean(params['period']?.weekday2);
			period.weekday3 = Boolean.parseBoolean(params['period']?.weekday3);
			period.weekday4 = Boolean.parseBoolean(params['period']?.weekday4);
			period.weekday5 = Boolean.parseBoolean(params['period']?.weekday5);
			period.weekday6 = Boolean.parseBoolean(params['period']?.weekday6);
			period.weekday7 = Boolean.parseBoolean(params['period']?.weekday7);
			period.product = product

			if(period.validate()){
				period.save(flush:true)
			}
			withFormat {
				html period:period
				xml {
					if(!period.hasErrors()){
						render period as XML
					}
					else{
						render period.errors as XML
					}
				}
				json { render ajaxResponseService.prepareResponse(period, period.asMapForJSON()).asMap() as JSON }
			}
		}
		else{
			response.sendError 404
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
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def period = params['period']?.id?IntraDayPeriod.get(params['period']?.id):null
		if(period && product && period.product==product && product.company==company){
			// period.properties = params['period']

			period.startDate = RenderUtil.translateDateTimeToCalendar(params['period']?.startDate, IperConstant.DATE_FORMAT)
			period.endDate = RenderUtil.translateDateTimeToCalendar(params['period']?.endDate, IperConstant.DATE_FORMAT)
			period.weekday1 = Boolean.parseBoolean(params['period']?.weekday1);
			period.weekday2 = Boolean.parseBoolean(params['period']?.weekday2);
			period.weekday3 = Boolean.parseBoolean(params['period']?.weekday3);
			period.weekday4 = Boolean.parseBoolean(params['period']?.weekday4);
			period.weekday5 = Boolean.parseBoolean(params['period']?.weekday5);
			period.weekday6 = Boolean.parseBoolean(params['period']?.weekday6);
			period.weekday7 = Boolean.parseBoolean(params['period']?.weekday7);

			if(period.validate()){
				period.save(flush:true)
			}
			withFormat {
				html period:period
				xml {
					if(!period.hasErrors()){
						render period as XML
					}
					else{
						render period.errors as XML
					}
				}
				json { render ajaxResponseService.prepareResponse(period, period.asMapForJSON()).asMap() as JSON }
			}
		}
		else{
			response.sendError 404
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
		def product = params['product']?.id?Product.get(params['product']?.id):null
		def period = params['period']?.id?IntraDayPeriod.get(params['period']?.id):null
		if(period && product && period.product==product && product.company==company){
			period.delete()
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
