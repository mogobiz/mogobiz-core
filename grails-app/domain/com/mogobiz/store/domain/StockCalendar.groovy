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
class StockCalendar
    implements java.io.Serializable
{
    def stockCalendarValidation
    def stockCalendarRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * <p>
     * Negative value means unlimited
     * </p>
     */
    long stock 
    /**
     * <p>
     * Negative value means unlimited
     * </p>
     */
    long sold 
    /**
     * <p>
     * Période de mise en vente de ce type de ticket
     * </p>
     */
    java.util.Calendar startDate 
    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    /**
     * 
     */
    com.mogobiz.store.domain.TicketType ticketType 

    static transients = [ 'stockCalendarValidation', 'stockCalendarRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'stock_calendar'

        version false

        id name:'id',column:'id',generator:'native'
        stock column:"stock",insertable:true,updateable:true,lazy:false,cache:false
        sold column:"sold",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ticketType column:"ticket_type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        stock ( blank:false, nullable:false, unique:false)
        sold ( blank:false, nullable:false, unique:false)
        startDate (nullable:true, unique:false)
        product ( blank:false, nullable:false)
        ticketType ( blank:false, nullable:false)
    }


    String toString(){return stockCalendarRender?.asString(this)}

    def beforeInsert = {
        stockCalendarValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        stockCalendarValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        stockCalendarValidation.beforeDelete(this)
    }

    def beforeValidate = {
        stockCalendarValidation.beforeValidate(this)
    }

    def afterInsert = {
        stockCalendarValidation.afterInsert(this)
    }

    def afterUpdate = {
        stockCalendarValidation.afterUpdate(this)
    }

    def afterDelete = {
        stockCalendarValidation.afterDelete(this)
    }

    def onLoad = {
        stockCalendarValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return stockCalendarRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}