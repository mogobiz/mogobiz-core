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
class FeatureValue
    implements java.io.Serializable
{
    def featureValueValidation
    def featureValueRender

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
    com.mogobiz.store.domain.Feature feature

    /**
     *
     */
    com.mogobiz.store.domain.Product product

    static transients = [ 'featureValueValidation', 'featureValueRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'feature_value'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false


        feature column:"feature_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        value (nullable:true, unique:false)
        feature ( blank:false, nullable:false)
        product ( blank:false, nullable:false)
    }


    String toString(){return featureValueRender?.asString(this)}

    def beforeInsert = {
        featureValueValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        featureValueValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        featureValueValidation.beforeDelete(this)
    }

    def beforeValidate = {
        featureValueValidation.beforeValidate(this)
    }

    def afterInsert = {
        featureValueValidation.afterInsert(this)
    }

    def afterUpdate = {
        featureValueValidation.afterUpdate(this)
    }

    def afterDelete = {
        featureValueValidation.afterDelete(this)
    }

    def onLoad = {
        featureValueValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return featureValueRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
