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
class EsSync
    implements java.io.Serializable
{
    def esSyncValidation
    def esSyncRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.util.Date timestamp 
    /**
     * 
     */
    java.lang.Boolean success 
    /**
     * 
     */
    java.lang.String report 
    /**
     * 
     */
    com.mogobiz.store.domain.EsEnv esEnv 

    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    /**
     * 
     */
    com.mogobiz.store.domain.Catalog target 

    static hasMany = [ catalogs:com.mogobiz.store.domain.Catalog , categories:com.mogobiz.store.domain.Category , products:com.mogobiz.store.domain.Product ]

    static transients = [ 'esSyncValidation', 'esSyncRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'es_sync'

        version false

        id name:'id',column:'id',generator:'native'
        timestamp column:"timestamp",insertable:true,updateable:true,lazy:false,cache:false
        success column:"success",insertable:true,updateable:true,lazy:false,cache:false
        report column:"report",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        esEnv column:"es_env_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        catalogs column:"catalogs_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        categories column:"categories_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        products column:"products_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        target column:"target_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        timestamp (nullable:true, unique:false)
        success (nullable:true, unique:false)
        report (nullable:true, unique:false)
        esEnv ( blank:false, nullable:false)
        company ( blank:false, nullable:false)
        target ( blank:false, nullable:false)
    }


    String toString(){return esSyncRender?.asString(this)}

    def beforeInsert = {
        esSyncValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        esSyncValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        esSyncValidation.beforeDelete(this)
    }

    def beforeValidate = {
        esSyncValidation.beforeValidate(this)
    }

    def afterInsert = {
        esSyncValidation.afterInsert(this)
    }

    def afterUpdate = {
        esSyncValidation.afterUpdate(this)
    }

    def afterDelete = {
        esSyncValidation.afterDelete(this)
    }

    def onLoad = {
        esSyncValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return esSyncRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}