package com.mogobiz.store.cmd

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
    boolean updateUsers = false

    static constraints = {
        idProfile nullable: true
        idCompany blank: false
        name blank: false
    }
}
