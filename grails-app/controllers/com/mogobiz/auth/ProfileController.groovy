package com.mogobiz.auth

import com.mogobiz.store.cmd.ProfileCommand
import com.mogobiz.store.cmd.UserPermissionCommand
import com.mogobiz.store.cmd.UserProfileCommand
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
            def idCompany = cmd.company.id
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, idCompany))) {
                def profile = cmd.profile ?: new Profile()
                profile.name = cmd.name
                profile.validate()
                if(!profile.hasErrors()){
                    profile.save(flush:true)
                    cmd.permissions.each {
                        profileService.saveProfilePermission(
                                profile,
                                true,
                                it,
                                idCompany as String)
                    }
                    PermissionType.minus(cmd.permissions).each {
                        profileService.saveProfilePermission(
                                profile,
                                false,
                                it,
                                idCompany as String)
                    }
                    withFormat {
                        html profile: profile
                        xml { render profile as XML }
                        json { render profile as JSON }
                    }
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
        if(profile && profile.company){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, profile.company.id))){
                profileService.removeProfile(profile)
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
    def apply(Long idProfile, Long idStore, String name){
        if(authenticationService.isPermitted(
                computeStorePermission(
                        PermissionType.ADMIN_STORE_PROFILES, idStore))){
            def profile = profileService.applyProfile(idProfile, idStore, name)
            withFormat {
                html profile: profile
                xml { render profile as XML }
                json { render profile as JSON }
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
        }
    }

    @Transactional
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
        }
    }

    @Transactional
    def addUserProfile(UserProfileCommand cmd){
        cmd.validate()
        if(!cmd.hasErrors()){
            def user = cmd.user
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_USERS, user.company?.id))) {
                profileService.addUserProfile(user, cmd.profile)
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }
    }

    @Transactional
    def removeUserProfile(UserProfileCommand cmd){
        cmd.validate()
        if(!cmd.hasErrors()){
            def user = cmd.user
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_USERS, user.company?.id))) {
                profileService.removeUserProfile(user, cmd.profile)
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }
    }

    @Transactional
    def addUserPermission(UserPermissionCommand cmd){
        cmd.validate()
        if(!cmd.hasErrors()){
            def user = cmd.user
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_USERS, user.company?.id))){
                def args = cmd.args
                def permission = profileService.saveUserPermission(user, true, cmd.permission, args.toArray(new String[args.size()]))
                withFormat {
                    html permission: permission
                    xml { render permission as XML }
                    json { render permission as JSON }
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }
    }

    @Transactional
    def removeUserPermission(UserPermissionCommand cmd){
        cmd.validate()
        if(!cmd.hasErrors()){
            def user = cmd.user
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_USERS, user.company?.id))){
                def args = cmd.args
                def permission = profileService.saveUserPermission(user, false, cmd.permission, args.toArray(new String[args.size()]))
                withFormat {
                    html permission: permission
                    xml { render permission as XML }
                    json { render permission as JSON }
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }
    }
}
