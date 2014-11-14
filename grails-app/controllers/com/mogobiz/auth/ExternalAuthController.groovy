/**
 * 
 */
package com.mogobiz.auth

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.domain.GoogleEnv
import grails.converters.JSON

import com.mogobiz.utils.SecurityUtils
import com.mogobiz.store.domain.AccountType
import com.mogobiz.store.domain.Company;
import com.mogobiz.store.domain.ExternalAuthLogin
import com.mogobiz.facebook.FBClient
import com.restfb.exception.FacebookException
import com.restfb.types.User

/**
 * @version $Id $
 * 
 * @author stephane.manciot@ebiznext.com
 *
 */

class ExternalAuthController {
	def grailsApplication

    AuthenticationService authenticationService

/*
 * Authentification au sein d'iper 2010 via OAuth
 */
	def signIn = {
		def status = params.status?params.status:'error'
		switch (status) {
			case 'error':
				redirect (controller:'auth', action:'login')
				break;
			case 'success':
				params.each({ log.info(it) })
				def seller = null
				def account_id = params.account_id as Long
				def targetUri = params.targetUri
				def password = null
				if(account_id){
					def externalAuthLogin = ExternalAuthLogin.get(account_id)
					if(externalAuthLogin){
						def login = externalAuthLogin.login
						def externalId = externalAuthLogin.externalId
						def firstName = externalAuthLogin.firstName
						def lastName = externalAuthLogin.lastName
						def accountType = externalAuthLogin.accountType
						def token = externalAuthLogin.token
						password = token
						def tokenSecret = externalAuthLogin.tokenSecret
						Company company = Company.findByTempSessionId(session.id) // s'agit il ? d'une ouveau enrollement de société ?
						String storeName = null
						if (company) {
							storeName = company.code;
							company.delete(flush:true); // On la supprime car elle va être créée avec l'utilisateur
						}
						try {
							seller = SecurityUtils.extractSeller(login, password, accountType, externalId, token, tokenSecret, firstName, lastName, login, false, storeName);
						}
						catch(Exception e) {
							e.printStackTrace();
							redirect (controller:'auth', action:'login')
							return
						}
					}

					redirect (controller:'auth', action:'signIn', params:[username:seller?.login, password:password, targetUri:targetUri])
				}
				break;
			default:
				redirect (controller:'auth', action:'login')
				break;
		}
	}
	
	/*
	 * Creation de store sans nommage par l'utilisateur (garageSale)
	 */
	def externalSignIn = {
		def status = params.status?params.status:'error'
		def targetUri = params.targetUri
		switch (status) {
			case 'success':
				def account_id = params.account_id as Long
				if(account_id){
					def externalAuthLogin = ExternalAuthLogin.get(account_id)
					if(externalAuthLogin){
						def login = externalAuthLogin.login
						def firstName = externalAuthLogin.firstName
						def lastName = externalAuthLogin.lastName
						def accountType = externalAuthLogin.accountType
						def externalId = externalAuthLogin.externalId
						def token = externalAuthLogin.token
						def password = token
						def tokenSecret = externalAuthLogin.tokenSecret
						String storeName = login
                        SecurityUtils.extractSeller(login, password, accountType, externalId, token, tokenSecret, firstName, lastName, login, true, storeName)
						redirect (url:targetUri+'?accessToken='+token+'&username='+login+'&firstName='+firstName+'&lastName='+lastName)
                        return
					}
				}
                redirect (url:targetUri+'?error=true')
                break;
			default:
				redirect (url:targetUri+'?error=true')
				break;
		}
	}

    def facebook = {
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/externalAuth/signIn?targetUri=' + params.targetUri
		def externalAuthPath = grailsApplication.config.external.authPath + '/facebook/authstart?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}

	def google = {
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/externalAuth/signIn?targetUri=' + params.targetUri
		def externalAuthPath = grailsApplication.config.external.authPath + '/google/authstart?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}

    def idn = {
        def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/externalAuth/signIn?targetUri=' + params.targetUri
        def externalAuthPath = grailsApplication.config.external.authPath + '/idn/authstart?returnURI=' + returnURI.encodeAsURL()
        redirect(url:externalAuthPath)
    }

    def twitter = {
        def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/externalAuth/signIn?targetUri=' + params.targetUri
        def externalAuthPath = grailsApplication.config.external.authPath + '/twitter/authstart?returnURI=' + returnURI.encodeAsURL()
        redirect(url:externalAuthPath)
    }

    def checkToken = {
		String token = params.accessToken
		FBClient fbClient = new FBClient(token)
		User user = null
		try{
			user = fbClient.getUser()
		}
		catch(FacebookException e){
			println(e.message)
		}
		if(user){
			def email = user?.email
			def externalId = user?.id
			def login = email
			def password = token
			def firstName = user?.firstName
			def lastName = user?.lastName
			def accountType = AccountType.FACEBOOK
			def tokenSecret = ""
			SecurityUtils.extractSeller(login, password, accountType, externalId, token, tokenSecret, firstName, lastName, email, true, null)
			render user as JSON
		}
		else{
			response.sendError 401
		}
	}

    // add google access token to company
    def googleShopping = {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        GoogleEnv env = company?.googleEnv
        if(!env){
            response.sendError 400
        }
        def scope = 'https://www.googleapis.com/auth/content'
        def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/externalAuth/googleShoppingToken?targetUri=' + params.targetUri
        def externalAuthPath = grailsApplication.config.external.authPath + '/google/authstart?returnURI=' + returnURI.encodeAsURL() + '&scope=' + scope.encodeAsURL()
        redirect(url:externalAuthPath)
    }

    def googleShoppingToken = {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        GoogleEnv env = company?.googleEnv
        if(!env){
            response.sendError 400
        }
        def status = params.status?params.status:'error'
        def map = [:] as Map
        switch (status) {
            case 'success':
                def account_id = params.account_id as Long
                if(account_id){
                    def externalAuthLogin = ExternalAuthLogin.get(account_id)
                    if(externalAuthLogin){
                        def token = externalAuthLogin.tokenSecret
                        env.client_token = token
                        env.active = true
                        env.validate()
                        if(env.hasErrors()){
                            def errors = []
                            env.errors.allErrors.each {
                                log.error(it)
                                errors << [error:[code:it.code, message:it.getDefaultMessage()]]
                            }
                            map << [errors: errors]
                        }
                        else{
                            env.save()
                            company.googleEnv = env
                            company.save()
                            map << [success:true]
                        }
                    }
                }
                else{
                    map << [success:false]
                }
                break;
            default:
                map << [success:false]
                break;
        }
        render map as JSON
    }

}
