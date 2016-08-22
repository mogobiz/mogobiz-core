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
class BOCartItem
    implements java.io.Serializable
{
    def BOCartItemValidation
    def BOCartItemRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     *
     */
    java.lang.String externalCode
    /**
     * 
     */
    java.lang.String code 
    /**
     * 
     */
    long price 
    /**
     * 
     */
    float tax 
    /**
     * 
     */
    long endPrice 
    /**
     * 
     */
    long totalPrice 
    /**
     * 
     */
    long totalEndPrice 
    /**
     * 
     */
    boolean hidden 
    /**
     * 
     */
    java.util.Calendar startDate 
    /**
     * 
     */
    java.util.Calendar endDate 
    /**
     * <p>
     * nombre de consommation nécessaire pour consommer totalement la
     * vente.<br/>
     * </p>
     * <p>
     * Correspond à la somme des quantité des types de tickets  (1 si
     * aucun type de ticket) multiplié par le nombre de suggestion
     * </p>
     */
    int quantity  = 1 
    /**
     * 
     */
    long ticketTypeFk 
    /**
     * 
     */
    java.lang.String url 
    /**
     * 
     */
    com.mogobiz.store.domain.BOCart bOCart 

    /**
     * 
     */
    com.mogobiz.store.domain.BODelivery bODelivery 

    static hasMany = [  bOProducts:com.mogobiz.store.domain.BOProduct ]

    static transients = [ 'BOCartItemValidation', 'BOCartItemRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_cart_item'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false
        tax column:"tax",insertable:true,updateable:true,lazy:false,cache:false
        endPrice column:"end_price",insertable:true,updateable:true,lazy:false,cache:false
        totalPrice column:"total_price",insertable:true,updateable:true,lazy:false,cache:false
        totalEndPrice column:"total_end_price",insertable:true,updateable:true,lazy:false,cache:false
        hidden column:"hidden",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        endDate column:"end_date",insertable:true,updateable:true,lazy:false,cache:false
        quantity column:"quantity",insertable:true,updateable:true,lazy:false,cache:false
        ticketTypeFk column:"ticket_type_fk",insertable:true,updateable:true,lazy:false,cache:false
        url column:"url",insertable:true,updateable:true,lazy:false,cache:false


        bOCart column:"b_o_cart_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        bOProducts column:"b_o_products_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        bODelivery column:"b_o_delivery_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        code ( blank:false, nullable:false, unique:true)
        price ( blank:false, nullable:false, unique:false)
        tax ( blank:false, nullable:false, unique:false)
        endPrice ( blank:false, nullable:false, unique:false)
        totalPrice ( blank:false, nullable:false, unique:false)
        totalEndPrice ( blank:false, nullable:false, unique:false)
        hidden ( blank:false, nullable:false, unique:false)
        startDate (nullable:true, unique:false)
        endDate (nullable:true, unique:false)
        quantity ( blank:false, nullable:false, unique:false)
        ticketTypeFk ( blank:false, nullable:false, unique:false)
        url (nullable:true, unique:false)
        bOCart ( blank:false, nullable:false)
        bODelivery (nullable:true)
    }


    String toString(){return BOCartItemRender?.asString(this)}

    def beforeInsert = {
        BOCartItemValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOCartItemValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOCartItemValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOCartItemValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOCartItemValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOCartItemValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOCartItemValidation.afterDelete(this)
    }

    def onLoad = {
        BOCartItemValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOCartItemRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}