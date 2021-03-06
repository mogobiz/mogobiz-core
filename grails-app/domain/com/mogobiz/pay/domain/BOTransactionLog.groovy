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
class BOTransactionLog
    implements java.io.Serializable
{
    def BOTransactionLogValidation
    def BOTransactionLogRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String extra 
    static transients = [ 'BOTransactionLogValidation', 'BOTransactionLogRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_transaction_log'

        version false

        id name:'id',column:'id',generator:'native'
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        extra (nullable:true, unique:false)
    }


    String toString(){return BOTransactionLogRender?.asString(this)}

    def beforeInsert = {
        BOTransactionLogValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOTransactionLogValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOTransactionLogValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOTransactionLogValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOTransactionLogValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOTransactionLogValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOTransactionLogValidation.afterDelete(this)
    }

    def onLoad = {
        BOTransactionLogValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOTransactionLogRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}