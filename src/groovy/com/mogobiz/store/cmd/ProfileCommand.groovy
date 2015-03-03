package com.mogobiz.store.cmd

import grails.validation.Validateable

/**
 *
 * Created by smanciot on 03/03/15.
 */
@Validateable
class ProfileCommand {

    Long id
    String name
    boolean updateUsers = false

    static constraints = {
        id nullable: true
        name blank: false
    }
}
