package com.mogobiz.store.admin

import com.mogobiz.service.TaxRateService
import grails.converters.JSON
import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse;

import com.mogobiz.store.domain.Company;
import com.mogobiz.store.domain.LocalTaxRate;
import com.mogobiz.store.domain.TaxRate;
import com.mogobiz.store.domain.User;
import com.mogobiz.ajax.AjaxResponseService;
import com.mogobiz.authentication.AuthenticationService;

class TaxRateController {

	AuthenticationService authenticationService
	TaxRateService taxRateService;
	AjaxResponseService ajaxResponseService	
	
	/**
	 * List all TaxRate for the company of the admin
	 * @return
	 */
	@Transactional(readOnly = true)
    def listTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		List<TaxRate> list = taxRateService.listTaxRate(company);
		List<Map> result = [];
		list.each {TaxRate taxRate ->
			result << taxRate.asMapForJSON();
		}
		render result as JSON;
	}
	
	/**
	 * Create a TaxRate for the company of the admin
	 * @return
	 */
	@Transactional
	def createTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		String name = params["name"];
		if (name == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		TaxRate taxRate = taxRateService.createTaxRate(company, name);
		renderTaxRate(taxRate);
	}
	
	/**
	 * Update the given TaxRate for the company of the admin
	 * @return
	 */
	@Transactional
	def updateTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long taxRateId = params.long("taxRateId");
		String name = params["name"];
		if (taxRateId == null || name == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		TaxRate taxRate = taxRateService.updateTaxRate(company, taxRateId, name);
		renderTaxRate(taxRate);
	}

	/**
	 * Delete the given TaxRate for the company of the admin
	 * @return
	 */
	@Transactional
	def deleteTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long taxRateId = params.long("taxRateId");
		if (taxRateId == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		TaxRate taxRate = taxRateService.deleteTaxRate(company, taxRateId);
		renderTaxRate(taxRate);
	}

	/**
	 * List all LocalTaxRate for the given TaxRate for the company of the admin
	 * @return
	 */
	@Transactional(readOnly = true)
	def listLocalTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long taxRateId = params.long("taxRateId");
		if (taxRateId == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		List<LocalTaxRate> list = taxRateService.listLocalTaxRate(company, taxRateId);
		List<Map> result = [];
		list.each {LocalTaxRate localTaxRate ->
			result << localTaxRate.asMapForJSON();
		}
		render result as JSON;
	}
	
	/**
	 * Create a LocalTaxRate for the given TaxRate for the given company
	 * @return
	 */
	@Transactional
	def createLocalTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long taxRateId = params.long("taxRateId");
		Float rate = params.float("rate");
		Boolean active = params.boolean("active");
		String country = params["country"];
		String state = params["state"];
		if (taxRateId == null || rate == null || active == null || country == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		LocalTaxRate localTaxRate = taxRateService.createLocalTaxRate(company, taxRateId, country, state, rate, active);
		renderLocalTaxRate(localTaxRate);
	}

	/**
	 * Update a LocalTaxRate for the given TaxRate for the given company
	 * @return
	 */
	@Transactional
	def updateLocalTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long localTaxRateId = params.long("localTaxRateId");
		Float rate = params.float("rate");
		Boolean active = params.boolean("active");
        String state = params["state"];
		if (localTaxRateId == null || rate == null || active == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		LocalTaxRate localTaxRate = taxRateService.updateLocalTaxRate(company, localTaxRateId, state, rate, active);
		renderLocalTaxRate(localTaxRate);
	}
	
	/**
	 * Delete a LocalTaxRate for the given TaxRate for the given company
	 * @return
	 */
	@Transactional
	def deleteLocalTaxRate() {
		User admin = request.admin ? request.admin : authenticationService.retrieveAuthenticatedUser()
		Company company = getCompanyFromUserOrParam(admin);
		if (admin == null || company == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long localTaxRateId = params.long("localTaxRateId");
		if (localTaxRateId == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		LocalTaxRate localTaxRate = taxRateService.deleteLocalTaxRate(company, localTaxRateId);
		renderLocalTaxRate(localTaxRate);
	}

	private void renderTaxRate(TaxRate taxRate) {
		if (taxRate == null) {
			response.sendError HttpServletResponse.SC_NOT_FOUND
		}
		else {
			render ajaxResponseService.prepareResponse(taxRate,taxRate?.asMapForJSON()).asMap() as JSON
		}
	}
	
	private void renderLocalTaxRate(LocalTaxRate localTaxRate) {
		if (localTaxRate == null) {
			response.sendError HttpServletResponse.SC_NOT_FOUND
		}
		else {
			render ajaxResponseService.prepareResponse(localTaxRate,localTaxRate?.asMapForJSON()).asMap() as JSON
		}
	}
	
	private Company getCompanyFromUserOrParam(User admin) {
		Long companyId = params.long("companyId")		
		if (admin.company != null && admin.company.id == companyId) {
			return admin.company
		}
		return Company.get(companyId);
	}
}
