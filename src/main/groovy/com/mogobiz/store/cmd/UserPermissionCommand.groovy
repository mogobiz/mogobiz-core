package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 */
class UserPermissionCommand implements Validateable {

    Long idUser

    PermissionType permission

    List<String> args = []

    static constraints = {
        idUser (nullable: false)
        permission (nullable: false)
        args (nullable: false)
    }
}
