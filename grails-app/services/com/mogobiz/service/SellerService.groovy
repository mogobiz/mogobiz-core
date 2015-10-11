package com.mogobiz.service

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.authentication.ProfileService
import com.mogobiz.store.domain.*
import com.mogobiz.tools.RandomPassword
import com.mogobiz.utils.PermissionType
import com.mogobiz.utils.SecureCodec
import com.mogobiz.utils.SymmetricCrypt
import grails.converters.JSON
import grails.util.Holders
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.crypto.hash.Sha256Hash
import org.codehaus.groovy.grails.web.json.JSONObject

class SellerService {
    static transactional = true

    def grailsApplication
    def emailConfirmationService
    AuthenticationService authenticationService
    CompanyService companyService
    ProfileService profileService


    def setActiveCompany(Seller seller, Company company) {
        if (!seller.companies?.contains(company) || seller.company == company)
            throw new Exception("Security breach")
        seller.company = company
        seller.save(flush: true)
    }


    def autoSignIn(String paramData) throws Exception {
//        String decodedData = RSA.decrypt(paramData, Environment.currentEnvironment == Environment.PRODUCTION ? new FileInputStream(Holders.config.rsa.key.dir, "private.key") : ServletContextHolder.servletContext.getResourceAsStream("/WEB-INF/secretkeys/private.key"))
//        String decodedData = RSA.decrypt(paramData, new FileInputStream("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys/private.key"))
        String decodedData = SymmetricCrypt.decrypt(paramData, Holders.config.application.secret, "AES")
        JSONObject data = JSON.parse(decodedData) as JSONObject
        String storename = data.get("storename")
        String storecode = data.get("storecode")
        String owneremail = data.get("owneremail")
        String ownerfirstname = data.get("ownerfirstname")
        String ownerlastname = data.get("ownerlastname")

        Company company = Company.findByCode(storecode)
        boolean creation = false
        if (company == null) {
            company = new Company(code: storecode, name: storename, aesPassword: SecureCodec.genKey())
            companyService.save(company)
            creation = true
        }
        Seller seller = Seller.findByEmail(owneremail)
        if (seller == null) {
            String clearPassword = RandomPassword.getRandomPassword(10)
            String password = new Sha256Hash(clearPassword)
            seller = new Seller(password: password, firstName: ownerfirstname, lastName: ownerlastname, email: owneremail, login: owneremail, admin: true, sell: true, validator: true, active: true)
            seller.company = company
            this.save(seller, clearPassword, false)
        }
        if (!seller.companies?.contains(company)) {
            seller.addToCompanies(company)
        }
        seller.company = company
        seller.autosign = true
        seller.save(flush: true)

        if(creation){
            companyService.createEsEnvAndCatalogAndProfiles(company, seller)
            profileService.postUpdateUserProfiles(seller, true, true)
        }

        UsernamePasswordToken authToken = new UsernamePasswordToken(seller.login, seller.password)
        // Log the user in the application.
        SecurityUtils.subject.login(authToken)
    }

    def addCompany(Seller seller, Company company) {
        if (authenticationService.canAdminAllStores()) {
            if (!seller.companies?.contains(company)) {
                seller.addToCompanies(company)
                seller.save(flush: true)
            }
        }
    }

    def removeCompany(Seller seller, Company company) {
        if (authenticationService.canAdminAllStores()) {
            boolean cleanup = company == seller.company
            seller.removeFromCompanies(company)
            if (cleanup)
                seller.company = seller.companies.getAt(0)
            seller.save(flush: true)
        }
    }

    def Seller update(Seller seller, def params) {
        def user = authenticationService.retrieveAuthenticatedUser()
        def isme = seller.id == user.id
        // FIXME (bug ihm)
        def oldPassword = seller.password
//        def wasAdmin = seller.admin
        def wasActive = seller.active
        seller.properties = params['seller']
        seller.login = seller.email
        seller.password = oldPassword

        // An active user cannot remove the active role from himself.
        if (wasActive && isme)
            seller.active = true;

        if (seller.validate()) {
            seller.addToCompanies(seller.company)
            seller.save(flush: true)
            return seller
        } else {
            throw new Exception(seller.errors.allErrors.toListString())
        }
    }


    def Seller save(Seller seller, String clearPassword, Boolean sendConfirmation) {
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
                seller.save(flush: true)
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
