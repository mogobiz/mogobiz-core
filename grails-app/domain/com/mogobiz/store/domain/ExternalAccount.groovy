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
class ExternalAccount
    implements java.io.Serializable
{
    def externalAccountValidation
    def externalAccountRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String login 
    /**
     * 
     */
    com.mogobiz.store.domain.AccountType accountType 
    /**
     * 
     */
    java.lang.String token 
    /**
     * 
     */
    java.lang.String tokenSecret 
    /**
     * 
     */
    java.lang.String externalId 
    /**
     * 
     */
    com.mogobiz.store.domain.User user 

    static transients = [ 'externalAccountValidation', 'externalAccountRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'external_account'

        version false

        id name:'id',column:'id',generator:'native'
        login column:"login",insertable:true,updateable:true,lazy:false,cache:false
        accountType column:"account_type",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        token column:"token",insertable:true,updateable:true,lazy:false,cache:false
        tokenSecret column:"token_secret",insertable:true,updateable:true,lazy:false,cache:false
        externalId column:"external_id",insertable:true,updateable:true,lazy:false,cache:false


        user column:"user_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        login ( blank:false, nullable:false, unique:false)
        accountType ( blank:false, nullable:false, unique:false)
        token (nullable:true, unique:false)
        tokenSecret (nullable:true, unique:false)
        externalId (nullable:true, unique:false)
        user ( blank:false, nullable:false)
    }


    String toString(){return externalAccountRender?.asString(this)}

    def beforeInsert = {
        externalAccountValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        externalAccountValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        externalAccountValidation.beforeDelete(this)
    }

    def beforeValidate = {
        externalAccountValidation.beforeValidate(this)
    }

    def afterInsert = {
        externalAccountValidation.afterInsert(this)
    }

    def afterUpdate = {
        externalAccountValidation.afterUpdate(this)
    }

    def afterDelete = {
        externalAccountValidation.afterDelete(this)
    }

    def onLoad = {
        externalAccountValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return externalAccountRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}