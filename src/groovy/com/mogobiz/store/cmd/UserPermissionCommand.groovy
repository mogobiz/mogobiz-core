package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 * Created by smanciot on 05/03/15.
 */
@Validateable
class UserPermissionCommand {

    Long idUser

    PermissionType permission

    Collection<String> args = []

    static constraints = {
        idUser (nullable: false)
        permission (nullable: false)
        args (nullable: false)
    }
}
