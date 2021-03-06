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
class GoogleVariationType
    implements java.io.Serializable
{
    def googleVariationTypeValidation
    def googleVariationTypeRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String xtype 
    static transients = [ 'googleVariationTypeValidation', 'googleVariationTypeRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'google_variation_type'

        version false

        id name:'id',column:'id',generator:'native'
        xtype column:"xtype",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        xtype ( blank:false, nullable:false, unique:true)
    }


    String toString(){return googleVariationTypeRender?.asString(this)}

    def beforeInsert = {
        googleVariationTypeValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        googleVariationTypeValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        googleVariationTypeValidation.beforeDelete(this)
    }

    def beforeValidate = {
        googleVariationTypeValidation.beforeValidate(this)
    }

    def afterInsert = {
        googleVariationTypeValidation.afterInsert(this)
    }

    def afterUpdate = {
        googleVariationTypeValidation.afterUpdate(this)
    }

    def afterDelete = {
        googleVariationTypeValidation.afterDelete(this)
    }

    def onLoad = {
        googleVariationTypeValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return googleVariationTypeRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}