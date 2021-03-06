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
class GoogleEnv
    implements java.io.Serializable
{
    def googleEnvValidation
    def googleEnvRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String merchant_id 
    /**
     * 
     */
    java.lang.String merchant_url 
    /**
     * 
     */
    java.lang.String client_token 
    /**
     * 
     */
    java.lang.String client_id 
    /**
     * 
     */
    java.lang.String client_secret 
    /**
     * 
     */
    java.lang.String cronExpr 
    /**
     * 
     */
    boolean running  = false 
    /**
     * 
     */
    java.lang.String extra 
    /**
     * 
     */
    boolean dry_run  = false 
    /**
     * 
     */
    boolean active  = false 
    /**
     * 
     */
    int version  = 2 
    static transients = [ 'googleEnvValidation', 'googleEnvRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'google_env'

        version false

        id name:'id',column:'id',generator:'native'
        merchant_id column:"merchant_id",insertable:true,updateable:true,lazy:false,cache:false
        merchant_url column:"merchant_url",insertable:true,updateable:true,lazy:false,cache:false
        client_token column:"client_token",insertable:true,updateable:true,lazy:false,cache:false
        client_id column:"client_id",insertable:true,updateable:true,lazy:false,cache:false
        client_secret column:"client_secret",insertable:true,updateable:true,lazy:false,cache:false
        cronExpr column:"cron_expr",insertable:true,updateable:true,lazy:false,cache:false
        running column:"running",insertable:true,updateable:true,lazy:false,cache:false
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        dry_run column:"dry_run",insertable:true,updateable:true,lazy:false,cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        version column:"version",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        merchant_id ( blank:false, nullable:false, unique:false)
        merchant_url (nullable:true, unique:false, url:true)
        client_token (nullable:true, unique:false)
        client_id (nullable:true, unique:false)
        client_secret (nullable:true, unique:false)
        cronExpr ( blank:false, nullable:false, unique:false)
        running ( blank:false, nullable:false, unique:false)
        extra (nullable:true, unique:false)
        dry_run ( blank:false, nullable:false, unique:false)
        active ( blank:false, nullable:false, unique:false)
        version ( blank:false, nullable:false, unique:false)
    }


    String toString(){return googleEnvRender?.asString(this)}

    def beforeInsert = {
        googleEnvValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        googleEnvValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        googleEnvValidation.beforeDelete(this)
    }

    def beforeValidate = {
        googleEnvValidation.beforeValidate(this)
    }

    def afterInsert = {
        googleEnvValidation.afterInsert(this)
    }

    def afterUpdate = {
        googleEnvValidation.afterUpdate(this)
    }

    def afterDelete = {
        googleEnvValidation.afterDelete(this)
    }

    def onLoad = {
        googleEnvValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return googleEnvRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}