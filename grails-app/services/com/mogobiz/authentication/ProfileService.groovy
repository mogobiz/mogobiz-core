package com.mogobiz.authentication

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.ProfilePermission
import com.mogobiz.store.domain.Role
import com.mogobiz.store.domain.RolePermission
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
        ProfilePermission pp = type ? getProfilePermission(p, type, args) : null
        if(!pp && type && add){
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
     * @param parent - parent profile
     * @param idStore - id store
     * @param name - profile name
     * @return created/upgraded Profile
     */
    Profile applyProfile(Profile parent, Long idStore, String name = null){
        Company company = Company.load(idStore)
        Profile child = null
        if(company && parent && !parent.company){
            child = Profile.findByCompanyAndParent(company, parent) ?: new Profile(
                    name: name ?: parent.name,
                    parent: parent,
                    company: company
            ).save(flush:true)
            upgradeProfile(child)
        }
        child
    }

    /**
     * unbind a profile from its parent
     * @param idProfile - id profile
     * @return Profile
     */
    Profile unbindProfile(Profile child){
        if(child && child.parent){
            child.parent = null
            child.save(flush: true)
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
     * upgrade profile permissions
     * @param child - the child profile to upgrade
     * @param parent - the parent profile from whom the permissions will be copied
     */
    void upgradeProfile(Profile child, Profile parent = child?.parent) {
        if(parent && !parent.company || child.company?.id == parent.company?.id){
            def wildCard = getWilcardPermission()
            def oldPermissions = ProfilePermission.findAllByProfile(child)
            oldPermissions.each { it.delete(flush: true) }
            def permissions = ProfilePermission.findAllByProfile(parent)
            permissions.each { parentPermission ->
                def pp = new ProfilePermission(
                        profile: child,
                        permission: wildCard,
                        key: parentPermission.key,
                        target: MessageFormat.format(parentPermission.target, child.company?.id as String)
                )
                pp.save(flush: true)
            }
        }
    }

    /**
     * upgrade all profiles which are linked to the profile whose id has been passed as parameter
     * @param parent - parent profile
     */
    void upgradeChildProfiles(Profile parent){
        if(parent && !parent.company){
            Profile.findAllByParent(parent).each {child ->
                upgradeProfile(child)
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
        String target = type ? computePermission(type, args) : null
        def wildCardPermission = getWilcardPermission()
        def userPermission = type && user ? getUserPermission(user, type, args) : null
        if(!userPermission && user && target && add){
            userPermission = new UserPermission(
                    permission: wildCardPermission,
                    target: target,
                    key: type.key,
                    user: user).save(flush:true)
        }
        else if(userPermission && !add){
            userPermission.delete(flush: true)
        }
        userPermission
    }

    RolePermission getRolePermission(Role r, PermissionType type, String ... args){
        DetachedCriteria<RolePermission> query = RolePermission.where {
            (target == computePermission(type, args)) && role.id == r.id && permission.id == getWilcardPermission().id
        }
        query.get()
    }

    RolePermission saveRolePermission(Role role, boolean add, PermissionType type, String ... args){
        String target = type ? computePermission(type, args) : null
        def wildCardPermission = getWilcardPermission()
        def rolePermission = type && role ? getRolePermission(role, type, args) : null
        if(!rolePermission && role && target && add){
            rolePermission = new RolePermission(
                    permission: wildCardPermission,
                    target: target,
                    key: type.key,
                    role: role).save(flush:true)
        }
        else if(rolePermission && !add){
            rolePermission.delete(flush: true)
        }
        rolePermission
    }

    /**
     * allows a copied from an existing profile
     * no link is created between the existing profile and the copied profile
     * @param parent - the profile to copy
     * @param idStore - the store in which the profile will be copied
     * @param name - the profile name
     * @return id of the copied profile
     */
    Profile copyProfile(Profile parent, Long idStore, String name = null){
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
