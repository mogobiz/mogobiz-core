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
class CustomerProfile
    implements java.io.Serializable
{
    def customerProfileValidation
    def customerProfileRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String code 
    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    java.lang.String description 
    /**
     * 
     */
    boolean active  = true 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'customerProfileValidation', 'customerProfileRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'customer_profile'

        version false

        id name:'id',column:'id',generator:'native'
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        code ( blank:false, nullable:false, unique:false)
        name ( blank:false, nullable:false, unique:false)
        description ( blank:false, nullable:false, unique:false)
        active ( blank:false, nullable:false, unique:false)
        company ( blank:false, nullable:false)
    }


    String toString(){return customerProfileRender?.asString(this)}

    def beforeInsert = {
        customerProfileValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        customerProfileValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        customerProfileValidation.beforeDelete(this)
    }

    def beforeValidate = {
        customerProfileValidation.beforeValidate(this)
    }

    def afterInsert = {
        customerProfileValidation.afterInsert(this)
    }

    def afterUpdate = {
        customerProfileValidation.afterUpdate(this)
    }

    def afterDelete = {
        customerProfileValidation.afterDelete(this)
    }

    def onLoad = {
        customerProfileValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return customerProfileRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}