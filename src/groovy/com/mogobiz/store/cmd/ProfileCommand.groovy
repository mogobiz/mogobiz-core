package com.mogobiz.store.cmd

import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 * Created by smanciot on 03/03/15.
 */
@Validateable
class ProfileCommand {

    Long idProfile
    Long idCompany
    String name
    List<PermissionType> permissions = []

    static constraints = {
        idProfile nullable: true
        idCompany nullable: false
        name blank: false
        permissions minSize: 1
    }
}
