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
class EventPeriodSale
    implements java.io.Serializable
{
    def eventPeriodSaleValidation
    def eventPeriodSaleRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    long nbTicketSold  = 0 
    /**
     * 
     */
    java.util.Date eventDate 
    /**
     * 
     */
    java.util.Date eventStartTime 
    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    static transients = [ 'eventPeriodSaleValidation', 'eventPeriodSaleRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'event_period_sale'

        version false

        id name:'id',column:'id',generator:'native'
        nbTicketSold column:"nb_ticket_sold",insertable:true,updateable:true,lazy:false,cache:false
        eventDate column:"event_date",insertable:true,updateable:true,lazy:false,cache:false
        eventStartTime column:"event_start_time",insertable:true,updateable:true,lazy:false,cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        nbTicketSold ( blank:false, nullable:false, unique:false)
        eventDate (nullable:true, unique:false)
        eventStartTime (nullable:true, unique:false)
        product (nullable:true)
    }


    String toString(){return eventPeriodSaleRender?.asString(this)}

    def beforeInsert = {
        eventPeriodSaleValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        eventPeriodSaleValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        eventPeriodSaleValidation.beforeDelete(this)
    }

    def beforeValidate = {
        eventPeriodSaleValidation.beforeValidate(this)
    }

    def afterInsert = {
        eventPeriodSaleValidation.afterInsert(this)
    }

    def afterUpdate = {
        eventPeriodSaleValidation.afterUpdate(this)
    }

    def afterDelete = {
        eventPeriodSaleValidation.afterDelete(this)
    }

    def onLoad = {
        eventPeriodSaleValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return eventPeriodSaleRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}