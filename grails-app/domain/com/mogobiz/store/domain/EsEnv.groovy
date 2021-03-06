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
class EsEnv
    implements java.io.Serializable
{
    def esEnvValidation
    def esEnvRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     *
     */
    java.lang.String name
    /**
     *
     */
    java.lang.String url
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
    boolean active  = false
    /**
     * <p>
     * Version de l'export ES
     * </p>
     */
    java.lang.String idx
    /**
     *
     */
    boolean success  = true
    /**
     *
     */
    java.lang.String activeIndex
    /**
     *
     */
    com.mogobiz.store.domain.Company company

    java.lang.String cacheUrls

    static transients = [ 'esEnvValidation', 'esEnvRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'es_env'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        url column:"url",insertable:true,updateable:true,lazy:false,cache:false
        cronExpr column:"cron_expr",insertable:true,updateable:true,lazy:false,cache:false
        running column:"running",insertable:true,updateable:true,lazy:false,cache:false
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        idx column:"idx",insertable:true,updateable:true,lazy:false,cache:false
        success column:"success",insertable:true,updateable:true,lazy:false,cache:false
        activeIndex column:"active_index",insertable:true,updateable:true,lazy:false,cache:false
        cacheUrls column:"cache_urls",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        url ( blank:false, nullable:false, unique:false)
        cronExpr ( blank:false, nullable:false, unique:false)
        running ( blank:false, nullable:false, unique:false)
        extra (nullable:true, unique:false)
        active ( blank:false, nullable:false, unique:false)
        idx (nullable:true, unique:false)
        success ( blank:false, nullable:false, unique:false)
        activeIndex (nullable:true, unique:false)
        cacheUrls (nullable:true, unique:false)
        company ( blank:false, nullable:false)
    }


    String toString(){return esEnvRender?.asString(this)}

    def beforeInsert = {
        esEnvValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        esEnvValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        esEnvValidation.beforeDelete(this)
    }

    def beforeValidate = {
        esEnvValidation.beforeValidate(this)
    }

    def afterInsert = {
        esEnvValidation.afterInsert(this)
    }

    def afterUpdate = {
        esEnvValidation.afterUpdate(this)
    }

    def afterDelete = {
        esEnvValidation.afterDelete(this)
    }

    def onLoad = {
        esEnvValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return esEnvRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
