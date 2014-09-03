// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service;

import com.mogobiz.store.domain.Company

/**
 * Management service compagnies
 */
class CompanyService
{

	/**
	 * This methode returns company's informations
	 * @param locale
	 * @param companyCode
	 * @return
	 * @throws java.lang.Exception
	 */
    Map getCompany(Locale locale, java.lang.String companyCode)
    {		
		Map result = [:]
		def company = Company.findByCode(companyCode)
		if (company) {
			result = company.asMapForJSON(null, null, locale?.language)
		}

        return result
	}
}