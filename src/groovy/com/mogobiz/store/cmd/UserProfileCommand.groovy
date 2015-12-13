package com.mogobiz.store.cmd

import grails.validation.Validateable

/**
 *
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
