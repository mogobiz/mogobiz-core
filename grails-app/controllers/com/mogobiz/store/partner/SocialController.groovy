/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner;

import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.ExternalAccount
import com.mogobiz.store.domain.ExternalAuthLogin
import com.mogobiz.store.domain.User
import grails.transaction.Transactional

public class SocialController {
	def authenticationService
	def grailsApplication

	@Transactional(readOnly = true)
	def index() {
		User seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		def result = [:]
		List<ExternalAccount> externalAccounts = ExternalAccount.findAllByUser(seller)
		for (ExternalAccount account in externalAccounts) {
			result.put(account.accountType.value, account.login)
		}
		withFormat {
			html result:result
			xml {  render result as XML }
			json { render result as JSON }
		}
	}

	@Transactional
	def socialOff() {
		User seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(!seller){
			response.sendError 401
			return
		}
		List<ExternalAccount> externalAccounts = ExternalAccount.findAllByUser(seller)
		ExternalAccount accountToDelete = externalAccounts.find { it.accountType.value == params["social"]}
		if (accountToDelete) {
			accountToDelete.delete()
		}
		chain(action:"index")
	}
	
	def facebook() {
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/social/addExternalAccount'
		def externalAuthPath = grailsApplication.config.external.authPath + '/facebook/authstart?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}
	def twitter() {
		User seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/social/addExternalAccount'
		def externalAuthPath = grailsApplication.config.external.authPath + '/twitter/authstart?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}
	def google() {
		User seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		def returnURI = 'http' + (request.secure?'s':'') + '://' + request.serverName + ':' + request.serverPort + request.contextPath + '/social/addExternalAccount'
		def externalAuthPath = grailsApplication.config.external.authPath + '/google/hybrid?returnURI=' + returnURI.encodeAsURL()
		redirect(url:externalAuthPath)
	}
	def error = {
	}
	@Transactional
	def addExternalAccount () {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def status = params.status?params.status:'error'
		switch (status) {
			case 'error':
				redirect (action:'error')
				break;
			case 'success':
			// params.each({ log.info(it) })
				def account_id = params.account_id
				if(account_id){
					ExternalAuthLogin externalAuthLogin = ExternalAuthLogin.get(account_id)
					if(externalAuthLogin){
						String login = externalAuthLogin.login
						ExternalAccount externalAccount = ExternalAccount.findByLoginAndAccountType(login, externalAuthLogin.accountType)
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
					redirect (action:'index')
				}
				break;
			default:
				redirect (action:'index')
				break;
		}
	}
}
