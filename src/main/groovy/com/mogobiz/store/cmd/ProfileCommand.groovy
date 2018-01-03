package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 */
class ProfileCommand implements Validateable{

    Long idProfile
    Long idCompany
    String name
    List<PermissionType> permissions = []

    static constraints = {
        idProfile nullable: true
        idCompany nullable: true
        name blank: false
        permissions minSize: 1
    }
}
