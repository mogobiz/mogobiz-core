/**
 * 
 */
package com.mogobiz.store.partner

import java.util.Map;
import grails.util.Environment


import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.ExternalAccount
import com.mogobiz.store.domain.ExternalAuthLogin

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class PartnerController {

	def authenticationService
	def grailsApplication
	def mailService
	def index = {	}

	def settings = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		List<ExternalAccount> externalAccounts = ExternalAccount.findAllByUser(seller)
		withFormat {
			html externalAccounts:externalAccounts
			xml {  render externalAccounts as XML }
			json { render externalAccounts as JSON }
		}
	}

	def addExternalAccount = {
		def status = params.status?params.status:'error'
		switch (status) {
			case 'error':
				redirect (controller:'partner', action:'settings')
				break;
			case 'success':
				params.each({ log.info(it) })
				def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
				if(seller == null){
					response.sendError 401
					return
				}
				def account_id = params.account_id
				if(account_id){
					def externalAuthLogin = ExternalAuthLogin.get(account_id)
					if(externalAuthLogin){
						def login = externalAuthLogin.login
						def externalAccount = ExternalAccount.findByLoginAndAccountType(login, externalAuthLogin.accountType)
						if(!externalAccount){
							externalAccount = new ExternalAccount()
						}
						externalAccount.login = login
						externalAccount.accountType = externalAuthLogin.accountType
						externalAccount.token = externalAuthLogin.token
						externalAccount.tokenSecret = externalAuthLogin.tokenSecret
						externalAccount.user = seller
						if(externalAccount.validate()){
							externalAccount.save()
						}
					}
					redirect (controller:'partner', action:'settings')
				}
				break;
			default:
				redirect (controller:'partner', action:'settings')
				break;
		}
	}

	def addFacebookExternalAccount = {
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/partner/addExternalAccount'
		def externalAuthPath = grailsApplication.config.externalAuthPath + '/facebook/authstart?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}
	
	def addGoogleExternalAccount = {
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/partner/addExternalAccount'
		def externalAuthPath = grailsApplication.config.externalAuthPath + '/google/hybrid?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}

}
