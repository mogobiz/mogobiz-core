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
class BOShopTransaction
    implements java.io.Serializable
{
    def BOShopTransactionValidation
    def BOShopTransactionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String extra 
    static transients = [ 'BOShopTransactionValidation', 'BOShopTransactionRender' ]


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


    String toString(){return BOShopTransactionRender?.asString(this)}

    def beforeInsert = {
        BOShopTransactionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOShopTransactionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOShopTransactionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOShopTransactionValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOShopTransactionValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOShopTransactionValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOShopTransactionValidation.afterDelete(this)
    }

    def onLoad = {
        BOShopTransactionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOShopTransactionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}