package com.mogobiz.store.cmd

import com.mogobiz.store.domain.User
import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 * Created by smanciot on 05/03/15.
 */
@Validateable
class UserPermissionCommand {

    User user

    PermissionType permission

    Collection<String> args = []

    static constraints = {
        user (nullable: false)
        permission (nullable: false)
        args (nullable: false)
    }
}
