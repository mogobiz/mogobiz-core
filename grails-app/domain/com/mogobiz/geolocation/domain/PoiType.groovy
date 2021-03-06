/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.geolocation.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class PoiType
    implements java.io.Serializable
{
    def poiTypeValidation
    def poiTypeRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String xtype 
    /**
     * 
     */
    java.lang.String code 
    /**
     * 
     */
    com.mogobiz.store.domain.Resource icon 

    static transients = [ 'poiTypeValidation', 'poiTypeRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'poi_type'

        version false

        id name:'id',column:'id',generator:'native'
        xtype column:"xtype",insertable:true,updateable:true,lazy:false,cache:false
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false


        icon column:"icon_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        xtype ( blank:false, nullable:false, unique:true)
        code ( blank:false, nullable:false, unique:false)
        icon (nullable:true)
    }


    String toString(){return poiTypeRender?.asString(this)}

    def beforeInsert = {
        poiTypeValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        poiTypeValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        poiTypeValidation.beforeDelete(this)
    }

    def beforeValidate = {
        poiTypeValidation.beforeValidate(this)
    }

    def afterInsert = {
        poiTypeValidation.afterInsert(this)
    }

    def afterUpdate = {
        poiTypeValidation.afterUpdate(this)
    }

    def afterDelete = {
        poiTypeValidation.afterDelete(this)
    }

    def onLoad = {
        poiTypeValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return poiTypeRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}