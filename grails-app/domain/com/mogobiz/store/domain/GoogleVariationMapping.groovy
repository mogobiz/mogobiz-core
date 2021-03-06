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
class GoogleVariationMapping
    implements java.io.Serializable
{
    def googleVariationMappingValidation
    def googleVariationMappingRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String mappings 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    /**
     * 
     */
    com.mogobiz.store.domain.GoogleVariationValue value 

    /**
     * 
     */
    com.mogobiz.store.domain.GoogleVariationType type 

    static transients = [ 'googleVariationMappingValidation', 'googleVariationMappingRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'google_variation_mapping'

        version false

        id name:'id',column:'id',generator:'native'
        mappings column:"mappings",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        value column:"value_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        type column:"type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        mappings ( blank:false, nullable:false, unique:false)
        company ( blank:false, nullable:false)
        value (nullable:true)
        type (nullable:true)
    }


    String toString(){return googleVariationMappingRender?.asString(this)}

    def beforeInsert = {
        googleVariationMappingValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        googleVariationMappingValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        googleVariationMappingValidation.beforeDelete(this)
    }

    def beforeValidate = {
        googleVariationMappingValidation.beforeValidate(this)
    }

    def afterInsert = {
        googleVariationMappingValidation.afterInsert(this)
    }

    def afterUpdate = {
        googleVariationMappingValidation.afterUpdate(this)
    }

    def afterDelete = {
        googleVariationMappingValidation.afterDelete(this)
    }

    def onLoad = {
        googleVariationMappingValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return googleVariationMappingRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}