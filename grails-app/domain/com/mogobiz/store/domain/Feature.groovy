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
class Feature
    implements java.io.Serializable
{
    def featureValidation
    def featureRender

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
    java.lang.String domain
    /**
     *
     */
    Boolean hide  = false
    /**
     *
     */
    java.lang.String value
    /**
     *
     */
    java.lang.String i18n
    /**
     *
     */
    com.mogobiz.store.domain.Product product

    /**
     *
     */
    com.mogobiz.store.domain.Category category

    static transients = [ 'featureValidation', 'featureRender' ]

    static hasMany = [values: FeatureValue]

    static mappedBy = [values: "feature"]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'feature'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false
        domain column:"domain",insertable:true,updateable:true,lazy:false,cache:false
        hide column:"hide",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        category column:"category_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        name ( blank:false, nullable:false, unique:false)
        position ( blank:false, nullable:false, unique:false)
        domain (nullable:true, unique:false)
        hide ( blank:false, nullable:false, unique:false)
        value (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        product (nullable:true)
        category (nullable:true)
    }


    String toString(){return featureRender?.asString(this)}

    def beforeInsert = {
        featureValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        featureValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        featureValidation.beforeDelete(this)
    }

    def beforeValidate = {
        featureValidation.beforeValidate(this)
    }

    def afterInsert = {
        featureValidation.afterInsert(this)
    }

    def afterUpdate = {
        featureValidation.afterUpdate(this)
    }

    def afterDelete = {
        featureValidation.afterDelete(this)
    }

    def onLoad = {
        featureValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return featureRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
