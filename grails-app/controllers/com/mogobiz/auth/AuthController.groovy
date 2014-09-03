/**
 * 
 */
package com.mogobiz.auth

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.web.util.SavedRequest
import org.apache.shiro.web.util.WebUtils

import com.mogobiz.store.domain.Company;

/**
 * @version $Id $
 * 
 * @author stephane.manciot@ebiznext.com
 *
 */
class AuthController {

	def recaptchaService

	// sign up	Company
	def signUpCompany = {
        // recaptcha check
		boolean ok = true
		if (!recaptchaService.verifyAnswer(session, request.getRemoteAddr(), params)) {
			 ok = false
		}
		else {
			Company company = Company.findByCode(params["code"]);
			if (company) {
				ok = false
			}
			else {
				company = new Company(code:params["code"], name:params["code"], temp)
				if (company.save(flush:true)) {
					ok = true;
				}
				else {
					 ok = false;
				}
			}
		}
        render ok
		
	}

	// sign in
	def signIn = {
		// Log the user in the application.
		def authToken = new UsernamePasswordToken(params.username, params.password)
		if(params.rememberMe){
			authToken.rememberMe = params.rememberMe
		}        
		
		try{
			SecurityUtils.subject.login(authToken)
			// If a controller redirected to this page, redirect back
			// to it.
			def originalParams = session.originalRequestParams
			if (originalParams){
				log.info "Redirecting to controller '${originalParams.controller}', action '${originalParams.action}'."
				
				// Remove the original parameters from the session.
				session.removeAttribute('originalRequestParams')
				
				// Redirect to the target controller and action.
				redirect(controller: originalParams.controller, action: originalParams.action, params: originalParams)
			}
			else if (params.targetUri){
				if(params.targetUri.startsWith('http')) {
					// redirect to the target url
					redirect(url : params.targetUri)
				}
				else{
					// redirect to the target uri
					redirect(uri : params.targetUri)
				}
			}
			else{
				// Redirect to the home page.
				redirect(uri: '/')
			}
		}
		catch (AuthenticationException ex){
			log.info "Authentication failure for user '${params.username}'."
			flash.message = ex.getMessage()//"Invalid username and/or password"
			redirect(action: 'login', params: [ username: params.username ])
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
			
			if(!params.targetUri) params.targetUri = targetUri
		} 
		
		return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
	}
}
