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
    Boolean running  = false
    /**
     *
     */
    java.lang.String frontKey
    /**
     *
     */
    Boolean active  = false
    /**
     *
     */
    java.lang.String cronExpr
    /**
     *
     */
    java.lang.String name
    /**
     *
     */
    Boolean operator  = false
    /**
     *
     */
    String remoteHost
    /**
     *
     */
    String remotePath
    /**
     *
     */
    String username
    /**
     *
     */
    String password
    /**
     *
     */
    String keyPath
    /**
     *
     */
    String passPhrase
    /**
     *
     */
    String localPath
    /**
     *
     */
    java.lang.String shopIds
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
        frontKey column:"front_key",insertable:true,updateable:true,lazy:false,cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        cronExpr column:"cron_expr",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        operator column:"operator",insertable:true,updateable:true,lazy:false,cache:false
        remoteHost column:"remote_host",insertable:true,updateable:true,lazy:false,cache:false
        remotePath column:"remote_path",insertable:true,updateable:true,lazy:false,cache:false
        username column:"username",insertable:true,updateable:true,lazy:false,cache:false
        password column:"password",insertable:true,updateable:true,lazy:false,cache:false
        keyPath column:"key_path",insertable:true,updateable:true,lazy:false,cache:false
        passPhrase column:"pass_phrase",insertable:true,updateable:true,lazy:false,cache:false
        localPath column:"local_path",insertable:true,updateable:true,lazy:false,cache:false
        shopIds column:"shop_ids",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        url ( blank:false, nullable:false, unique:false)
        apiKey ( blank:false, nullable:false, unique:false)
        shopId ( blank:false, nullable:false, unique:false)
        running ( nullable:true, unique:false)
        frontKey (nullable:true, unique:false)
        active ( nullable:true, unique:false)
        cronExpr (nullable:true, unique:false)
        name (nullable:true, unique:false)
        company ( blank:false, nullable:false)
        operator ( nullable:true, unique:false)
        remoteHost ( nullable:true, unique:false)
        remotePath ( nullable:true, unique:false)
        username ( nullable:true, unique:false)
        password ( nullable:true, unique:false)
        keyPath ( nullable:true, unique:false)
        passPhrase ( nullable:true, unique:false)
        localPath ( nullable:true, unique:false)
        shopIds ( nullable:true, unique:false)
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
