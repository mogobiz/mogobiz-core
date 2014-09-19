package com.mogobiz.service

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.domain.*
import com.mogobiz.utils.RSA
import com.mogobiz.utils.RandomPassword
import grails.converters.JSON
import grails.util.Environment
import grails.util.Holders
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.crypto.hash.Sha256Hash
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.json.JSONObject

class SellerService {
    static transactional = true

    def grailsApplication
    def emailConfirmationService
    AuthenticationService authenticationService
    CompanyService companyService


    def setActiveCompany(Seller seller, Company company) {
        seller.companies.remove(seller.company)
        seller.company = company
        seller.companies.remove(company)
        seller.companies.add(company)
        seller.save(flush: true)
    }


    def autoSignIn(String paramData) throws Exception {
//        String decodedData = RSA.decrypt(paramData, Environment.currentEnvironment == Environment.PRODUCTION ? new FileInputStream(Holders.config.rsa.key.dir, "private.key") : ServletContextHolder.servletContext.getResourceAsStream("/WEB-INF/secretkeys/private.key"))
//        JSONObject data = JSON.parse(decodedData)
//        String storename = data.get("storename")
//        String storecode = data.get("storecode")
//        String owneremail = data.get("owneremail")
//        String ownerfirstname = data.get("ownerfirstname")
//        String ownerlastname = data.get("ownerlastname")
        String storename =" coucou"
        String storecode = "coucou"
        String owneremail ="hayssam@saleh.fr"
        String ownerfirstname = "hayssam"
        String ownerlastname = "saleh"

        Company company = Company.findByCode(storecode)
        if (company == null) {
            company = new Company(code: storecode, name: storename)
            companyService.save(company)
        }
        Seller seller = Seller.findByEmail(owneremail)
        if (seller == null) {
            String clearPassword = RandomPassword.getRandomPassword(10)
            String password = new Sha256Hash(clearPassword)
            seller = new Seller(password: password, firstName: ownerfirstname, lastName: ownerlastname, email: owneremail, login:owneremail, admin: true, sell: true, validator: true, active: true)
            seller.company = company
            this.save(seller, false)
        }
        if (!seller.companies?.contains(company)) {
            seller.addToCompanies(company)
        }
        seller.company = company
        seller.autosign = true
        seller.save(flush: true)

        UsernamePasswordToken authToken = new UsernamePasswordToken(seller.login, seller.password)
        // Log the user in the application.
        SecurityUtils.subject.login(authToken)
    }

    def addCompany(Seller seller, Company company) {
        if (authenticationService.canAdminAllStores()) {
            if (!seller.companies?.contains(company))
                seller.addToCompanies(company)
        }
    }

    def removeCompany(Seller seller, Company company) {
        if (authenticationService.canAdminAllStores()) {
            seller.removeFromCompanies(company)
            seller.save(flush: true)
        }
    }

    def Seller update(Seller seller, def params) {
        def user = authenticationService.retrieveAuthenticatedUser()
        def isitme = seller.id == user.id
        def isSeller = isitme && SecurityUtils.getSubject().hasRole(RoleName.PARTNER.name())
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
            if (!oldSeller.companies?.contains(company))
                oldSeller.addToCompanies(company)
            return oldSeller
        } else {
            if (seller.validate()) {
                if (!seller.companies?.contains(seller.company)) {
                    seller.addToCompanies(seller.company)
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