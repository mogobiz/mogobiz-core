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
class Seller
    extends com.mogobiz.store.domain.User
    implements java.io.Serializable
{
    def sellerValidation
    def sellerRender






    /**
     * 
     */
    boolean admin  = false 
    /**
     * 
     */
    boolean validator  = false 
    /**
     * 
     */
    boolean sell  = false 
    /**
     * 
     */
    boolean agent  = false 
    static hasMany = [ companies:com.mogobiz.store.domain.Company ]

    static transients = [ 'sellerValidation', 'sellerRender' ]


    static mapping = {







        cache usage:'read-write'

        tablePerHierarchy false

        table 'seller'

        version false

        id name:'id',column:'id',generator:'native'
        admin column:"admin",insertable:true,updateable:true,lazy:false,cache:false
        validator column:"validator",insertable:true,updateable:true,lazy:false,cache:false
        sell column:"sell",insertable:true,updateable:true,lazy:false,cache:false
        agent column:"agent",insertable:true,updateable:true,lazy:false,cache:false

        companies column:"companies_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {


        admin ( blank:false, nullable:false, unique:false)
        validator ( blank:false, nullable:false, unique:false)
        sell ( blank:false, nullable:false, unique:false)
        agent ( blank:false, nullable:false, unique:false)
    }


    String toString(){return sellerRender?.asString(this)}

    def beforeInsert = {
        sellerValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        sellerValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        sellerValidation.beforeDelete(this)
    }

    def beforeValidate = {
        sellerValidation.beforeValidate(this)
    }

    def afterInsert = {
        sellerValidation.afterInsert(this)
    }

    def afterUpdate = {
        sellerValidation.afterUpdate(this)
    }

    def afterDelete = {
        sellerValidation.afterDelete(this)
    }

    def onLoad = {
        sellerValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return sellerRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}