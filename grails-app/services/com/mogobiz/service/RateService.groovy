package com.mogobiz.service
import com.mogobiz.store.exception.CurrencyRateException
import com.mogobiz.utils.MogopayRate
import grails.converters.JSON
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.util.Holders
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.web.json.JSONObject

import java.text.NumberFormat

class RateService {
	static transactional = false

	/**
	 * Call Mogopay and return the map (code/MogopayRate) of rates
	 * @return
	 * @throws CurrencyRateException
	 */
	@Cacheable('currencyCache')
	private Map<String, MogopayRate> retrieveRates() throws CurrencyRateException {
		try
		{
			def http = new HTTPBuilder(Holders.config.mogopay.url)
			def data = http.get( path : 'rate/list', query : [:])
			List<JSONObject> res = JSON.parse(data.toString())
			return res.collectEntries { JSONObject it ->
				MogopayRate rate = new MogopayRate(code: it.get('currencyCode'), name: it.get('currencyCode'), rate:  it.get('currencyRate') as Double, currencyFractionDigits: it.get('currencyFractionDigits'))
				[(rate.code) : rate]
			}
		}
		catch (Exception ex) {
			throw new CurrencyRateException("Unable to retrieve rates", ex)
		}
	}
	
	/**
	 * Find the MogopayRate corresponding to the given currencyCode
	 * @param currencyCode
	 * @return
	 * @throws CurrencyRateException
	 */
	MogopayRate getMogopayRate(String currencyCode) throws CurrencyRateException {
		Map<String, MogopayRate> map = retrieveRates();
		MogopayRate rate = map.get(currencyCode);
		if (rate == null) {
			throw new CurrencyRateException("Unable to retrieve rate for " + currencyCode + ". The currency rate isn't define")
		}
		return rate;
	}

	/**
	 * Find the rate corresponding to the given currencyCode
	 * @param currencyCode
	 * @return
	 * @throws CurrencyRateException
	 */
	double getRate(String currencyCode) throws CurrencyRateException {
		MogopayRate rate = getMogopayRate(currencyCode);
		return rate.rate.doubleValue();
	}
	
	/**
	 * Returns the list of rates. Each item of list contain a "code", "name" and "active"
	 * @param currentCurrency : the current country (if set)
	 * @return
	 * @throws CurrencyRateException
	 */
	List<Map> list(String currentCurrency) throws CurrencyRateException {
		Map<String, MogopayRate> map = retrieveRates();
		return map.collect { 
			[code: it.key, name: it.value.name, active: it.key.equals(currentCurrency)]
		}
	}
	
	/**
	 * Returns the list of countries. Each item of list contain a "code", "name" and "active"
	 * @param country : the code of the country to get
	 * @param currentCountry : the current country (if set)
	 * @return
	 */
	Map get(String currency, String currentCurrency = null) throws CurrencyRateException {
		MogopayRate rate = getMogopayRate(currency);
		return [code: currency, name: rate.name, rate: rate.rate]
	}

	/**
	 * Returns true if the currencyCode is managed by Mogopay
	 * @param currencyCode
	 * @return
	 * @throws CurrencyRateException
	 */
	boolean isManaged(String currencyCode) throws CurrencyRateException {
		if (currencyCode == null || currencyCode.length() == 0) {
			return false;
		}
		else
		{
			Map<String, MogopayRate> map = retrieveRates();
			return map.containsKey(currencyCode)
		}
	}

	@CacheEvict(value = 'currencyCache', allEntries = true)
	void clearCache() {
	}

	/**
	 * Format the given amount (in the Mogobiz unit) into the given currency by using
	 * the number format of the given country
	 * @param amount
	 * @param currencyCode
	 * @param locale
	 * @return
	 * @throws CurrencyRateException
	 */
	String format(Long amount, String currencyCode, Locale locale) throws CurrencyRateException {
		if (locale == null) {
			throw new CurrencyRateException("Unable to format ammount : locale is null")			
		}
		if (amount != null) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
            numberFormat.setCurrency(Currency.getInstance(currencyCode));
            return numberFormat.format(amount * getRate(currencyCode));
        }
        return null;
	}
	
	/**
	 * Return the amount convert into the given currency. The return is in cents
	 * @param amount
	 * @param currencyCode
	 * @return
	 * @throws CurrencyRateException
	 */
	long calculateAmount(long amount, MogopayRate rate) {
		return (long)(amount * rate.rate.doubleValue() * Math.pow(10, rate.currencyFractionDigits))
	}
	
	/**
	 * Convert the given amount of the given currency into the Mogobiz unit by applying the currency rate 
	 * @param amount
	 * @param currencyCode
	 * @return
	 * @throws CurrencyRateException
	 */
	Long inverse(Double amount, String currencyCode) throws CurrencyRateException {
		if (amount)
		{
			return (long)(amount.doubleValue() / getRate(currencyCode))
		}
		else
		{
			return null;
		}
	}
}
