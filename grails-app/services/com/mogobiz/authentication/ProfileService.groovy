package com.mogobiz.authentication

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.ProfilePermission
import com.mogobiz.utils.PermissionType
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional

import java.text.MessageFormat

import static com.mogobiz.utils.ProfileUtils.*

@Transactional
class ProfileService {

    ProfilePermission getProfilePermission(Profile p, PermissionType type, String ... args){
        DetachedCriteria<ProfilePermission> query = ProfilePermission.where {
            (target == computePermission(type, args)) && profile.id == p.id
        }
        query.get()
    }

    Permission getWilcardPermission(){
        Permission permission = Permission.findByTypeAndPossibleActions(WILDCARD_PERMISSION, ALL);
        if (!permission) {
            permission = new Permission(type:WILDCARD_PERMISSION, possibleActions:ALL)
            permission.validate()
            if (permission.hasErrors())
            {
                permission.errors.allErrors.each { log.error(it) }
            }
            else
            {
                permission.save(flush:true)
            }
        }
        permission
    }

    void saveProfilePermission(Profile p, boolean add, PermissionType type, String ... args){
        ProfilePermission pp = getProfilePermission(p, type, args)
        if(!pp && add){
            pp = new ProfilePermission(target: computePermission(type, args), profile: p, permission: getWilcardPermission())
            pp.save(flush: true)
        }
        else if(pp && !add){
            pp.delete(flush: true)
        }
    }

    void applyProfile(Long idProfile, Long idStore){
        Profile parent = Profile.load(idProfile)
        Company company = Company.load(idStore)
        if(company && parent && !parent.company){
            def child = Profile.findByCompanyAndParent(company, parent) ?: new Profile(
                    name: parent.name,
                    parent: parent,
                    company: company
            ).save(flush:true)
            upgradeProfile(child, parent)
        }
    }

    void upgradeProfile(Profile child, Profile parent) {
        def wildCard = getWilcardPermission()
        def oldPermissions = ProfilePermission.findAllByProfile(child)
        oldPermissions.each { it.delete(flush: true) }
        def permissions = ProfilePermission.findAllByProfile(parent)
        permissions.each { parentPermission ->
            def pp = new ProfilePermission(
                    profile: child,
                    permission: wildCard,
                    target: MessageFormat.format(parentPermission.target, child.company?.id as String)
            )
            pp.save(flush: true)
        }
    }

    void updateProfile(Profile parent){
        if(!parent.company){
            Profile.findAllByParent(parent).each {child ->
                upgradeProfile(child, parent)
            }
        }
    }
}
