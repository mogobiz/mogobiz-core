package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 */
@Validateable
class PermissionCommand {

    Long idCompany

    PermissionType permission

    List<String> args = []

    static constraints = {
        idCompany nullable: true
        permission (nullable: false)
        args (nullable: false)
    }
}
