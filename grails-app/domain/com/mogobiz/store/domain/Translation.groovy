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
class Translation
    implements java.io.Serializable
{
    def translationValidation
    def translationRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    long target 
    /**
     * 
     */
    java.lang.String lang 
    /**
     * 
     */
    java.lang.String value 
    /**
     * 
     */
    java.lang.String type 
    /**
     * 
     */
    long companyId 
    static transients = [ 'translationValidation', 'translationRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'xtranslation'

        version false

        id name:'id',column:'id',generator:'native'
        target column:"target",insertable:true,updateable:true,lazy:false,cache:false
        lang column:"lang",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        type column:"type",insertable:true,updateable:true,lazy:false,cache:false
        companyId column:"company_id",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        target ( blank:false, nullable:false, unique:false)
        lang ( blank:false, nullable:false, unique:false)
        value ( blank:false, nullable:false, unique:false)
        type (nullable:true, unique:false)
        companyId ( blank:false, nullable:false, unique:false)
    }

    String toString(){return translationRender?.asString(this)}

    def beforeInsert = {
        translationValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        translationValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        translationValidation.beforeDelete(this)
    }

    def beforeValidate = {
        translationValidation.beforeValidate(this)
    }

    def afterInsert = {
        translationValidation.afterInsert(this)
    }

    def afterUpdate = {
        translationValidation.afterUpdate(this)
    }

    def afterDelete = {
        translationValidation.afterDelete(this)
    }

    def onLoad = {
        translationValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return translationRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}