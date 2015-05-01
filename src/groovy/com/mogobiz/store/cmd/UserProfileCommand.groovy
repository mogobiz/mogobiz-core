package com.mogobiz.store.cmd

import grails.validation.Validateable

/**
 *
 * Created by smanciot on 05/03/15.
 */
@Validateable
class UserProfileCommand {
    Long idUser

    Long idProfile

    static constraints = {
        idUser (nullable: false)
        idProfile (nullable: false)
    }
}
