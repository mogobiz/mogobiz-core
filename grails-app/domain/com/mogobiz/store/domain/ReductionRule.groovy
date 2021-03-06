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
class ReductionRule
    implements java.io.Serializable
{
    def reductionRuleValidation
    def reductionRuleRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    com.mogobiz.store.domain.ReductionRuleType xtype 
    /**
     * 
     */
    java.lang.Long quantityMin 
    /**
     * 
     */
    java.lang.Long quantityMax 
    /**
     * <p>
     * discount (or percent) if type is DISCOUNT (example : -1000 or
     * 10%)
     * </p>
     */
    java.lang.String discount 
    /**
     * 
     */
    java.lang.Long xPurchased 
    /**
     * 
     */
    java.lang.Long yOffered 
    static transients = [ 'reductionRuleValidation', 'reductionRuleRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'reduction_rule'

        version false

        id name:'id',column:'id',generator:'native'
        xtype column:"xtype",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        quantityMin column:"quantity_min",insertable:true,updateable:true,lazy:false,cache:false
        quantityMax column:"quantity_max",insertable:true,updateable:true,lazy:false,cache:false
        discount column:"discount",insertable:true,updateable:true,lazy:false,cache:false
        xPurchased column:"x_purchased",insertable:true,updateable:true,lazy:false,cache:false
        yOffered column:"y_offered",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        xtype ( blank:false, nullable:false, unique:false)
        quantityMin (nullable:true, unique:false)
        quantityMax (nullable:true, unique:false)
        discount (nullable:true, unique:false)
        xPurchased (nullable:true, unique:false)
        yOffered (nullable:true, unique:false)
    }


    String toString(){return reductionRuleRender?.asString(this)}

    def beforeInsert = {
        reductionRuleValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        reductionRuleValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        reductionRuleValidation.beforeDelete(this)
    }

    def beforeValidate = {
        reductionRuleValidation.beforeValidate(this)
    }

    def afterInsert = {
        reductionRuleValidation.afterInsert(this)
    }

    def afterUpdate = {
        reductionRuleValidation.afterUpdate(this)
    }

    def afterDelete = {
        reductionRuleValidation.afterDelete(this)
    }

    def onLoad = {
        reductionRuleValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return reductionRuleRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}