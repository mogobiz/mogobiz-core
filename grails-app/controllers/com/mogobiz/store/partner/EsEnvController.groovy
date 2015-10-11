/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.store.domain.EsEnv
import com.mogobiz.authentication.AuthenticationService
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import org.quartz.CronExpression

class EsEnvController {

    AuthenticationService authenticationService


    @Transactional(readOnly = true)
    def show() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        Long id = params['esenv']?.id?.toLong()
        if (id != null) {
            EsEnv env = EsEnv.get(id)
            if (env && env.company == seller.company) {
                withFormat {
                    html env: env
                    xml { render env as XML }
                    json { render env as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            List<EsEnv> envs = EsEnv.findAllByCompany(seller.company)
            if (!envs) {
                envs = []
            }
            withFormat {
                html envs: envs
                json { render envs as JSON }
            }
        }
    }

    @Transactional
    def save() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        EsEnv env = new EsEnv(params['esenv'] as Map)
        env.company = company
        if (env.validate()) {
            if(!CronExpression.isValidExpression(env.cronExpr)){
                log.error("invalid cron expression ${env.cronExpr}")
            }
            else{
                if(env.active){
                    EsEnv.executeUpdate("update EsEnv set active = :active", [active:false])
                    servletContext.setAttribute(env.company.code, env.url)
                }
                env.save()
            }
        }
        withFormat {
            html env: env
            json { render env as JSON }
        }
    }

    @Transactional
    def update() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def env = params['esenv']?.id ? EsEnv.get(params['esenv']?.id) : null
        if (env && env.company == company) {
            env.properties = params['esenv']
            if (env.validate()) {
                if(!CronExpression.isValidExpression(env.cronExpr)){
                    log.error("invalid cron expression ${env.cronExpr}")
                }
                else {
                    env.save(flush: true)
                    if (env.active) {
                        EsEnv.executeUpdate("update EsEnv set active = :active where company.id =  :idCompany and id != :id ", [id: env.id, active: false, idCompany: company.id])
                    }
                }
            }
            else {
                env.errors.allErrors.each {
                    log.error(it)
                }
            }
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
        def env = params['esenv']?.id ? EsEnv.get(params['esenv']?.id) : null
        if (env && env.company == company) {
            env.delete()
        }
        withFormat {
            json { render [:] as JSON }
        }
    }
}
