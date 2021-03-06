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
class Warehouse
    implements java.io.Serializable
{
    def warehouseValidation
    def warehouseRender

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
    java.lang.String externalCode 
    /**
     * 
     */
    long pickDelayInMinutes 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 
    /**
     * 
     */
    com.mogobiz.geolocation.domain.Location shipFrom

    static transients = [ 'warehouseValidation', 'warehouseRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'warehouse'

        version false

        id name:'id',column:'id',generator:'native'
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        pickDelayInMinutes column:"pick_delay_in_minutes",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        shipFrom column:"ship_from_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        code ( blank:false, nullable:false, unique:false)
        name ( blank:false, nullable:false, unique:false)
        externalCode (nullable:true, unique:false)
        pickDelayInMinutes ( blank:false, nullable:false, unique:false)
        company (nullable:true)
        shipFrom (nullable:true)
    }


    String toString(){return warehouseRender?.asString(this)}

    def beforeInsert = {
        warehouseValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        warehouseValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        warehouseValidation.beforeDelete(this)
    }

    def beforeValidate = {
        warehouseValidation.beforeValidate(this)
    }

    def afterInsert = {
        warehouseValidation.afterInsert(this)
    }

    def afterUpdate = {
        warehouseValidation.afterUpdate(this)
    }

    def afterDelete = {
        warehouseValidation.afterDelete(this)
    }

    def onLoad = {
        warehouseValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return warehouseRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}