/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class BOCart
    implements java.io.Serializable
{
    def BOCartValidation
    def BOCartRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String transactionUuid 
    /**
     * 
     */
    java.lang.String buyer 
    /**
     * 
     */
    java.util.Calendar date 
    /**
     * 
     */
    long price 
    /**
     * 
     */
    com.mogobiz.store.domain.TransactionStatus status 
    /**
     * 
     */
    java.lang.String currencyCode 
    /**
     * 
     */
    double currencyRate
    /**
     *
     */
    java.lang.String externalOrderId
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'BOCartValidation', 'BOCartRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_cart'

        version false

        id name:'id',column:'id',generator:'native'
        transactionUuid column:"transaction_uuid",insertable:true,updateable:true,lazy:false,cache:false
        buyer column:"buyer",insertable:true,updateable:true,lazy:false,cache:false
        date column:"xdate",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false
        status column:"status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        currencyCode column:"currency_code",insertable:true,updateable:true,lazy:false,cache:false
        currencyRate column:"currency_rate",insertable:true,updateable:true,lazy:false,cache:false
        externalOrderId column:"external_order_id",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        transactionUuid (nullable:true, unique:false)
        buyer ( blank:false, nullable:false, unique:false)
        date ( blank:false, nullable:false, unique:false)
        price ( blank:false, nullable:false, unique:false)
        status ( blank:false, nullable:false, unique:false)
        currencyCode ( blank:false, nullable:false, unique:false)
        currencyRate ( blank:false, nullable:false, unique:false)
        company ( blank:false, nullable:false)
        externalOrderId (nullable:true, unique:false)
    }


    String toString(){return BOCartRender?.asString(this)}

    def beforeInsert = {
        BOCartValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOCartValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOCartValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOCartValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOCartValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOCartValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOCartValidation.afterDelete(this)
    }

    def onLoad = {
        BOCartValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOCartRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}