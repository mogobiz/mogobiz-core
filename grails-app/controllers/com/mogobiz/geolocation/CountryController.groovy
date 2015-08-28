package com.mogobiz.geolocation

import com.mogobiz.service.CountryService
import grails.converters.JSON
import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse;

import com.mogobiz.store.exception.CountryException;

class CountryController {

	CountryService countryService
	
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
