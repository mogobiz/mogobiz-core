package com.mogobiz.store.partner

import com.mogobiz.store.domain.EsEnv
import com.mogobiz.authentication.AuthenticationService
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.cache.CacheEvict

class EsEnvController {

    AuthenticationService authenticationService
    def show = {
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

    @CacheEvict(value='globalCache', allEntries=true)
    def save() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller?.company
        EsEnv env = new EsEnv(params['esenv'])
        env.company = company
        if (env.validate()) {
            if(env.active){
                EsEnv.executeUpdate("update EsEnv set active = :active", [active:false])
                servletContext.setAttribute(env.company.code, env.url)
            }
            env.save()
        }
        withFormat {
            html env: env
            json { render env as JSON }
        }
    }

    @CacheEvict(value='globalCache', allEntries=true)
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

    @CacheEvict(value='globalCache', allEntries=true)
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
