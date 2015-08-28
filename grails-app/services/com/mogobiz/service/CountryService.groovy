package com.mogobiz.service

import com.mogobiz.constant.IperConstant
import com.mogobiz.store.domain.Country
import com.mogobiz.store.domain.CountryAdmin
import com.mogobiz.store.exception.CountryException

class CountryService implements IperConstant {
	static transactional = false


	public Map<String, String> retrieveCountries() throws CountryException {
		try
		{
			return Country.findAllByShipping(true).collectEntries {
				[(it.code) : it.name]
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
     * Returns the list of country states. Each item of list contain a "code", "name" and "active"
     * @param countryCode : the code of the country to get
     * @return
     */
    def getCountryStates(String countryCode) throws CountryException {
        Country country = Country.findByCode(countryCode);
        if(country != null) {
            try {
                List<CountryAdmin> states =  CountryAdmin.findAllByCountryAndLevel(country, 1)
                return states.sort { a, b -> a.name <=> b.name}
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new CountryException("Unable to retrieve states")
            }
        }
    }
}
