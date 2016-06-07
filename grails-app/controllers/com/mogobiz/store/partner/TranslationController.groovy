    /*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

    import com.mogobiz.ajax.AjaxResponse
    import grails.converters.JSON
    import grails.transaction.Transactional

    import javax.servlet.http.HttpServletResponse

    /**
 * Translation manager controller
 */
class TranslationController {

	def authenticationService
	def translationService;
	
	/**
	 * Returns a list of configurable languages ​​by the Partner application
	 */
	@Transactional(readOnly = true)
    def languages() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		render translationService.languages(seller) as JSON
	}
	
	/**
	 * Returns a list of Translation corresponding to the given target
	 * Parameter "target" is required
	 */
	@Transactional(readOnly = true)
	def list() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long target = params.long("target")
		String type = params["type"]
		if (target == null || type == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		render translationService.list(target, type) as JSON
	}
	
	/**
	 * Delete the Translation corresponding to the given target and language
	 * Parameters "target" and "language" are required
	 */
	@Transactional
	def delete() {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if (seller == null) {
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
			return
		}

		Long target = params.long("target")
		String lang = params["language"]
		String type = params["type"]
		if (target == null || lang == null || type == null) {
			response.sendError HttpServletResponse.SC_BAD_REQUEST
			return
		}

		AjaxResponse reponse = translationService.delete(target, lang, type);
		render reponse.asMap() as JSON		
	}
	
	/**
	 * Create or update the Translation corresponding to the given target and language.
	 * Parameters "target", "language" and "value" are required
	 */
	@Transactional
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
