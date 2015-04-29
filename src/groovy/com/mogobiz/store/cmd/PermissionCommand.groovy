package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 * Created by smanciot on 05/03/15.
 */
@Validateable
class PermissionCommand {

    Long idCompany

    PermissionType permission

    Collection<String> args = []

    static constraints = {
        idCompany nullable: true
        permission (nullable: false)
        args (nullable: false)
    }
}
