package com.mogobiz.auth

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.authentication.ProfileService
import com.mogobiz.store.cmd.ProfileCommand
import com.mogobiz.store.cmd.UserPermissionCommand
import com.mogobiz.store.cmd.UserProfileCommand
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.User
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

    AuthenticationService authenticationService

    ProfileService profileService

    @Transactional
    def index(Long idStore){
        if(authenticationService.isPermitted(
                computeStorePermission(
                        PermissionType.ADMIN_STORE_PROFILES, idStore))){
            def profiles = idStore ? Profile.where {
                company.id == idStore
            }.list() : Profile.findAllByCompanyIsNull()
            withFormat {
                html profiles: profiles
                xml { render profiles.collect {it.asMapForJSON()} as XML }
                json { render profiles.collect {it.asMapForJSON()} as JSON }
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
                            PermissionType.ADMIN_STORE_PROFILES, profile.company?.id)))
            withFormat {
                html profile: profile.asMapForJSON()
                xml { render profile.asMapForJSON() as XML }
                json { render profile.asMapForJSON() as JSON }
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
            def company = Company.load(idCompany)
//            if(company){
                if(authenticationService.isPermitted(
                        computeStorePermission(
                                PermissionType.ADMIN_STORE_PROFILES, idCompany))) {
                    def profile = cmd.idProfile ? Profile.load(cmd.idProfile) : null
                    if(!profile){
                        profile = new Profile(company: company)
                    }
                    if(profile.company?.id == idCompany){
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
                                html profile: profile.asMapForJSON()
                                xml { render profile.asMapForJSON() as XML }
                                json { render profile.asMapForJSON() as JSON }
                            }
                        }
                        else{
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
                        }
                    }
                    else{
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST)
                    }
                }
                else{
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                }
//            }
//            else{
//                response.sendError(HttpServletResponse.SC_NOT_FOUND)
//            }
        }
    }

    @Transactional
    def update(ProfileCommand cmd){
        forward(action: "save", params: [cmd: cmd])
    }

    @Transactional
    def upgrade(Long id){
        def profile = Profile.load(id)
        if(profile){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, profile.company?.id))){
                if(!profile.parent){
                    profileService.upgradeChildProfiles(profile)
                    withFormat {
                        xml { render [:] as XML }
                        json { render [:] as JSON }
                    }
                }
                else{
                    profile = profileService.upgradeProfile(profile)
                    withFormat {
                        html profile: profile.asMapForJSON()
                        xml { render profile.asMapForJSON() as XML }
                        json { render profile.asMapForJSON() as JSON }
                    }
                }
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
    def delete(Long id){
        def profile = Profile.load(id)
        if(profile){
            if(!profile.company){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST)
            }
            else if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, profile.company.id))){
                profileService.removeProfile(profile)
                withFormat {
                    xml { render [:] as XML }
                    json { render [:] as JSON }
                }
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
        def parent = Profile.load(idProfile)
        if(parent){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, idStore))){
                def child = profileService.applyProfile(parent, idStore, name)
                withFormat {
                    html profile: child.asMapForJSON()
                    xml { render child.asMapForJSON() as XML }
                    json { render child.asMapForJSON() as JSON }
                }
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
    def unbind(Long idProfile){
        def profile = Profile.load(idProfile)
        if(profile){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, profile.company?.id))){
                profile = profileService.unbindProfile(profile)
                withFormat {
                    html profile: profile.asMapForJSON()
                    xml { render profile.asMapForJSON() as XML }
                    json { render profile.asMapForJSON() as JSON }
                }
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
    def copy(Long idProfile, Long idStore, String name){
        def parent = Profile.load(idProfile)
        if(parent){
            if(authenticationService.isPermitted(
                    computeStorePermission(
                            PermissionType.ADMIN_STORE_PROFILES, idStore))){
                def profile = profileService.copyProfile(parent, idStore, name)
                withFormat {
                    html profile: profile.asMapForJSON()
                    xml { render profile.asMapForJSON() as XML }
                    json { render profile.asMapForJSON() as JSON }
                }
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
    def addUserProfile(UserProfileCommand cmd){
        cmd.validate()
        if(!cmd.hasErrors()){
            def user = User.load(cmd.idUser)
            def profile = Profile.load(cmd.idProfile)
            if(user && profile){
                if(authenticationService.isPermitted(
                        computeStorePermission(
                                PermissionType.ADMIN_STORE_USERS, user?.company?.id))) {
                    profileService.addUserProfile(user, profile)
                    withFormat {
                        xml { render [:] as XML }
                        json { render [:] as JSON }
                    }
                }
                else{
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
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
            def user = User.load(cmd.idUser)
            def profile = Profile.load(cmd.idProfile)
            if(user && profile) {
                if (authenticationService.isPermitted(
                        computeStorePermission(
                                PermissionType.ADMIN_STORE_USERS, user.company?.id))) {
                    profileService.removeUserProfile(user, profile)
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
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
            def user = User.load(cmd.idUser)
            if(user){
                if(authenticationService.isPermitted(
                        computeStorePermission(
                                PermissionType.ADMIN_STORE_USERS, user.company?.id))){
                    def args = cmd.args
                    def permission = profileService.saveUserPermission(user, true, cmd.permission, args.toArray(new String[args.size()]))
                    withFormat {
                        html permission: permission.asMapForJSON()
                        xml { render permission.asMapForJSON() as XML }
                        json { render permission.asMapForJSON() as JSON }
                    }
                }
                else{
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
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
            def user = User.load(cmd.idUser)
            if(user) {
                if (authenticationService.isPermitted(
                        computeStorePermission(
                                PermissionType.ADMIN_STORE_USERS, user.company?.id))) {
                    def args = cmd.args
                    profileService.saveUserPermission(user, false, cmd.permission, args.toArray(new String[args.size()]))
                    withFormat {
                        xml { render [:] as XML }
                        json { render [:] as JSON }
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
            }
        }
        else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }
    }
}
