package com.mogobiz.store.admin

import com.mogobiz.service.CountryService
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.CompanyProperty
import com.mogobiz.store.domain.EsEnv
import com.mogobiz.store.domain.Seller
import grails.converters.JSON
import grails.converters.XML
import grails.util.Holders
import org.apache.shiro.SecurityUtils

import java.text.Normalizer
import javax.servlet.http.HttpServletResponse

import com.mogobiz.store.domain.Company
import com.mogobiz.store.exception.CountryException;
import com.mogobiz.geolocation.domain.Location
import com.mogobiz.utils.SecureCodec

/**
 * Controller utilisé pour gérer les entreprises
 *
 * @author stephane.manciot@ebiznext.com
 *
 */

class CompanyController {

    def grailsApplication
    def ajaxResponseService
    def authenticationService
    CountryService countryService

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

    def show() {
        def id = params.id
        if (id != null) {
            def company = Company.get(id)
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

            def companiesList = c.list { order("name", "asc") }

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

    private String normalizeName(String companyName) {
        return Normalizer.normalize(companyName, Normalizer.Form.NFD)
                .replaceAll("\\s", "-").replaceAll("\\p{IsM}+", "").replaceAll("[^a-zA-Z0-9-]", "");
    }

    def isNameNew() {
        def normalizedName = normalizeName(params['name'])
        def exist = Company.findByName(normalizedName)
        def map = [:]
        map.put("result", exist ? "error" : "success")
        withFormat {
            json { render map as JSON }
        }
    }

    def isCodeNew() {
        def normalizedCode = normalizeName(params['code'])
        def exist = Company.findByCode(normalizedCode)
        def map = [:]
        map.put("result", exist ? "error" : "success")
        withFormat {
            json { render map as JSON }
        }
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

    /**
     * create new company
     */
    def save() {
        def companyVO = [:]

        Company company = new Company(params['company'])
        if (!company.code)
            company.code = normalizeName(company.name)
        company.code = company.code.toLowerCase()
        Company exist = Company.findByCode(company.code)
        //		if (!exist) {
        //			exist = Company.findByWebsite(company.website)
        //		}
        if (exist) {
            response.sendError 403
            return
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
            companyVO = company.asMapForJSON()
        }

        withFormat {
            html {
                redirect(action: 'show', params: [format: 'html'])
            }
            xml {
                if (!company.hasErrors()) {
                    redirect(action: 'show', params: [format: 'xml'])
                } else {
                    render company.errors as XML
                }
            }
            json {
                render ajaxResponseService.prepareResponse(company, companyVO).asMap() as JSON
            }
        }
    }

    def delete() {
        // TODO
    }

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
