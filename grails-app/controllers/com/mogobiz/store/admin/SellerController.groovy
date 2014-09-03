/**
 *
 */
package com.mogobiz.store.admin

import com.mogobiz.store.domain.UserProperty
import grails.converters.JSON
import grails.converters.XML

import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha256Hash

import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Role
import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.UserPermission
import com.mogobiz.utils.RandomPassword

/**
 * Controller utilis� pour g�rer les vendeurs
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class SellerController {
    def ajaxResponseService
    def authenticationService
    def emailConfirmationService

    def show() {
        def id = params.id
        if (id != null) {
            def seller = Seller.get(id)
            if (seller) {

                // to fix potential security hole
                if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                    redirect(controller: 'auth', action: 'unauthorized')
                }

                withFormat {
                    html company: seller
                    xml { render seller as XML }
                    json { render seller as JSON }
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
        def user = authenticationService.retrieveAuthenticatedUser()

        def seller = params['seller']?.id ? Seller.get(params['seller']?.id) : null

        if (seller) {
            def isitme = seller.id == user.id
            def isSeller = isitme && SecurityUtils.getSubject().hasRole(RoleName.PARTNER.name())
            def sellerVO

            // to fix potential security hole
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            }

            // FIXME (bug ihm)
            def oldPassword = seller.password
            def wasAdmin = seller.admin
            def wasActive = seller.active
            seller.properties = params['seller']
            seller.login = seller.email
            seller.password = oldPassword

            // An admin cannot remove the admin role from himself.
            if (wasAdmin && isitme)
                seller.admin = true;

            // An active user cannot remove the active role from himself.
            if (wasActive && isitme)
                seller.active = true;

            if (seller.validate()) {
                //gestion des roles
                seller.roles.clear()
                if (seller.validator) {
                    seller.addToRoles(Role.findByName(RoleName.VALIDATOR))
                }
                if (seller.agent) {
                    seller.addToRoles(Role.findByName(RoleName.SALESAGENT))
                }
                if (seller.sell || isSeller) {
                    seller.addToRoles(Role.findByName(RoleName.PARTNER))
                }

                seller.save()

                def permission = Permission.findByTypeAndPossibleActions(
                        'org.apache.shiro.authz.permission.WildcardPermission',
                        '*')
                if (permission) {
                    UserPermission userPermission = UserPermission.createCriteria().get {
                        eq('permission.id', permission?.id)
                        eq('user.id', seller.id)
                        eq('target', 'company:' + seller.company.id + ':admin')
                        eq('actions', '*')
                    }
                    if (seller.admin) {
                        if (!userPermission) {
                            userPermission = new UserPermission(
                                    permission: permission,
                                    user: seller,
                                    target: 'company:' + seller.company.id + ':admin',
                                    actions: '*'
                            )
                            userPermission.save()
                        }
                    } else if (userPermission) {
                        userPermission.delete()
                    }
                }

                sellerVO = seller.asMapForJSON()
            }

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
        def sellerVO

        def clearPassword = RandomPassword.getRandomPassword(10)
        seller.password = new Sha256Hash(clearPassword)
        seller.login = seller.email
        seller.active = false

        if (seller.validate()) {

            //gestion des roles
            if (seller.validator) {
                seller.addToRoles(Role.findByName(RoleName.VALIDATOR))
            }
            if (seller.agent) {
                seller.addToRoles(Role.findByName(RoleName.SALESAGENT))
            }
            if (seller.sell) {
                seller.addToRoles(Role.findByName(RoleName.PARTNER))
            }
            if (seller.location) {
                def location = seller.location
                def country = location.country
                if (country && country.name) {
                    country = Country.findByName(country.name)
                    location.country = country
                    if (location.validate()) {
                        location.save()
                    } else {
                        seller.errors = location.errors
                    }
                } else {
                    seller.errors = location.errors
                }
            }
            seller.save()

            if (seller.admin) {
                def permission = Permission.findByTypeAndPossibleActions(
                        'org.apache.shiro.authz.permission.WildcardPermission',
                        '*')
                if (permission) {
                    UserPermission userPermission = new UserPermission(
                            permission: permission,
                            user: seller,
                            target: 'company:' + seller.company.id + ':admin',
                            actions: '*'
                    )
                    userPermission.save()
                }
            }
            String targetUri = grailsApplication.config.grails.serverURL;
            // email confirmation
            emailConfirmationService.sendConfirmation(
                    to: seller.email,
                    event: 'signup',
                    eventNamespace: 'plugin.emailConfirmation',
                    subject: "Email confirmation",
                    view: '/sellerPassword/newAccountEmail',
                    model: [
                            login    : seller.email,
                            password : clearPassword,
                            targetUri: targetUri
                    ]
            )

            sellerVO = seller.asMapForJSON()
        }

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
