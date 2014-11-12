package com.mogobiz.service

import com.mogobiz.constant.IperConstant
import com.mogobiz.store.exception.CountryException
import grails.converters.JSON
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.util.Holders
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.web.json.JSONObject

class CountryService implements IperConstant {
	static transactional = false

	UuidDataService uuidDataService;
	
	/**
	 * Call Mogopay and return the map (code/name) of countries
	 * @return
	 * @throws CountryException
	 */
	@Cacheable('countryCache')
	public Map<String, String> retrieveCountries() throws CountryException {
		try
		{
			def http = new HTTPBuilder(Holders.config.mogopay.url)
			def data = http.get( path : 'country/countries-for-shipping', query : [:])
			List<JSONObject> res = JSON.parse(data.toString())
			return res.collectEntries {
				[(it.get('code')) : it.get('name')]
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new CountryException("Unable to retrieve countries")
		}
	}

	/**
	 * Returns the list of countries. Each item of list contain a "code", "name" and "active"
	 * @param currentCountry : the current country (if set)
	 * @return
	 */
	List<Map> list(String currentCountry = null) throws CountryException {
		Map<String, String> map = retrieveCountries()
		return map.collect { 
			[code: it.key, name: it.value, active: it.key.equals(currentCountry)]
		}
	}
	
	/**
	 * Returns the list of countries. Each item of list contain a "code", "name" and "active"
	 * @param country : the code of the country to get
	 * @param currentCountry : the current country (if set)
	 * @return
	 */
	Map get(String country, String currentCountry = null) throws CountryException {
		Map<String, String> map = retrieveCountries()
		String name = map.get(country);
		if (name != null) {
			return [code: country, name: name, active: country.equals(currentCountry)]			
		}
		else {
			return null;
		}
	}

	/**
	 * Returns true if the country is managed by Mogopay
	 * @param country
	 * @return
	 */
	boolean isManaged(String country) throws CountryException {
		if (country == null || country.length() == 0) {
			return false;
		}
		else
		{
			Map<String, String> map = retrieveCountries()
			return map.containsKey(country)
		}
	}
	
	String getCurrent() {
		String country = uuidDataService.getCountry();
		if (country == null) {
			List<Map> l = list();
			if (l != null && l.size() > 0) {
				country = l[0]["code"]
			}
		}
		return country;
	}
	
	@CacheEvict(value = 'countryCache', allEntries = true)
	void clearCache() {
	}
}
