/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.geolocation

import com.mogobiz.store.exception.CountryException
import grails.converters.JSON

import javax.servlet.http.HttpServletResponse

class CountryController {

	def countryService
	
	/**
	 * List all countries
	 * @return
	 */
	def countries() {
		try
		{
			List<Map> countries = countryService.list()
			render countries as JSON
		}
		catch (CountryException ex) {
			log.error(ex.message);
			response.sendError HttpServletResponse.SC_NOT_FOUND
		}
	}

    def countryStates(String countryCode){
        try
        {
            def sates = countryService.getCountryStates(countryCode)
            render sates as JSON
        }
        catch (CountryException ex) {
            log.error(ex.message);
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }
}
