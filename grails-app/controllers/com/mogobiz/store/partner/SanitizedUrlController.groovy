/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Resource

class SanitizedUrlController {
	@Transactional(readOnly = true)
	def getProduct(String companyCode, String sanitizedUrl) {
		Product product = Product.findBySanitizedName(sanitizedUrl)
		Company company = Company.findByCode(companyCode)
		if (product && company) {
			chain(controller:'store', action:'getProduct', params:[id:product.id])
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Transactional(readOnly = true)
	def getResource(String companyCode, String sanitizedUrl) {
		Resource resource = Resource.findBySanitizedName(sanitizedUrl)
		Company company = Company.findByCode(companyCode)
		if (resource && company) {
			chain(controller:'resource', action:'display', params:[id:resource.id])
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
