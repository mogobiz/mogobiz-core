package com.mogobiz.store.admin

import com.mogobiz.ajax.AjaxResponseService
import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.Company
import com.mogobiz.json.RenderUtil

/**
 * 
 * @author stephane.manciot@ebiznext.com
 *
 */
class CompanyPaymentPolicyController
{
	AjaxResponseService ajaxResponseService

	def show = {
		def company = params['company']?.id?Company.get(params['company']?.id):null
		if(company)
		{
			def map = toPaymentVO(company);
			withFormat {
				html payment:map
				xml {  render map as XML }
				json { render ajaxResponseService.prepareResponse(company, map).asMap() as JSON }
			}
		}
		else
		{
			response.sendError 404
		}
	}

	def update = {
		def company = params['company']?.id?Company.get(params['company']?.id):null
		if(company)
		{
			company.properties = params['company']
			if(company.validate()){
				company.save()
			}

			def map = toPaymentVO(company);

			withFormat {
				html payment:map
				xml {  render map as XML }
				json { render ajaxResponseService.prepareResponse(company, map).asMap() as JSON }
			}
		}
		else
		{
			response.sendError 404
		}
	}

	private Map toPaymentVO(Company company) {
		def map = RenderUtil.asMapForJSON([
			'currencyCode',
			'apiKey',
			'onlineValidation'
		], company)
		return map
	}

}
