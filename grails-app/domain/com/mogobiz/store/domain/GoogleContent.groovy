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
class GoogleContent
    implements java.io.Serializable
{
    def googleContentValidation
    def googleContentRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String accountId 
    /**
     * 
     */
    java.lang.String accountLogin 
    /**
     * 
     */
    java.lang.String accountPassword 
    /**
     * 
     */
    java.lang.String accountType 
    /**
     * 
     */
    boolean googleSearch  = true 
    static transients = [ 'googleContentValidation', 'googleContentRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'google_content'

        version false

        id name:'id',column:'id',generator:'native'
        accountId column:"account_id",insertable:true,updateable:true,lazy:false,cache:false
        accountLogin column:"account_login",insertable:true,updateable:true,lazy:false,cache:false
        accountPassword column:"account_password",insertable:true,updateable:true,lazy:false,cache:false
        accountType column:"account_type",insertable:true,updateable:true,lazy:false,cache:false
        googleSearch column:"google_search",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        accountId ( blank:false, nullable:false, unique:false)
        accountLogin ( blank:false, nullable:false, unique:false)
        accountPassword ( blank:false, nullable:false, unique:false)
        accountType ( blank:false, nullable:false, unique:false)
        googleSearch ( blank:false, nullable:false, unique:false)
    }


    String toString(){return googleContentRender?.asString(this)}

    def beforeInsert = {
        googleContentValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        googleContentValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        googleContentValidation.beforeDelete(this)
    }

    def beforeValidate = {
        googleContentValidation.beforeValidate(this)
    }

    def afterInsert = {
        googleContentValidation.afterInsert(this)
    }

    def afterUpdate = {
        googleContentValidation.afterUpdate(this)
    }

    def afterDelete = {
        googleContentValidation.afterDelete(this)
    }

    def onLoad = {
        googleContentValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return googleContentRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}