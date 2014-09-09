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
class UuidData
    implements java.io.Serializable
{
    def uuidDataValidation
    def uuidDataRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String payload 
    /**
     * 
     */
    java.lang.String xtype 
    /**
     * <p>
     * Expire date of the UuidData. ExpireDate is use by the Job to
     * test if the Uuid must be delete
     * </p>
     */
    java.util.Calendar expireDate 
    static transients = [ 'uuidDataValidation', 'uuidDataRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false


        cache usage:'read-write'

        tablePerHierarchy false

        table 'uuid_data'

        version false

        id name:'id',column:'id',generator:'native'
        payload column:"payload",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        xtype column:"xtype",insertable:true,updateable:true,lazy:false,cache:false
        expireDate column:"expire_date",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        payload (nullable:true, unique:false)
        xtype ( blank:false, nullable:false, unique:false)
        expireDate ( blank:false, nullable:false, unique:false)
    }


    String toString(){return uuidDataRender?.asString(this)}

    def beforeInsert = {
        uuidDataValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        uuidDataValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        uuidDataValidation.beforeDelete(this)
    }

    def beforeValidate = {
        uuidDataValidation.beforeValidate(this)
    }

    def afterInsert = {
        uuidDataValidation.afterInsert(this)
    }

    def afterUpdate = {
        uuidDataValidation.afterUpdate(this)
    }

    def afterDelete = {
        uuidDataValidation.afterDelete(this)
    }

    def onLoad = {
        uuidDataValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return uuidDataRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}