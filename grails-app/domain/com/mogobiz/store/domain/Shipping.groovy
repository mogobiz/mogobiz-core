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
class Shipping
    implements java.io.Serializable
{
    def shippingValidation
    def shippingRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    long weight 
    /**
     * 
     */
    com.mogobiz.store.domain.WeightUnit weightUnit 
    /**
     * 
     */
    long width 
    /**
     * 
     */
    long height 
    /**
     * 
     */
    java.lang.Long depth 
    /**
     * 
     */
    com.mogobiz.store.domain.LinearUnit linearUnit 
    /**
     * 
     */
    long amount 
    /**
     * 
     */
    boolean free  = false 
    static transients = [ 'shippingValidation', 'shippingRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'shipping'

        version false

        id name:'id',column:'id',generator:'native'
        weight column:"weight",insertable:true,updateable:true,lazy:false,cache:false
        weightUnit column:"weight_unit",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        width column:"width",insertable:true,updateable:true,lazy:false,cache:false
        height column:"height",insertable:true,updateable:true,lazy:false,cache:false
        depth column:"depth",insertable:true,updateable:true,lazy:false,cache:false
        linearUnit column:"linear_unit",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        amount column:"amount",insertable:true,updateable:true,lazy:false,cache:false
        free column:"free",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        weight ( blank:false, nullable:false, unique:false)
        weightUnit ( blank:false, nullable:false, unique:false)
        width ( blank:false, nullable:false, unique:false)
        height ( blank:false, nullable:false, unique:false)
        depth ( blank:false, nullable:false, unique:false)
        linearUnit ( blank:false, nullable:false, unique:false)
        amount ( blank:false, nullable:false, unique:false)
        free ( blank:false, nullable:false, unique:false)
    }


    String toString(){return shippingRender?.asString(this)}

    def beforeInsert = {
        shippingValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        shippingValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        shippingValidation.beforeDelete(this)
    }

    def beforeValidate = {
        shippingValidation.beforeValidate(this)
    }

    def afterInsert = {
        shippingValidation.afterInsert(this)
    }

    def afterUpdate = {
        shippingValidation.afterUpdate(this)
    }

    def afterDelete = {
        shippingValidation.afterDelete(this)
    }

    def onLoad = {
        shippingValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return shippingRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}