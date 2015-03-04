package com.mogobiz.auth

import com.mogobiz.store.cmd.ProfileCommand
import com.mogobiz.store.domain.Profile
import com.mogobiz.utils.PermissionType
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse

import static com.mogobiz.utils.ProfileUtils.*

/**
 *
 * Created by smanciot on 10/02/15.
 */
class ProfileController {

    def authenticationService

    def profileService

    @Transactional
    def index(Long idStore){
        if(authenticationService.isPermitted(
                computeStorePermission(
                        PermissionType.ADMIN_STORE_PROFILES, idStore))){
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
    def show(Long id){
        def profile = Profile.load(id)
        if(profile){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, profile.company.id)))
            withFormat {
                html profile: profile
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

    @Transactional
    def save(ProfileCommand cmd){
        cmd.validate()
        if(!cmd.hasErrors()){
            def idCompany = cmd.idCompany
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, idCompany))) {
                def idProfile = cmd.idProfile
                def profile = idProfile ? Profile.load(idProfile) : new Profile()
                profile.name = cmd.name
                profile.validate()
                if(!profile.hasErrors()){
                    profile.save(flush:true)
                    profileService.saveProfilePermission(
                            profile,
                            cmd.users,
                            PermissionType.ADMIN_STORE_USERS,
                            idCompany as String)
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
    }

    @Transactional
    def delete(Long id){
        def profile = Profile.load(id)
        if(profile){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, profile.company.id))){
                profile.delete(flush:true)
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def copy(Long idProfile, Long idStore, String name){
        if(authenticationService.isPermitted(
                computeStorePermission(
                        PermissionType.ADMIN_STORE_PROFILES, idStore))){
            def profile = profileService.copyProfile(idProfile, idStore, name)
            withFormat {
                html profile: profile
                xml { render profile as XML }
                json { render profile as JSON }
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

}
