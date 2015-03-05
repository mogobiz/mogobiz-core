package com.mogobiz.store.cmd

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Profile
import com.mogobiz.utils.PermissionType
import grails.validation.Validateable

/**
 *
 * Created by smanciot on 03/03/15.
 */
@Validateable
class ProfileCommand {

    Profile profile
    Company company
    String name
    Collection<PermissionType> permissions = []

    static constraints = {
        profile nullable: true
        company blank: false
        name blank: false
    }
}
