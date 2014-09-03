package com.mogobiz.store.partner

import javax.servlet.http.HttpServletResponse

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Resource
import com.mogobiz.service.StoreService

class SanitizedUrlController {
	StoreService storeService
	void getProduct(String companyCode, String sanitizedUrl) {
		session.storeData = storeService.loadStoreSessionData(companyCode)
		Product product = Product.findBySanitizedName(sanitizedUrl)
		Company company = Company.findByCode(companyCode)
		if (product && company) {
			chain(controller:'store', action:'getProduct', params:[id:product.id])
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	void getResource(String companyCode, String sanitizedUrl) {
		session.storeData = storeService.loadStoreSessionData(companyCode)
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
