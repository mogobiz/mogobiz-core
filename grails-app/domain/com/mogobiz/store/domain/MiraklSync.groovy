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
class MiraklSync
    implements java.io.Serializable
{
    def miraklSyncValidation
    def miraklSyncRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String trackingId 
    /**
     * 
     */
    com.mogobiz.store.domain.MiraklSyncType type 
    /**
     * 
     */
    com.mogobiz.store.domain.MiraklSyncStatus status 
    /**
     * 
     */
    java.lang.String errorReport 
    /**
     * 
     */
    java.util.Date timestamp 
    /**
     * 
     */
    com.mogobiz.store.domain.Catalog catalog 

    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'miraklSyncValidation', 'miraklSyncRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'mirakl_sync'

        version false

        id name:'id',column:'id',generator:'native'
        trackingId column:"tracking_id",insertable:true,updateable:true,lazy:false,cache:false
        type column:"type",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        status column:"status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        errorReport column:"error_report",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        timestamp column:"timestamp",insertable:true,updateable:true,lazy:false,cache:false


        catalog column:"catalog_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        trackingId ( blank:false, nullable:false, unique:false)
        type ( blank:false, nullable:false, unique:false)
        status ( blank:false, nullable:false, unique:false)
        errorReport (nullable:true, unique:false)
        timestamp ( blank:false, nullable:false, unique:false)
        catalog ( blank:false, nullable:false)
        company ( blank:false, nullable:false)
    }


    String toString(){return miraklSyncRender?.asString(this)}

    def beforeInsert = {
        miraklSyncValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        miraklSyncValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        miraklSyncValidation.beforeDelete(this)
    }

    def beforeValidate = {
        miraklSyncValidation.beforeValidate(this)
    }

    def afterInsert = {
        miraklSyncValidation.afterInsert(this)
    }

    def afterUpdate = {
        miraklSyncValidation.afterUpdate(this)
    }

    def afterDelete = {
        miraklSyncValidation.afterDelete(this)
    }

    def onLoad = {
        miraklSyncValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return miraklSyncRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}