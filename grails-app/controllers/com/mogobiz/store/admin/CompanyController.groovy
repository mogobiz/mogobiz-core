/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.admin

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.exceptions.CompanyAlreadyExistException
import com.mogobiz.exceptions.InvalidDomainObjectException
import com.mogobiz.geolocation.domain.Location
import com.mogobiz.service.CompanyService
import com.mogobiz.service.CountryService
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.CompanyProperty
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.exception.CountryException
import com.mogobiz.utils.IperUtil
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import org.apache.shiro.SecurityUtils

import javax.servlet.http.HttpServletResponse

/**
 * Controller utilisé pour gérer les entreprises
 *
 * @author stephane.manciot@ebiznext.com
 *
 */

class CompanyController {

    def grailsApplication
    AjaxResponseService ajaxResponseService
    AuthenticationService authenticationService
    CountryService countryService
    CompanyService companyService

    def initDisplayCompany() {
    }

    def initCreateCompany() {
    }

    /**
     * List countries to use to fill country of company
     * @return
     */
    def countries() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            List<Map> countries = countryService.list()
            render countries as JSON
        }
        catch (CountryException ex) {
            log.error(ex.message);
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    @Transactional(readOnly = true)
    def show() {
        if (params.id) {
            def company = Company.get(params.id)
            if (company) {
                def companyVO = company.asMapForJSON()
                withFormat {
                    html companyVO: companyVO
                    xml { render companyVO as XML }
                    json { render companyVO as JSON }
                }
            } else {
                response.sendError 404
            }
        } else if (params.code) {
            def company = Company.findByCode(params.code)
            if (company) {
                def companyVO = company.asMapForJSON()
                withFormat {
                    html companyVO: companyVO
                    xml { render companyVO as XML }
                    json { render companyVO as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            def c = Company.createCriteria()

            def companiesList = c.list { ne("code", "*") order("name", "asc") }

            def retList = new ArrayList()
            companiesList.each { comp ->
                retList.add(comp.asMapForJSON())
            }
            withFormat {
                html retList: retList
                xml { render retList as XML }
                json { render retList as JSON }
            }
        }
    }

    /**
     * update company's general informations
     */
    @Transactional
    def update() {

        def companyVO = [:]
        def id = params['company']?.id
        Company company = params['company']?.id ? Company.get(params['company']?.id) : null
        if (company) {
            String name = company.name
            String code = company.code
            String aesPassword = company.aesPassword;

            company.properties = params['company']

            // we protect ourselves against any code/name change.
            company.name = name
            company.code = code
            company.aesPassword = aesPassword;

            if (company.validate()) {
                if (company.location) {
                    Location location = company.location
                    if (location.validate()) {
                        location.save()
                        company.save()
                    } else {
                        company.errors = location.errors
                    }
                } else {
                    company.save()
                }
                companyVO = company.asMapForJSON()
            }
            withFormat {
                html {
                    render(view: 'show', model: [companyVO: companyVO])
                }
                xml {
                    if (!company.hasErrors()) {
                        render companyVO as XML
                    } else {
                        render company.errors as XML
                    }
                }
                json {
                    render ajaxResponseService.prepareResponse(company, companyVO).asMap() as JSON
                }
            }
        } else {
            response.sendError 404
        }
    }


    @Transactional(readOnly = true)
    def isNameNew() {
        def exist = Company.findByName(params['name'])
        def map = [:]
        map.put("result", exist ? "error" : "success")
        withFormat {
            json { render map as JSON }
        }
    }

    @Transactional(readOnly = true)
    def isCodeNew() {
        def normalizedCode = IperUtil.normalizeName(params['code'])
        def exist = Company.findByCode(normalizedCode)
        def map = [:]
        map.put("result", exist ? "error" : "success")
        withFormat {
            json { render map as JSON }
        }
    }

    /**
     * create new company
     */
    @Transactional
    def save() {
        try {
            Company company = companyService.save(new Company(params['company']))
            Map companyVO = company.asMapForJSON()
            withFormat {
                html {
                    redirect(action: 'show', params: [format: 'html'])
                }
                json {
                    render ajaxResponseService.prepareResponse(company, companyVO).asMap() as JSON
                }
            }
        }
        catch (CompanyAlreadyExistException ex) {
            ex.printStackTrace()
            response.sendError 403
            return
        }
        catch (InvalidDomainObjectException ex) {
            ex.printStackTrace()
            response.sendError 400
            return
        }
    }

    @Transactional
    def delete() {
        // TODO
    }

    @Transactional
    def saveProperty(Long company_id, String name, String value) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
        } else if (seller.company?.id == company_id) {
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {
                Company company = Company.get(company_id)
                CompanyProperty property = CompanyProperty.findByCompanyAndName(company, name)
                property?.delete(flush: true)
                property = new CompanyProperty(company: company, name: name, value: value)
                property.save(flush: true)
                withFormat {
                    json { true }
                }
            }
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def updateProperty(Long company_id, String name, String value) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        } else if (seller.company?.id == company_id) {
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {
                Company company = Company.get(company_id)
                CompanyProperty property = CompanyProperty.findByCompanyAndName(company, name)
                if (property) {
                    property.name = name
                    property.value = value
                    property.save(flush: true)
                }
                withFormat {
                    json { property ? true : false }
                }
            }
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def deleteProperty(Long id) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
        } else {
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {
                CompanyProperty property = CompanyProperty.get(id)
                if (property && property.company == seller.company) {
                    property.delete(flush: true)
                    render([success: true] as Map) as JSON
                    return
                } else {
                    response.sendError 404
                }
            }
        }
    }
}
