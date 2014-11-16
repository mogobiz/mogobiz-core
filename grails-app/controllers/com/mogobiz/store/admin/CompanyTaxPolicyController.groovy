package com.mogobiz.store.admin

import com.mogobiz.ajax.AjaxResponseService
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import org.apache.shiro.SecurityUtils

import com.mogobiz.store.domain.TaxRate

/**
 * 
 * @author stephane.manciot@ebiznext.com
 *
 */
class CompanyTaxPolicyController {

	AjaxResponseService ajaxResponseService

	def initTaxRateDialogPage() {}


	@Transactional(readOnly = true)
	def show(){
		def taxRate = params.id?TaxRate.get(params.id):null
		def companyId = params['company']?.id
		if(taxRate){
			// to fix potential security hole
			if(!SecurityUtils.getSubject().isPermitted('company:'+taxRate.company?.id+':admin')){
				redirect(controller:'auth', action:'unauthorized')
			}
			withFormat {
				html taxRate:taxRate
				xml {  render taxRate as XML }
				json { render ajaxResponseService.prepareResponse(taxRate, taxRate.asMapForJSON()).asMap() as JSON }
			}
		}
		else if(companyId){
			def taxRates = TaxRate.createCriteria().list{
				eq('company.id', Long.parseLong(companyId))
			}.collect { it.asMapForJSON() }
			withFormat {
				html taxRates:taxRates
				xml {  render taxRates as XML }
				json { render taxRates as JSON }
			}
		}
		else{
			response.sendError 404
		}
	}

	@Transactional
	def save() {
		def taxRate = new TaxRate(params['taxRate'])
		if(taxRate.validate()){
			taxRate.save()
		}
		withFormat {
			html taxRate:taxRate
			xml {  render taxRate.hasErrors()?taxRate.errors:taxRate.asMapForJSON() as XML }
			json { render ajaxResponseService.prepareResponse(taxRate, taxRate.asMapForJSON()).asMap() as JSON }
		}
	}

	@Transactional
	def update() {
		def taxRate = params['taxRate']?.id?TaxRate.get(params['taxRate']?.id):null

		if(taxRate){
			// to fix potential security hole
			if(!SecurityUtils.getSubject().isPermitted('company:'+taxRate.company?.id+':admin')){
				redirect(controller:'auth', action:'unauthorized')
			}

			taxRate.properties = params['taxRate']

			if(taxRate.validate()){
				taxRate.save()
			}

			withFormat {
				html taxRate:taxRate
				xml {  render taxRate.hasErrors()?taxRate.errors:taxRate.asMapForJSON() as XML }
				json { render ajaxResponseService.prepareResponse(taxRate, taxRate.asMapForJSON()).asMap() as JSON }
			}
		}
		else{
			response.sendError 404
		}
	}

	@Transactional
	def delete() {
		def taxRate = params['taxRate']?.id?TaxRate.get(params['taxRate']?.id):null

		if(taxRate){
			// to fix potential security hole
			if(!SecurityUtils.getSubject().isPermitted('company:'+taxRate.company?.id+':admin')){
				redirect(controller:'auth', action:'unauthorized')
			}
			taxRate.delete()
			def result = [:]
			result.put("result", "success")
			withFormat {
				xml {  render result as XML }
				json { render result as JSON }
			}
		}
		else{
			response.sendError 404
		}
	}
}
