package com.mogobiz.store.admin

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.service.SellerService
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.UserProperty
import com.mogobiz.utils.RandomPassword
import grails.converters.JSON
import grails.converters.XML
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha256Hash

class SellerController {
    AjaxResponseService ajaxResponseService
    AuthenticationService authenticationService

    SellerService sellerService

    def addCompany() {
        if (!authenticationService.canAdminAllStores()) {
            redirect(controller: 'auth', action: 'unauthorized')
        }

        long sellerId = params.long("seller.id")
        String companyCode = params["company.code"]
        Company company = Company.findByCode(companyCode)
        Seller paramSeller = Seller.get(sellerId)
        sellerService.addCompany(paramSeller, company)
    }

    def removeCompany() {
        if (!authenticationService.canAdminAllStores()) {
            redirect(controller: 'auth', action: 'unauthorized')
        }

        String sellerId = params["seller.id"]
        String companyCode = params["company.code"]
        Company company = Company.findByCode(companyCode)
        Seller seller = Seller.get(sellerId)
        sellerService.removeCompany(seller, company)
    }

    def setActiveCompany() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        String companyCode = params["company.code"]
        Company company = Company.findByCode(companyCode)
        if (company) {
            sellerService.setActiveCompany(seller, company)
            withFormat {
                json { render true as JSON }
            }
        } else {
            response.sendError 404
        }
    }

    def existEmail(String email) {
        Seller seller = Seller.findByEmail(email)
        withFormat {
            render(seller != null) as JSON
        }
    }

    def show() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null && ! authenticationService.canAdminAllStores()) {
            response.sendError 401
            return
        }
        def id = params.id
        if (id != null) {
            Seller paramSeller = Seller.get(id)
            if (paramSeller) {

                withFormat {
                    json { render paramSeller.asMapForJSON() as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            def companyId = params['company']?.id
            if (companyId) {

                def crit = Seller.createCriteria()
                def sellerList = crit.list {
                    if (companyId) {
                        eq('company.id', Long.parseLong(companyId))
                    }
                    order("login", "asc")
                }

                // construct the seller VO list to send to client
                def sellerVOList = new ArrayList()
                sellerList.each { user ->
                    sellerVOList.add(user.asMapForJSON())
                }
                withFormat {
                    html sellerVOList: sellerVOList
                    xml { render sellerVOList as XML }
                    json { render sellerVOList as JSON }
                }
            } else {
                response.sendError 404
            }
        }
    }

    def update() {
        def seller = params['seller']?.id ? Seller.get(params['seller']?.id) : null

        if (seller) {
            sellerService.update(seller, params)
            Map sellerVO = seller.asMapForJSON()
            withFormat {
                html {
                    if (!seller.hasErrors()) {
                        render(view: 'show', model: [sellerVO: sellerVO])
                    }
                }
                xml {
                    if (!seller.hasErrors()) {
                        render sellerVO as XML
                    } else {
                        render seller.errors as XML
                    }
                }
                json {
                    render ajaxResponseService.prepareResponse(seller, sellerVO).asMap() as JSON
                }
            }
        } else {
            response.sendError 404
        }
    }

    def save() {
        def seller = new Seller(params['seller'])

        def clearPassword = RandomPassword.getRandomPassword(10)
        seller.password = new Sha256Hash(clearPassword)
        seller.login = seller.email
        seller.active = false
        sellerService.save(seller, true)
        Map sellerVO = seller.asMapForJSON()

        withFormat {
            html {
                if (!seller.hasErrors()) {
                    render(view: 'show', model: [sellerVO: sellerVO])
                }
            }
            xml {
                if (!seller.hasErrors()) {
                    render sellerVO as XML
                } else {
                    render seller.errors as XML
                }
            }
            json {
                render ajaxResponseService.prepareResponse(seller, sellerVO).asMap() as JSON
            }
        }
    }

    def delete() {
    }

    def initSellerDialogPage() {

    }

    def isEmailNew() {
        def email = params['email']
        def exist = Seller.findByLogin(email)
        def map = [:]
        map.put("result", exist ? "error" : "success")
        withFormat {
            json { render map as JSON }
        }
    }

    def saveProperty(Long seller_id, String name, String value) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        seller = Seller.get(seller_id)
        if (seller) {
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {
                UserProperty property = UserProperty.findByUserAndName(seller, name)
                property?.delete(flush: true)
                property = new UserProperty(user: seller, name: name, value: value)
                property.save(flush: true)
                withFormat {
                    json { true }
                }
            }
        } else {
            response.sendError 404
        }
    }

    def updateProperty(Long seller_id, String name, String value) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        seller = Seller.get(seller_id)
        if (seller) {
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {
                UserProperty property = UserProperty.findByUserAndName(seller, name)
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
            return
        } else {
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {
                UserProperty property = UserProperty.get(id)
                if (property) {
                    property.delete(flush: true)
                }
                withFormat {
                    json { property ? true : false }
                }
            }

        }
    }
}
