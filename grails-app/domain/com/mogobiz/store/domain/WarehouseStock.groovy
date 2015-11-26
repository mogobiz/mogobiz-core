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
class WarehouseStock
    implements java.io.Serializable
{
    def warehouseStockValidation
    def warehouseStockRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * <p>
     * Negative value means unlimited
     * </p>
     */
    java.lang.Long stock 
    /**
     * 
     */
    com.mogobiz.store.domain.Warehouse warehouse 

    /**
     * 
     */
    com.mogobiz.store.domain.TicketType ticketType 

    static transients = [ 'warehouseStockValidation', 'warehouseStockRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'warehouse_stock'

        version false

        id name:'id',column:'id',generator:'native'
        stock column:"stock",insertable:true,updateable:true,lazy:false,cache:false


        warehouse column:"warehouse_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ticketType column:"ticket_type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        stock (nullable:true, unique:false)
        warehouse ( blank:false, nullable:false)
        ticketType ( blank:false, nullable:false)
    }


    String toString(){return warehouseStockRender?.asString(this)}

    def beforeInsert = {
        warehouseStockValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        warehouseStockValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        warehouseStockValidation.beforeDelete(this)
    }

    def beforeValidate = {
        warehouseStockValidation.beforeValidate(this)
    }

    def afterInsert = {
        warehouseStockValidation.afterInsert(this)
    }

    def afterUpdate = {
        warehouseStockValidation.afterUpdate(this)
    }

    def afterDelete = {
        warehouseStockValidation.afterDelete(this)
    }

    def onLoad = {
        warehouseStockValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return warehouseStockRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}