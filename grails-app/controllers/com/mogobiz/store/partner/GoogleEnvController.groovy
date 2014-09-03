package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.domain.GoogleEnv
import grails.converters.JSON
import grails.converters.XML

class GoogleEnvController {

    AuthenticationService authenticationService

    def grailsApplication

    def show = {
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

    def saveOrUpdate = {
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

    def delete = {
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
