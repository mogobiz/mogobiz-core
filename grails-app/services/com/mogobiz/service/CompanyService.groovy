/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.authentication.ProfileService
import com.mogobiz.exceptions.CompanyAlreadyExistException
import com.mogobiz.exceptions.InvalidDomainObjectException
import com.mogobiz.store.domain.Catalog;
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.EsEnv
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.Seller
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.PermissionType
import com.mogobiz.utils.SecureCodec
import grails.util.Holders

import static com.mogobiz.utils.ProfileUtils.*

/**
 * Management service compagnies
 */
class CompanyService
{
    def grailsApplication

    AuthenticationService authenticationService

    ProfileService profileService

	/**
	 * This methode returns company's informations
	 * @param locale
	 * @param companyCode
	 * @return
	 * @throws java.lang.Exception
	 */
    Map getCompany(Locale locale, String companyCode)
    {
		Map result = [:]
		def company = Company.findByCode(companyCode)
		if (company) {
			result = company.asMapForJSON(null, null, locale?.language)
		}

        return result
	}

    void createEsEnvAndCatalogAndProfiles(Company company, Seller seller = null) {
        def profiles = Profile.findAllByCompanyIsNull()
        profiles.each {parent ->
            profileService.applyProfile(parent, company.id)
        }
        EsEnv env = EsEnv.findByCompanyAndName(company, 'dev')
        if(!env){
            env = new EsEnv(
                    name: 'dev',
                    url: Holders.config.elasticsearch.serverURL as String,
                    cronExpr: Holders.config.elasticsearch.export.cron as String,
                    company: company,
                    active: true
            )
            env.save(flush: true)
        }
        Catalog catalog = Catalog.findByCompanyAndName(company, "Default Catalog")
        if(!catalog){
            catalog = new Catalog(name: "Default Catalog", uuid: UUID.randomUUID().toString(), social: false, activationDate: new Date(), company: company)
            catalog.save(flush: true)
        }
        if(seller){
            // admin profile
            def profile = Profile.findByCompanyAndName(company, 'admin')
            profileService.addSellerProfile(seller, profile)
            // seller profile
            profile = Profile.findByCompanyAndName(company, 'seller')
            profileService.addSellerProfile(seller, profile)
            // validator profile
            profile = Profile.findByCompanyAndName(company, 'validator')
            profileService.addSellerProfile(seller, profile)
            // user permissions related to this store
            profileService.saveUserPermission(
                    seller,
                    true,
                    PermissionType.PUBLISH_STORE_CATALOGS_TO_ENV,
                    company.id as String,
                    env.id as String
            )
            profileService.saveUserPermission(
                    seller,
                    true,
                    PermissionType.UPDATE_STORE_CATALOG,
                    company.id as String,
                    catalog.id as String
            )
            profileService.saveUserPermission(
                    seller,
                    false,
                    PermissionType.UPDATE_STORE_CATEGORY_WITHIN_CATALOG,
                    company.id as String,
                    catalog.id as String,
                    ALL
            )
        }
    }


    Map save(Company company) {
        if (!company.code)
            company.code = IperUtil.normalizeName(company.name)
        company.code = company.code.toLowerCase()
        Company exist = Company.findByCode(company.code)
        if (exist) {
            throw new CompanyAlreadyExistException()
        }
        company.website = company.website ?: "http://" + company.code + ".mogobiz.com"
        company.aesPassword = SecureCodec.genKey();
        if (company.validate()) {
            if (company.location) {
                def location = company.location
                if (location.validate()) {
                    location.save()
                    company.save()
                    createEsEnvAndCatalogAndProfiles(company)
                } else {
                    company.errors = location.errors
                }
            } else {
                company.save()
                createEsEnvAndCatalogAndProfiles(company)
            }
            return company.asMapForJSON()
        }
        else {
            company.errors.allErrors.each { println it }
            throw new InvalidDomainObjectException("Company")
        }
    }
}
