/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEntity.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class BOReturn
    implements java.io.Serializable
{
    def BOReturnValidation
    def BOReturnRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String motivation 
    /**
     * 
     */
    com.mogobiz.store.domain.ReturnStatus status 
    /**
     * 
     */
    com.mogobiz.store.domain.BOReturnedItem bOReturnedItem 

    static transients = [ 'BOReturnValidation', 'BOReturnRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_return'

        version false

        id name:'id',column:'id',generator:'native'
        motivation column:"motivation",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        status column:"status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false


        bOReturnedItem column:"b_o_returned_item_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        motivation (nullable:true, unique:false)
        status ( blank:false, nullable:false, unique:false)
        bOReturnedItem (nullable:true)
    }


    String toString(){return BOReturnRender?.asString(this)}

    def beforeInsert = {
        BOReturnValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOReturnValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOReturnValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOReturnValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOReturnValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOReturnValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOReturnValidation.afterDelete(this)
    }

    def onLoad = {
        BOReturnValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOReturnRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}