/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.admin

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.store.domain.Company
import com.mogobiz.json.RenderUtil
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

/**
 * 
 * @author stephane.manciot@ebiznext.com
 *
 */
class CompanyShippingPolicyController {

	AjaxResponseService ajaxResponseService

	@Transactional(readOnly = true)
	def show() {
		def company = params['company']?.id?Company.get(params['company']?.id):null
		if(company) {
			def map = toShippingVO(company)
			withFormat {
				html payment:map
				xml {  render map as XML }
				json { render ajaxResponseService.prepareResponse(company, map).asMap() as JSON }
			}
		}
		else {
			response.sendError 404
		}
	}

	@Transactional
	def update() {
		def company = params['company']?.id?Company.get(params['company']?.id):null
		if(company) {
            company.properties = params['company']
			if(company.validate()){
                // weight unit
                //String weightUnit = params['company']?.weightUnit?params['company']?.weightUnit.name:null
                //company.weightUnit = weightUnit && weightUnit.trim().length() > 0 ?WeightUnit.valueOf(weightUnit):null
                // refund policy
                //String refundPolicy = params['company']?.refundPolicy?params['company']?.refundPolicy.name:null
                //company.refundPolicy = refundPolicy && refundPolicy.trim().length() > 0?RefundPolicy.valueOf(refundPolicy):null
                // TODO shipping carriers
                // from shipping address (shipFrom)
                if(company.shipFrom){
                    def shipFrom = company.shipFrom
                    if(shipFrom.validate()){
                        shipFrom.save()
                        company.save()
                    }
                    else{
                        company.errors = shipFrom.errors
                    }
                }else{
                    company.save()
                }
			}

			def map = toShippingVO(company);

			withFormat {
				html payment:map
				xml {  render map as XML }
				json { render ajaxResponseService.prepareResponse(company, map).asMap() as JSON }
			}
		}
		else {
			response.sendError 404
		}
	}

	private Map toShippingVO(Company company){
		def map = RenderUtil.asMapForJSON([
			// begin shipping policy
			// international shipping
			'shippingInternational',
			// shipping address from
			'shipFrom',
			'shipFrom.id',
			'shipFrom.city',
			'shipFrom.road1',
			'shipFrom.road2',
			'shipFrom.postalCode',
            'shipFrom.state',
			'shipFrom.countryCode',
			// shipping carriers
			'shippingCarriers',
			'shippingCarriers.ups',
			'shippingCarriers.fedex',
			// handling time
			'handlingTime',
			// weight unit
			'weightUnit',
			// refund policy
			'refundPolicy',
			// return policy
			'returnPolicy'
			// end shipping policy
		], company)
		return map
	}
}
