package com.mogobiz.store.cmd

import grails.validation.Validateable

/**
 */
@Validateable
class IBeaconCommand {

    Long id
    String uuid
    String name
    Calendar startDate
    Calendar endDate
    Boolean active
    String major
    String minor

    static constraints = {
        id ( nullable:true)
        uuid ( blank:false, nullable:false)
        name ( blank:false, nullable:false)
        major ( blank:false, nullable:false)
        minor ( blank:false, nullable:false)
        startDate ( nullable:false)
        endDate ( nullable:false)
        active ( nullable:false)
    }
}
