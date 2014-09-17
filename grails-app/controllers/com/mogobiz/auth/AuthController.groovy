/**
 *
 */
package com.mogobiz.auth

import com.megatome.grails.RecaptchaService
import com.mogobiz.service.CompanyService
import com.mogobiz.service.SellerService
import com.mogobiz.store.domain.Seller
import com.mogobiz.utils.RSA
import grails.converters.JSON
import grails.util.Environment
import grails.util.Holders
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.web.util.SavedRequest
import org.apache.shiro.web.util.WebUtils

import com.mogobiz.store.domain.Company
import org.codehaus.groovy.grails.web.json.JSONObject;

/**
 * @version $Id $
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class AuthController {

    RecaptchaService recaptchaService
    CompanyService companyService
    SellerService sellerService

    // sign up	Company
    def signUpCompany = {
        // recaptcha check
        boolean ok = true
        if (!recaptchaService.verifyAnswer(session, request.getRemoteAddr(), params)) {
            ok = false
        } else {
            Company company = Company.findByCode(params["code"]);
            if (company) {
                ok = false
            } else {
                company = new Company(code: params["code"], name: params["code"], temp)
                if (companyService.save(company)) {
                    ok = true;
                } else {
                    ok = false;
                }
            }
        }
        render ok

    }

    // sign in
    def signIn = {
        if (params.data) {
            String decodedData = RSA.decrypt(params.data, Environment.currentEnvironment == Environment.PRODUCTION ? new FileInputStream(Holders.config.rsa.key.dir, "private.key") : SCH.servletContext.getResourceAsStream("/WEB-INF/secretkeys/private.key"))
            JSONObject data = JSON.parse(decodedData)
            String storename = data.get("storename")
            String storecode = data.get("storecode")
            String owneremail = data.get("owneremail")
            String ownerfirstname = data.get("ownerfirstname")
            String ownerlastname = data.get("ownerlastname")
            Company company = Company.findByCode(storecode)
            if (company == null) {
                company = new Company(code: storecode, name: storename)
                companyService.save(company)
            }
            Seller seller = Seller.findByEmail(owneremail)
            if (seller == null) {
                seller = new Seller(firstName: ownerfirstname, lastName: ownerlastname, email: owneremail, admin: true, sell: true, validator: true, active: true)
                seller.company = company
                sellerService.save(seller, false)
            }
            seller.save(flush:true)

        }
        // Log the user in the application.
        def authToken = new UsernamePasswordToken(params.username, params.password)
        if (params.rememberMe) {
            authToken.rememberMe = params.rememberMe
        }

        try {
            SecurityUtils.subject.login(authToken)
            // If a controller redirected to this page, redirect back
            // to it.
            def originalParams = session.originalRequestParams
            if (originalParams) {
                log.info "Redirecting to controller '${originalParams.controller}', action '${originalParams.action}'."

                // Remove the original parameters from the session.
                session.removeAttribute('originalRequestParams')

                // Redirect to the target controller and action.
                redirect(controller: originalParams.controller, action: originalParams.action, params: originalParams)
            } else if (params.targetUri) {
                if (params.targetUri.startsWith('http')) {
                    // redirect to the target url
                    redirect(url: params.targetUri)
                } else {
                    // redirect to the target uri
                    redirect(uri: params.targetUri)
                }
            } else {
                // Redirect to the home page.
                redirect(uri: '/')
            }
        }
        catch (AuthenticationException ex) {
            log.info "Authentication failure for user '${params.username}'."
            flash.message = ex.getMessage()//"Invalid username and/or password"
            redirect(action: 'login', params: [username: params.username])
        }
    }

    // sign out
    def signOut = {
        // Log the user out of the application.
        SecurityUtils.subject?.logout()
        // Redirect to the home page.
        redirect(uri: '/')
    }

    // Just show the "unauthorized.gsp" view.
    def unauthorized = {
    }

    // prepare login view for redirection.
    def login = {
        SavedRequest sRequest = WebUtils.getSavedRequest(request)
        if (sRequest) {
            def uri = sRequest.getRequestURI()
            def targetUri = sRequest.getRequestURI() - request.contextPath
            def query = sRequest.getQueryString()

            if (query) {
                if (!query.startsWith('?')) {
                    query = '?' + query
                }

                targetUri += query
            }

            if (!params.targetUri) params.targetUri = targetUri
        }

        return [username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri]
    }
}
