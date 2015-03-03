package com.mogobiz.authentication

import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.ProfilePermission
import com.mogobiz.utils.PermissionType
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional

import static com.mogobiz.utils.ProfileUtils.*

@Transactional
class ProfileService {

    ProfilePermission getProfilePermission(PermissionType type, Long idStore, String ... args){
        DetachedCriteria<ProfilePermission> query = ProfilePermission.where {
            (target == computePermission(type, args)) && profile.company.id == idStore
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
}
