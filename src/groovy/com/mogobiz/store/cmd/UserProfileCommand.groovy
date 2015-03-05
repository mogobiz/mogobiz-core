package com.mogobiz.store.cmd

import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.User
import grails.validation.Validateable

/**
 *
 * Created by smanciot on 05/03/15.
 */
@Validateable
class UserProfileCommand {
    User user

    Profile profile

    static constraints = {
        user (nullable: false)
        profile (nullable: false)
    }
}
