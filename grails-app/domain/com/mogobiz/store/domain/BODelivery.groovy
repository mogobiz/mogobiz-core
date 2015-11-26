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
class BODelivery
    implements java.io.Serializable
{
    def BODeliveryValidation
    def BODeliveryRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String tracking 
    /**
     * 
     */
    com.mogobiz.store.domain.DeliveryStatus status 
    /**
     * 
     */
    java.lang.String extra 
    /**
     * 
     */
    com.mogobiz.store.domain.BOCart bOCart 

    static transients = [ 'BODeliveryValidation', 'BODeliveryRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_delivery'

        version false

        id name:'id',column:'id',generator:'native'
        tracking column:"tracking",insertable:true,updateable:true,lazy:false,cache:false
        status column:"status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        bOCart column:"b_o_cart_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        tracking (nullable:true, unique:false)
        status ( blank:false, nullable:false, unique:false)
        extra (nullable:true, unique:false)
        bOCart ( blank:false, nullable:false)
    }


    String toString(){return BODeliveryRender?.asString(this)}

    def beforeInsert = {
        BODeliveryValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BODeliveryValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BODeliveryValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BODeliveryValidation.beforeValidate(this)
    }

    def afterInsert = {
        BODeliveryValidation.afterInsert(this)
    }

    def afterUpdate = {
        BODeliveryValidation.afterUpdate(this)
    }

    def afterDelete = {
        BODeliveryValidation.afterDelete(this)
    }

    def onLoad = {
        BODeliveryValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BODeliveryRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}