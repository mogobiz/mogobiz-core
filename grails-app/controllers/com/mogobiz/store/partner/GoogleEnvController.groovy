/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.store.domain.GoogleEnv
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

class GoogleEnvController {

    def authenticationService

    grails.core.GrailsApplication grailsApplication

    @Transactional(readOnly = true)
    def show() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        GoogleEnv env = seller.company?.googleEnv
        if(env){
            withFormat {
                html env: env
                xml { render env as XML }
                json { render env as JSON }
            }
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def saveOrUpdate() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        GoogleEnv env = company?.googleEnv
        if(!env){
            env = new GoogleEnv(params)
        }
        else if(!env.running){
            env.properties = params
        }
        if(!env.merchant_url){
            env.merchant_url = company.website
        }
        env.validate()
        if(env.hasErrors()){
            env.errors.allErrors.each {
                log.error(it)
            }
        }
        else{
            env.save()
            company.googleEnv = env
            company.save()
        }
        withFormat {
            html env: env
            json { render env as JSON }
        }
    }

    @Transactional
    def delete() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def env = company.googleEnv
        if (env && !env.running) {
            company.googleEnv = null
            company.save()
            env.delete()
        }
        withFormat {
            json { render [:] as JSON }
        }
    }

}
