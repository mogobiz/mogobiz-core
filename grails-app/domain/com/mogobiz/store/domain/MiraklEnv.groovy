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
class MiraklEnv
    implements java.io.Serializable
{
    def miraklEnvValidation
    def miraklEnvRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String url 
    /**
     * 
     */
    java.lang.String apiKey 
    /**
     * 
     */
    java.lang.String shopId 
    /**
     * 
     */
    boolean running  = false 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'miraklEnvValidation', 'miraklEnvRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'mirakl_env'

        version false

        id name:'id',column:'id',generator:'native'
        url column:"url",insertable:true,updateable:true,lazy:false,cache:false
        apiKey column:"api_key",insertable:true,updateable:true,lazy:false,cache:false
        shopId column:"shop_id",insertable:true,updateable:true,lazy:false,cache:false
        running column:"running",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        url ( blank:false, nullable:false, unique:false)
        apiKey ( blank:false, nullable:false, unique:false)
        shopId ( blank:false, nullable:false, unique:false)
        running ( blank:false, nullable:false, unique:false)
        company ( blank:false, nullable:false)
    }


    String toString(){return miraklEnvRender?.asString(this)}

    def beforeInsert = {
        miraklEnvValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        miraklEnvValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        miraklEnvValidation.beforeDelete(this)
    }

    def beforeValidate = {
        miraklEnvValidation.beforeValidate(this)
    }

    def afterInsert = {
        miraklEnvValidation.afterInsert(this)
    }

    def afterUpdate = {
        miraklEnvValidation.afterUpdate(this)
    }

    def afterDelete = {
        miraklEnvValidation.afterDelete(this)
    }

    def onLoad = {
        miraklEnvValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return miraklEnvRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}