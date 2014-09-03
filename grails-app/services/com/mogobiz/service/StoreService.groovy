package com.mogobiz.service

import com.mogobiz.constant.IperConstant
import com.mogobiz.store.customer.StoreSessionData
import com.mogobiz.store.domain.Company
import com.mogobiz.store.vo.CartVO

class StoreService implements IperConstant {

	UuidDataService uuidDataService
	CatalogService catalogService
	CountryService countryService
	CartService cartService
	RateService rateService
	
	/**
	 * This method initializes the default StoreSessionData for the company
	 * @param country
	 * @param currency
	 * @param companyCode
	 * @return
	 * @throws IllegalArgumentException
	 */
	StoreSessionData loadStoreSessionData(String country, String currency, String companyCode) throws IllegalArgumentException {
		if (!currency || !companyCode) {
			throw new IllegalArgumentException("Country, currency and companyCode are required by the method loadStoreSessionData (" + country + ", " + currency + ", " + companyCode + ")");
		}
		if (country == null) {
			country = countryService.getCurrent();
		}
		uuidDataService.setCountry(country);
		StoreSessionData sessionData = new StoreSessionData(country: country, currency: currency);	
		
		Company company = Company.findByCode(companyCode)
		if (company) {
			sessionData.companyId = company.id;
			sessionData.companyCode = company.code
			
			sessionData.catalogId = catalogService.getDefaultCatalogId(company.id)
			if (sessionData.catalogId) {
				 return sessionData;
			}
			else
			{
				throw new IllegalArgumentException("Invalid company (" + companyCode + "). The default catalog does not exist");				
			}
		}
		else
		{
			throw new IllegalArgumentException("Unknown company (" + companyCode + ")");			
		}
	}
	
	/**
	 * Set the country of the StoreSessionData. If the current country and the new country are differents, 
	 * this method cleans the CartVO. If the clean is not success, the new country isn't set 
	 * @param locale
	 * @param sessionData
	 * @param cart
	 * @param country
	 * @return true is the new country exists and if the cartVO has been cleared
	 * @throws IllegalArgumentException
	 */
	boolean setCountry(Locale locale, StoreSessionData sessionData, CartVO cart, String country) throws IllegalArgumentException {
		if (sessionData == null) {
			throw new IllegalArgumentException("SessionData is required");
		}
		if (country == null) {
			throw new IllegalArgumentException("Country is required");
		}

		boolean result = countryService.isManaged(country) 
		if (result) {
			if (sessionData.country != country) {
				result = cartService.clear(locale, sessionData.currency, cart).success;
				if (result) {
					sessionData.country = country
					uuidDataService.setCountry(country);
				}
			}
		}
		return result;
	}
	
	/**
	 * Set the currency of the StoreSessionData. 
	 * @param sessionData
	 * @param currency
	 * @return true is the new currency exists
	 * @throws IllegalArgumentException
	 */
	boolean setCurrency(StoreSessionData sessionData, String currency) throws IllegalArgumentException {
		if (sessionData == null) {
			throw new IllegalArgumentException("SessionData is required");
		}
		if (currency == null) {
			throw new IllegalArgumentException("Currency is required");
		}

		if (rateService.isManaged(currency) ) {
			sessionData.currency = currency
			return true;
		}
		return false;
	}

}
