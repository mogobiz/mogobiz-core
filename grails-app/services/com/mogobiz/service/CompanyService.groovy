// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service

import com.mogobiz.exceptions.CompanyAlreadyExistException
import com.mogobiz.exceptions.InvalidDomainObjectException
import com.mogobiz.store.domain.Catalog;
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.EsEnv
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.SecureCodec
import grails.util.Holders

/**
 * Management service compagnies
 */
class CompanyService
{
    def grailsApplication
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
    private void createEsEnvAndCatalog(Company company) {
        EsEnv env = new EsEnv(
                name: 'dev',
                url: Holders.config.elasticsearch.serverURL as String,
                cronExpr: Holders.config.elasticsearch.export.cron as String,
                company: company,
                active: true
        )
        env.save(flush: true)
        Catalog catalog = new Catalog(name: "Default Catalog", uuid: UUID.randomUUID().toString(), social: false, activationDate: new Date(), company: company)
        catalog.save(flush: true)
    }


    Company save(Company company) {
        if (!company.code)
            company.code = IperUtil.normalizeName(company.name)
        company.code = company.code.toLowerCase()
        Company exist = Company.findByCode(company.code)
        if (exist) {
            throw new CompanyAlreadyExistException()
        }
        company.website = company.website ?: "http://" + company.name + grailsApplication.config.rootDomain
        company.aesPassword = SecureCodec.genKey();
        if (company.validate()) {
            if (company.location) {
                def location = company.location
                if (location.validate()) {
                    location.save()
                    company.save()
                    createEsEnvAndCatalog(company)
                } else {
                    company.errors = location.errors
                }
            } else {
                company.save()
                createEsEnvAndCatalog(company)
            }
            return company.asMapForJSON()
        }
        else {
            company.errors.allErrors.each { println it }
            throw new InvalidDomainObjectException("Company")
        }
    }
}