package com.mogobiz.store.cmd

import grails.validation.Validateable

@Validateable
class ShippingRuleCommand {
    Long id
    String countryCode
    String price
    Long minAmount
    Long maxAmount

    static constraints = {
        id ( nullable:true)
        countryCode ( blank:false, nullable:false)
        price ( blank:false, nullable:false)
        minAmount ( blank:false, nullable:false)
        maxAmount ( blank:false, nullable:false)
    }
}
