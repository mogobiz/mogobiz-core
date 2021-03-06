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
class GoogleVariationValue
    implements java.io.Serializable
{
    def googleVariationValueValidation
    def googleVariationValueRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String value 
    /**
     * 
     */
    com.mogobiz.store.domain.GoogleVariationType type 

    static transients = [ 'googleVariationValueValidation', 'googleVariationValueRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'google_variation_value'

        version false

        id name:'id',column:'id',generator:'native'
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false


        type column:"type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        value ( blank:false, nullable:false, unique:true)
        type ( blank:false, nullable:false)
    }


    String toString(){return googleVariationValueRender?.asString(this)}

    def beforeInsert = {
        googleVariationValueValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        googleVariationValueValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        googleVariationValueValidation.beforeDelete(this)
    }

    def beforeValidate = {
        googleVariationValueValidation.beforeValidate(this)
    }

    def afterInsert = {
        googleVariationValueValidation.afterInsert(this)
    }

    def afterUpdate = {
        googleVariationValueValidation.afterUpdate(this)
    }

    def afterDelete = {
        googleVariationValueValidation.afterDelete(this)
    }

    def onLoad = {
        googleVariationValueValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return googleVariationValueRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}