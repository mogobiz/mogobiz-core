package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 */
class PermissionCommand implements Validateable{

    Long idCompany

    PermissionType permission

    List<String> args = []

    static constraints = {
        idCompany nullable: true
        permission (nullable: false)
        args (nullable: false)
    }
}
