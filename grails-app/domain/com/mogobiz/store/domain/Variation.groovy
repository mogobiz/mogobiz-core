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
class Variation
    implements java.io.Serializable
{
    def variationValidation
    def variationRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String externalCode 
    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    int position 
    /**
     * 
     */
    Boolean hide  = false 
    /**
     * 
     */
    java.lang.String googleVariationType 
    /**
     * 
     */
    java.lang.String i18n 
    /**
     * 
     */
    com.mogobiz.store.domain.Category category 

    static hasMany = [  variationValues:com.mogobiz.store.domain.VariationValue ]

    static transients = [ 'variationValidation', 'variationRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'variation'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false
        hide column:"hide",insertable:true,updateable:true,lazy:false,cache:false
        googleVariationType column:"google_variation_type",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        category column:"category_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        variationValues column:"variation_values_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        name ( blank:false, nullable:false, unique:false)
        position ( blank:false, nullable:false, unique:false)
        hide ( blank:false, nullable:false, unique:false)
        googleVariationType (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        category (nullable:true)
    }


    String toString(){return variationRender?.asString(this)}

    def beforeInsert = {
        variationValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        variationValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        variationValidation.beforeDelete(this)
    }

    def beforeValidate = {
        variationValidation.beforeValidate(this)
    }

    def afterInsert = {
        variationValidation.afterInsert(this)
    }

    def afterUpdate = {
        variationValidation.afterUpdate(this)
    }

    def afterDelete = {
        variationValidation.afterDelete(this)
    }

    def onLoad = {
        variationValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return variationRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}