package com.mogobiz.store.cmd

import grails.validation.Validateable

/**
 * Created by yoannbaudy on 24/04/14.
 */
@Validateable
class IBeaconCommand {

    Long id
    String uuid
    String name
    Calendar startDate
    Calendar endDate
    Boolean active

    static constraints = {
        id ( nullable:true)
        uuid ( blank:false, nullable:false)
        name ( blank:false, nullable:false)
        startDate ( nullable:false)
        endDate ( nullable:false)
        active ( nullable:false)
    }
}
