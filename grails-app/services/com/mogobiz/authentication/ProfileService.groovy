package com.mogobiz.authentication

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.ProfilePermission
import com.mogobiz.store.domain.User
import com.mogobiz.store.domain.UserPermission
import com.mogobiz.utils.PermissionType
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional

import java.text.MessageFormat

import static com.mogobiz.utils.ProfileUtils.*

@Transactional
class ProfileService {

    /**
     *
     * @param p - profile
     * @param type - permission
     * @param args - values to apply to the permission
     * @return ProfilePermission
     */
    ProfilePermission getProfilePermission(Profile p, PermissionType type, String ... args){
        DetachedCriteria<ProfilePermission> query = ProfilePermission.where {
            (target == computePermission(type, args)) && profile.id == p.id && permission.id == getWilcardPermission().id
        }
        query.get()
    }

    /**
     *
     * @return wild card permission
     */
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

    /**
     * add or remove permission to a profile
     * @param p - the profile
     * @param add - add/remove profile permission
     * @param type - permission type
     * @param args - values to apply to the permission
     * @return added/removed ProfilePermission
     */
    ProfilePermission saveProfilePermission(Profile p, boolean add, PermissionType type, String ... args){
        ProfilePermission pp = getProfilePermission(p, type, args)
        if(!pp && add){
            pp = new ProfilePermission(target: computePermission(type, args), profile: p, permission: getWilcardPermission())
            pp.save(flush: true)
        }
        else if(pp && !add){
            pp.delete(flush: true)
        }
        pp
    }

    /**
     * create/upgrade a profile within a particular store linked to the profile whose id has been passed as parameter
     * @param idProfile - id profile
     * @param idStore - id store
     * @param name - profile name
     * @return created/upgraded Profile
     */
    Profile applyProfile(Long idProfile, Long idStore, String name = null){
        Profile parent = Profile.load(idProfile)
        Company company = Company.load(idStore)
        Profile child = null
        if(company && parent && !parent.company){
            child = Profile.findByCompanyAndParent(company, parent) ?: new Profile(
                    name: name ?: parent.name,
                    parent: parent,
                    company: company
            ).save(flush:true)
            upgradeProfile(child, parent)
        }
        child
    }

    void removeProfile(Profile profile){
        User.findAllByProfilesInList([profile]).each {user ->
            user.removeFromProfiles(profile)
            user.save(flush: true)
        }
        Profile.findAllByParent(profile).each{child ->
            User.findAllByProfilesInList([child]).each {user ->
                user.removeFromProfiles(profile)
                user.save(flush: true)
            }
            child.delete(flush: true)
        }
        profile.delete(flush: true)
    }

    /**
     * copy profile permissions
     * @param child - the child profile to upgrade
     * @param parent - the parent profile from whom the permissions will be copied
     */
    void upgradeProfile(Profile child, Profile parent) {
        if(!parent.company || child.company?.id == parent.company?.id){
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
    }

    /**
     * upgrade all profiles which are linked to the profile whose id has been passed as parameter
     * @param idProfile - id profile
     */
    void upgradeChildProfiles(Long idProfile){
        Profile parent = Profile.load(idProfile)
        if(parent && !parent.company){
            Profile.findAllByParent(parent).each {child ->
                upgradeProfile(child, parent)
            }
        }
    }

    /**
     * add a user profile
     * @param user - user
     * @param profile - profile
     */
    void addUserProfile(User user, Profile profile){
        if(profile && user && profile.company.id == user.company.id){
            user.addToProfiles(profile)
            user.save(flush: true)
        }
    }

    /**
     * remove a user profile
     * @param user - user
     * @param profile - profile
     */
    void removeUserProfile(User user, Profile profile ){
        if(profile && user && profile in user.profiles){
            user.removeFromProfiles(profile)
            user.save(flush: true)
        }
    }

    UserPermission getUserPermission(User u, PermissionType type, String ... args){
        DetachedCriteria<UserPermission> query = UserPermission.where {
            (target == computePermission(type, args)) && user.id == u.id && permission.id == getWilcardPermission().id
        }
        query.get()

    }

    UserPermission saveUserPermission(User user, boolean add, PermissionType type, String ... args){
        String target = computePermission(type, args)
        def wildCardPermission = getWilcardPermission()
        def userPermission = getUserPermission(user, type, args)
        if(!userPermission && add){
            userPermission = new UserPermission(
                    permission: wildCardPermission,
                    target: target,
                    user: user).save(flush:true)
        }
        else if(userPermission && !add){
            userPermission.delete(flush: true)
        }
        userPermission
    }

    /**
     * allows a copied from an existing profile
     * no link is created between the existing profile and the copied profile
     * @param idProfile - the profile to copy
     * @param idStore - the store in which the profile will be copied
     * @param name - the profile name
     * @return id of the copied profile
     */
    Profile copyProfile(Long idProfile, Long idStore, String name = null){
        Profile parent = Profile.load(idProfile)
        Company company = Company.load(idStore)
        Profile child = null
        if(company && parent && (!parent.company || parent.company.id == idStore)){
            child = new Profile(
                    name: name ?: parent.name,
                    company: company
            ).save(flush:true)
            upgradeProfile(child, parent)
        }
        child
    }
}
