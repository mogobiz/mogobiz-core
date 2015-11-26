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
class Consumption
    implements java.io.Serializable
{
    def consumptionValidation
    def consumptionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.util.Calendar date 
    /**
     * 
     */
    com.mogobiz.store.domain.BOTicketType bOTicketType 

    static transients = [ 'consumptionValidation', 'consumptionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'consumption'

        version false

        id name:'id',column:'id',generator:'native'
        date column:"xdate",insertable:true,updateable:true,lazy:false,cache:false


        bOTicketType column:"b_o_ticket_type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        date ( blank:false, nullable:false, unique:false)
        bOTicketType (nullable:true)
    }

    static java.util.Collection findBySale(final long idSale)
    {
        return Consumption.findBySale("SELECT consomation FROM Sale AS sale JOIN sale.bOProducts AS boProduct JOIN boProduct.consumptions AS consomation WHERE sale.id = :idSale", idSale, 0, 0);
    }

    static java.util.Collection findBySale(final long idSale, int pageNumber, int pageSize)
    {
        return Consumption.findBySale("SELECT consomation FROM Sale AS sale JOIN sale.bOProducts AS boProduct JOIN boProduct.consumptions AS consomation WHERE sale.id = :idSale", idSale, pageNumber, pageSize);
    }

    static java.util.Collection findBySale(final java.lang.String queryString, final long idSale, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? Consumption.executeQuery(queryString, [idSale:idSale], paginateParams) : Consumption.executeQuery(queryString, [idSale:idSale])
    }
    static java.util.Collection findBySaleAndProduct(final long idSale, final long idProduct)
    {
        return Consumption.findBySaleAndProduct("SELECT consomation FROM Sale AS sale JOIN sale.bOProducts AS boProduct JOIN boProduct.consumptions AS consomation JOIN boProduct.product AS produit WHERE sale.id = :idSale and produit.id = :idProduct", idSale, idProduct, 0, 0);
    }

    static java.util.Collection findBySaleAndProduct(final long idSale, final long idProduct, int pageNumber, int pageSize)
    {
        return Consumption.findBySaleAndProduct("SELECT consomation FROM Sale AS sale JOIN sale.bOProducts AS boProduct JOIN boProduct.consumptions AS consomation JOIN boProduct.product AS produit WHERE sale.id = :idSale and produit.id = :idProduct", idSale, idProduct, pageNumber, pageSize);
    }

    static java.util.Collection findBySaleAndProduct(final java.lang.String queryString, final long idSale, final long idProduct, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? Consumption.executeQuery(queryString, [idSale:idSale,idProduct:idProduct], paginateParams) : Consumption.executeQuery(queryString, [idSale:idSale,idProduct:idProduct])
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return consumptionRender?.asString(this)}

    def beforeInsert = {
        consumptionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        consumptionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        consumptionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        consumptionValidation.beforeValidate(this)
    }

    def afterInsert = {
        consumptionValidation.afterInsert(this)
    }

    def afterUpdate = {
        consumptionValidation.afterUpdate(this)
    }

    def afterDelete = {
        consumptionValidation.afterDelete(this)
    }

    def onLoad = {
        consumptionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return consumptionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}