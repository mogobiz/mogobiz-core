package com.mogobiz.store.partner

import javax.servlet.http.HttpServletResponse;

import grails.converters.JSON

import com.mogobiz.service.TranslationService;
import com.mogobiz.ajax.AjaxResponse;
import com.mogobiz.authentication.AuthenticationService;

/**
 * Translation manager controller
 */
class TranslationController {

	AuthenticationService authenticationService
	TranslationService translationService;
	
	/**
	 * Returns a list of configurable languages ​​by the Partner application
	 */
    def languages() { 
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		render translationService.languages() as JSON
	}
	
	/**
	 * Returns a list of Translation corresponding to the given target
	 * Parameter "target" is required
	 */
	def list() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long target = params.long("target")
		if (target == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		render translationService.list(target) as JSON		
	}
	
	/**
	 * Delete the Translation corresponding to the given target and language
	 * Parameters "target" and "language" are required
	 */
	def delete() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long target = params.long("target")
		String lang = params["language"]
		if (target == null || lang == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		AjaxResponse reponse = translationService.delete(target, lang);		
		render reponse.asMap() as JSON		
	}
	
	/**
	 * Create or update the Translation corresponding to the given target and language.
	 * Parameters "target", "language" and "value" are required
	 */
	def update() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long target = params.long("target")
		String lang = params["language"]
        String value = params["value"]
        String type = params["type"]
		if (target == null || lang == null || value == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		AjaxResponse reponse = translationService.update(seller, target, lang, value, type);
		render reponse.asMap() as JSON		
	}
}
