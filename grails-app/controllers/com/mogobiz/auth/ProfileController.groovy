package com.mogobiz.auth

import com.mogobiz.store.domain.Profile
import com.mogobiz.utils.PermissionType
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse

/**
 *
 * Created by smanciot on 10/02/15.
 */
class ProfileController {

    def authenticationService

    public static final String WILDCARD_PERMISSION = 'org.apache.shiro.authz.permission.WildcardPermission'
    public static final String ALL = '*'

    @Transactional
    def index(Long idStore){
        def store = idStore ? idStore.toString() : authenticationService.ALL
        if(authenticationService.isPermitted("company:$store:profile:show")){
            def profiles = idStore ? Profile.where {
                company.id == idStore
            }.list() : Profile.findAll()
            withFormat {
                html profiles: profiles
                xml { render profiles as XML }
                json { render profiles as JSON }
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
        }
    }

    @Transactional
    def show(Long idProfile){
        def profile = Profile.load(idProfile)
        if(profile){
            if(authenticationService.isPermitted("company:${profile.company.id}:profile:show"))
            withFormat {
                html profiles: profile
                xml { render profile as XML }
                json { render profile as JSON }
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    private String updateUsersPermission(Long idStore){
        authenticationService.computeStorePermission(PermissionType.UPDATE_USERS, idStore)
    }

}
