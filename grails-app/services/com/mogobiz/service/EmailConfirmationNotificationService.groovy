/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.domain.User
import grails.events.Listener

public class EmailConfirmationNotificationService {
	@Listener(topic='signup.confirmed', namespace='plugin.emailConfirmation') 
    def signupConfirmed(info) {
			EmailConfirmationNotificationService.log.info("User with id ${info.id} has confirmed their email address ${info.email}")
			// load account
			User account = User.findByEmail(info.email)
			// activate account
			if(account){
				account.active = true
				account.save()
			}
			// TODO
			return [controller:'partner', action:'index']
    }
 
    @Listener(topic='signup.timeout', namespace='plugin.emailConfirmation') 
    def signupConfirmationTimedOut(info) {
		EmailConfirmationNotificationService.log.warn("User with id ${info.id} failed to confirm email ${info.email} address after 30 days")
		User account = User.findByEmail(info.email)
		if(account) {
			account.delete()
		}
    }
 
    @Listener(topic='signup.invalid', namespace='plugin.emailConfirmation') 
    def signupConfirmationWasInvalid(info) {
        EmailConfirmationNotificationService.log.info "User ${info.email} failed to confirm"
		return [controller:'partner', action:'index']
    }
	
	@Listener(topic='lost.confirmed', namespace='plugin.emailConfirmation') 
    def lostConfirmed(info) {
    }
 
    @Listener(topic='lost.timeout', namespace='plugin.emailConfirmation') 
    def lostConfirmationTimedOut(info) {
        EmailConfirmationNotificationService.log.info "A user failed to confirm, the token in their link was ${info.id}"
    }
 
    @Listener(topic='lost.invalid', namespace='plugin.emailConfirmation') 
    def lostuserConfirmationWasInvalid(info) {
        EmailConfirmationNotificationService.log.info "User ${info.email} failed to confirm"
        return [controller:'account', action:'index']
    }
}
