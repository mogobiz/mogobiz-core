package com.mogobiz.store.cmd

import grails.validation.Validateable

/**
 *
 */
class UserProfileCommand implements Validateable{
    Long idUser

    Long idProfile

    static constraints = {
        idUser (nullable: false)
        idProfile (nullable: false)
    }
}
