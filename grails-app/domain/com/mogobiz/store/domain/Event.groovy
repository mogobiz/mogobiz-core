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
class Event
    implements java.io.Serializable
{
    def eventValidation
    def eventRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    com.mogobiz.store.domain.EventType xtype 
    /**
     * 
     */
    java.util.Calendar date 
    /**
     * 
     */
    java.lang.String description 
    /**
     * 
     */
    com.mogobiz.store.domain.User user 

    /**
     * 
     */
    com.mogobiz.store.domain.Resource resource 

    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    static transients = [ 'eventValidation', 'eventRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'event'

        version false

        id name:'id',column:'id',generator:'native'
        xtype column:"xtype",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        date column:"xdate",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        user column:"user_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        resource column:"resource_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        xtype ( blank:false, nullable:false, unique:false)
        date ( blank:false, nullable:false, unique:false)
        description (nullable:true, unique:false)
        user ( blank:false, nullable:false)
        resource (nullable:true)
        product (nullable:true)
    }


    String toString(){return eventRender?.asString(this)}

    def beforeInsert = {
        eventValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        eventValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        eventValidation.beforeDelete(this)
    }

    def beforeValidate = {
        eventValidation.beforeValidate(this)
    }

    def afterInsert = {
        eventValidation.afterInsert(this)
    }

    def afterUpdate = {
        eventValidation.afterUpdate(this)
    }

    def afterDelete = {
        eventValidation.afterDelete(this)
    }

    def onLoad = {
        eventValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return eventRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}