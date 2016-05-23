/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 *
 */
package com.mogobiz.auth

import com.megatome.grails.RecaptchaService
import com.mogobiz.service.CompanyService
import com.mogobiz.service.SellerService
import com.mogobiz.store.domain.Company
import grails.transaction.Transactional
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.web.util.SavedRequest
import org.apache.shiro.web.util.WebUtils

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
    @Transactional
    def signUpCompany() {
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
    @Transactional
    def signIn() {
        if (params.data) {
            try {
                sellerService.autoSignIn(params.data)
                if (params.targetUri) {
                    if (params.targetUri.startsWith('http')) {
                        // redirect to the target url
                        redirect(url: params.targetUri)
                    } else {
                        // redirect to the target uri
                        redirect(uri: params.targetUri)
                    }
                }
                else {
                    redirect(uri: '/')
                }
            }
            catch (Exception ex) {
                ex.printStackTrace()
                log.info "Authentication failure for data '${params.data}'."
                String message = g.message(code: ex.getMessage(),args: ["${params.username}"])
                response.sendError (403, message)
            }

        } else {
            // Log the user in the application.
            UsernamePasswordToken authToken = new UsernamePasswordToken(params.username.toString(), params.password.toString())
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
                flash.message = g.message(code: ex.getMessage(),args: ["${params.username}"])//"Invalid username and/or password"
                redirect(action: 'login', params: [username: params.username])
            }
        }

    }

// sign out
    def signOut() {
        // Log the user out of the application.
        SecurityUtils.subject?.logout()
        // Redirect to the home page.
        redirect(uri: '/')
    }

// Just show the "unauthorized.gsp" view.
    def unauthorized() {
    }

// prepare login view for redirection.
    def login() {
        SavedRequest sRequest = WebUtils.getSavedRequest(request)
        if (sRequest) {
            String uri = sRequest.getRequestURI()
            String targetUri = sRequest.getRequestURI() - request.contextPath
            String query = sRequest.getQueryString()

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
