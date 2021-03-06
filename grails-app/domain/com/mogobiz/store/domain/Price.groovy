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
class Price
    implements java.io.Serializable
{
    def priceValidation
    def priceRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * <p>
     * Montant unitaire du SKU
     * </p>
     */
    long price 
    /**
     * 
     */
    java.util.Calendar startDate 
    /**
     * 
     */
    java.util.Calendar stopDate 
    /**
     * 
     */
    java.lang.String discount 
    /**
     * 
     */
    com.mogobiz.store.domain.CustomerProfile customerProfile 

    /**
     * 
     */
    com.mogobiz.store.domain.TicketType ticketType 

    static transients = [ 'priceValidation', 'priceRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'price'

        version false

        id name:'id',column:'id',generator:'native'
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        stopDate column:"stop_date",insertable:true,updateable:true,lazy:false,cache:false
        discount column:"discount",insertable:true,updateable:true,lazy:false,cache:false


        customerProfile column:"customer_profile_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ticketType column:"ticket_type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        price (nullable:true, unique:false)
        startDate (nullable:true, unique:false)
        stopDate (nullable:true, unique:false)
        discount (nullable:true, unique:false)
        customerProfile ( blank:false, nullable:false)
        ticketType ( blank:false, nullable:false)
    }


    String toString(){return priceRender?.asString(this)}

    def beforeInsert = {
        priceValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        priceValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        priceValidation.beforeDelete(this)
    }

    def beforeValidate = {
        priceValidation.beforeValidate(this)
    }

    def afterInsert = {
        priceValidation.afterInsert(this)
    }

    def afterUpdate = {
        priceValidation.afterUpdate(this)
    }

    def afterDelete = {
        priceValidation.afterDelete(this)
    }

    def onLoad = {
        priceValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return priceRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}