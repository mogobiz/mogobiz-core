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
class Catalog
    implements java.io.Serializable
{
    def catalogValidation
    def catalogRender

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
    java.util.Date activationDate
    /**
     *
     */
    java.lang.String description
    /**
     *
     */
    boolean social  = false
    /**
     *
     */
    java.lang.String channels
    /**
     *
     */
    boolean deleted  = false
    /**
     *
     */
    java.lang.String xcatalog
    /**
     *
     */
    long returnMaxDelay  = 0
    /**
     *
     */
    java.lang.String i18n
    /**
     *
     */
    boolean readOnly  = false
    /**
     *
     */
    com.mogobiz.store.domain.Company company

    com.mogobiz.store.domain.MiraklEnv miraklEnv

    static transients = [ 'catalogValidation', 'catalogRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'xcatalog'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        activationDate column:"activation_date",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        social column:"social",insertable:true,updateable:true,lazy:false,cache:false
        channels column:"channels",insertable:true,updateable:true,lazy:false,cache:false
        deleted column:"deleted",insertable:true,updateable:true,lazy:false,cache:false
        xcatalog column:"xcatalog",insertable:true,updateable:true,lazy:false,cache:false
        returnMaxDelay column:"return_max_delay",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        readOnly column:"read_only",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
        miraklEnv column:"mirakl_env_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        name ( blank:false, nullable:false, unique:false)
        activationDate ( blank:false, nullable:false, unique:false)
        description (nullable:true, unique:false)
        social ( blank:false, nullable:false, unique:false)
        channels (nullable:true, unique:false)
        deleted ( blank:false, nullable:false, unique:false)
        xcatalog (nullable:true, unique:false)
        returnMaxDelay ( blank:false, nullable:false, unique:false)
        i18n (nullable:true, unique:false)
        readOnly (nullable:true, unique:false)
        company ( blank:false, nullable:false)
        miraklEnv (nullable:true, unique:false)
    }


    String toString(){return catalogRender?.asString(this)}

    def beforeInsert = {
        catalogValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        catalogValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        catalogValidation.beforeDelete(this)
    }

    def beforeValidate = {
        catalogValidation.beforeValidate(this)
    }

    def afterInsert = {
        catalogValidation.afterInsert(this)
    }

    def afterUpdate = {
        catalogValidation.afterUpdate(this)
    }

    def afterDelete = {
        catalogValidation.afterDelete(this)
    }

    def onLoad = {
        catalogValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return catalogRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
