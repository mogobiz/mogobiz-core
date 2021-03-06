/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.pay.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class PaymentData
    implements java.io.Serializable
{
    def paymentDataValidation
    def paymentDataRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String xtype 
    /**
     * 
     */
    java.lang.String payload 
    static transients = [ 'paymentDataValidation', 'paymentDataRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'payment_data'

        version false

        id name:'id',column:'id',generator:'native'
        xtype column:"xtype",insertable:true,updateable:true,lazy:false,cache:false
        payload column:"payload",insertable:true,updateable:true,lazy:false,type:"text",cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        xtype ( blank:false, nullable:false, unique:false)
        payload ( blank:false, nullable:false, unique:false)
    }


    String toString(){return paymentDataRender?.asString(this)}

    def beforeInsert = {
        paymentDataValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        paymentDataValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        paymentDataValidation.beforeDelete(this)
    }

    def beforeValidate = {
        paymentDataValidation.beforeValidate(this)
    }

    def afterInsert = {
        paymentDataValidation.afterInsert(this)
    }

    def afterUpdate = {
        paymentDataValidation.afterUpdate(this)
    }

    def afterDelete = {
        paymentDataValidation.afterDelete(this)
    }

    def onLoad = {
        paymentDataValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return paymentDataRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}