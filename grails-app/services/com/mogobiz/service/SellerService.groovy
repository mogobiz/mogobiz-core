package com.mogobiz.service

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Role
import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.UserPermission
import org.apache.shiro.SecurityUtils

class SellerService {
    def grailsApplication
    def emailConfirmationService
    AuthenticationService authenticationService

    static transactional = true

    def setActiveCompany(Seller seller, Company company) {
        seller.companies.remove(seller.company)
        seller.company = company
        seller.companies.remove(company)
        seller.companies.add(company)
        seller.save(flush: true)
    }

    def addCompany(Seller seller, Company company) {
        if (!seller.companies.contains(company))
            seller.companies.add(company)
    }

    def removeCompany(Seller seller, Company company) {
        seller.companies.remove(company)
        seller.save(flush: true)
    }

    def Seller update(Seller seller, def params) {
        def user = authenticationService.retrieveAuthenticatedUser()
        def isitme = seller.id == user.id
        def isSeller = isitme && SecurityUtils.getSubject().hasRole(RoleName.PARTNER.name())
        def sellerVO
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
            seller.companies.add(seller.company)
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
            return seller
        } else {
            throw new Exception(seller.errors.allErrors.toListString())
        }
    }


    def Seller save(Seller seller, Boolean sendConfirmation) {
        Seller oldSeller = Seller.findByEmail(seller.email)
        if (oldSeller != null) {
            Company company = Company.get(seller.company.id)
            if (!oldSeller.companies.contains(company))
                oldSeller.companies.add(company)
            return oldSeller
        } else {
            if (seller.validate()) {
                if (seller.companies != null && !seller.companies.contains(seller.company)) {
                    seller.companies.add(seller.company)
                }
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
                if (sendConfirmation) {
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
                }
                return seller
            } else {
                throw new Exception(seller.errors.allErrors.toListString())
            }
        }
    }
}
