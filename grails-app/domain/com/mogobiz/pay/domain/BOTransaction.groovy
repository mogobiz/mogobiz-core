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
class BOTransaction
    implements java.io.Serializable
{
    def BOTransactionValidation
    def BOTransactionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String extra 
    static transients = [ 'BOTransactionValidation', 'BOTransactionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_transaction'

        version false

        id name:'id',column:'id',generator:'native'
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        extra (nullable:true, unique:false)
    }


    String toString(){return BOTransactionRender?.asString(this)}

    def beforeInsert = {
        BOTransactionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOTransactionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOTransactionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOTransactionValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOTransactionValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOTransactionValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOTransactionValidation.afterDelete(this)
    }

    def onLoad = {
        BOTransactionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOTransactionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}