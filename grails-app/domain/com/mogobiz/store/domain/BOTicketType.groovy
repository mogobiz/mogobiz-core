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
class BOTicketType
    implements java.io.Serializable
{
    def BOTicketTypeValidation
    def BOTicketTypeRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    int age 
    /**
     * 
     */
    int quantity  = 1 
    /**
     * 
     */
    long price 
    /**
     * 
     */
    java.lang.String ticketType 
    /**
     * 
     */
    java.lang.String qrcode 
    /**
     * 
     */
    java.lang.String qrcodeContent 
    /**
     * 
     */
    java.lang.String firstname 
    /**
     * 
     */
    java.lang.String lastname 
    /**
     * 
     */
    java.lang.String email 
    /**
     * 
     */
    java.lang.String phone 
    /**
     * 
     */
    java.util.Date birthdate 
    /**
     * 
     */
    java.lang.String shortCode 
    /**
     * 
     */
    java.util.Calendar startDate 
    /**
     * 
     */
    java.util.Calendar endDate 
    /**
     * 
     */
    com.mogobiz.store.domain.BOProduct bOProduct 

    static transients = [ 'BOTicketTypeValidation', 'BOTicketTypeRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_ticket_type'

        version false

        id name:'id',column:'id',generator:'native'
        age column:"age",insertable:true,updateable:true,lazy:false,cache:false
        quantity column:"quantity",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false
        ticketType column:"ticket_type",insertable:true,updateable:true,lazy:false,cache:false
        qrcode column:"qrcode",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        qrcodeContent column:"qrcode_content",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        firstname column:"firstname",insertable:true,updateable:true,lazy:false,cache:false
        lastname column:"lastname",insertable:true,updateable:true,lazy:false,cache:false
        email column:"email",insertable:true,updateable:true,lazy:false,cache:false
        phone column:"phone",insertable:true,updateable:true,lazy:false,cache:false
        birthdate column:"birthdate",insertable:true,updateable:true,lazy:false,cache:false
        shortCode column:"short_code",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        endDate column:"end_date",insertable:true,updateable:true,lazy:false,cache:false


        bOProduct column:"b_o_product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        age (nullable:true, unique:false)
        quantity ( blank:false, nullable:false, unique:false)
        price ( blank:false, nullable:false, unique:false)
        ticketType (nullable:true, unique:false)
        qrcode (nullable:true, unique:false)
        qrcodeContent (nullable:true, unique:false)
        firstname (nullable:true, unique:false)
        lastname (nullable:true, unique:false)
        email (nullable:true, unique:false)
        phone (nullable:true, unique:false)
        birthdate (nullable:true, unique:false)
        shortCode (nullable:true, unique:false)
        startDate (nullable:true, unique:false)
        endDate (nullable:true, unique:false)
        bOProduct ( blank:false, nullable:false)
    }

    static java.util.Collection findByBOProduct(final long idProduct)
    {
        return BOTicketType.findByBOProduct("FROM BOTicketType WHERE bOProduct.id = :idProduct", idProduct, 0, 0);
    }

    static java.util.Collection findByBOProduct(final long idProduct, int pageNumber, int pageSize)
    {
        return BOTicketType.findByBOProduct("FROM BOTicketType WHERE bOProduct.id = :idProduct", idProduct, pageNumber, pageSize);
    }

    static java.util.Collection findByBOProduct(final java.lang.String queryString, final long idProduct, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? BOTicketType.executeQuery(queryString, [idProduct:idProduct], paginateParams) : BOTicketType.executeQuery(queryString, [idProduct:idProduct])
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return BOTicketTypeRender?.asString(this)}

    def beforeInsert = {
        BOTicketTypeValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOTicketTypeValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOTicketTypeValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOTicketTypeValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOTicketTypeValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOTicketTypeValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOTicketTypeValidation.afterDelete(this)
    }

    def onLoad = {
        BOTicketTypeValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOTicketTypeRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}