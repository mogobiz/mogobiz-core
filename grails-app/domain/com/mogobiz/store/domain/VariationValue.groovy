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
class VariationValue
    implements java.io.Serializable
{
    def variationValueValidation
    def variationValueRender

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
    java.lang.String value 
    /**
     * 
     */
    int position 
    /**
     * 
     */
    java.lang.String googleVariationValue 
    /**
     * 
     */
    java.lang.String i18n 
    /**
     * 
     */
    com.mogobiz.store.domain.Variation variation 

    static transients = [ 'variationValueValidation', 'variationValueRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'variation_value'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false
        googleVariationValue column:"google_variation_value",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        variation column:"variation_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        value ( blank:false, nullable:false, unique:false)
        position ( blank:false, nullable:false, unique:false)
        googleVariationValue (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        variation ( blank:false, nullable:false)
    }


    String toString(){return variationValueRender?.asString(this)}

    def beforeInsert = {
        variationValueValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        variationValueValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        variationValueValidation.beforeDelete(this)
    }

    def beforeValidate = {
        variationValueValidation.beforeValidate(this)
    }

    def afterInsert = {
        variationValueValidation.afterInsert(this)
    }

    def afterUpdate = {
        variationValueValidation.afterUpdate(this)
    }

    def afterDelete = {
        variationValueValidation.afterDelete(this)
    }

    def onLoad = {
        variationValueValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return variationValueRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}