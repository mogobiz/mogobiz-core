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
class ShippingRule
    implements java.io.Serializable
{
    def shippingRuleValidation
    def shippingRuleRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String countryCode 
    /**
     * 
     */
    long minAmount 
    /**
     * 
     */
    long maxAmount 
    /**
     * 
     */
    java.lang.String price 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'shippingRuleValidation', 'shippingRuleRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'shipping_rule'

        version false

        id name:'id',column:'id',generator:'native'
        countryCode column:"country_code",insertable:true,updateable:true,lazy:false,cache:false
        minAmount column:"min_amount",insertable:true,updateable:true,lazy:false,cache:false
        maxAmount column:"max_amount",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        countryCode ( blank:false, nullable:false, unique:false)
        minAmount ( blank:false, nullable:false, unique:false)
        maxAmount ( blank:false, nullable:false, unique:false)
        price ( blank:false, nullable:false, unique:false)
        company (nullable:true)
    }


    String toString(){return shippingRuleRender?.asString(this)}

    def beforeInsert = {
        shippingRuleValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        shippingRuleValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        shippingRuleValidation.beforeDelete(this)
    }

    def beforeValidate = {
        shippingRuleValidation.beforeValidate(this)
    }

    def afterInsert = {
        shippingRuleValidation.afterInsert(this)
    }

    def afterUpdate = {
        shippingRuleValidation.afterUpdate(this)
    }

    def afterDelete = {
        shippingRuleValidation.afterDelete(this)
    }

    def onLoad = {
        shippingRuleValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return shippingRuleRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}