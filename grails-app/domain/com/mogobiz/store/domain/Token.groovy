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
class Token
    implements java.io.Serializable
{
    def tokenValidation
    def tokenRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String redirectURI 
    /**
     * 
     */
    java.lang.String value 
    /**
     * 
     */
    int expiresIn  = 0 
    /**
     * 
     */
    java.lang.String scope 
    /**
     * 
     */
    java.lang.String clientId 
    /**
     * 
     */
    java.lang.String state 
    /**
     * 
     */
    com.mogobiz.store.domain.User user 

    static transients = [ 'tokenValidation', 'tokenRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'token'

        version false

        id name:'id',column:'id',generator:'native'
        redirectURI column:"redirect_u_r_i",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false
        expiresIn column:"expires_in",insertable:true,updateable:true,lazy:false,cache:false
        scope column:"scope",insertable:true,updateable:true,lazy:false,cache:false
        clientId column:"client_id",insertable:true,updateable:true,lazy:false,cache:false
        state column:"state",insertable:true,updateable:true,lazy:false,cache:false


        user column:"user_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        redirectURI ( blank:false, nullable:false, unique:false)
        value ( blank:false, nullable:false, unique:false)
        expiresIn ( blank:false, nullable:false, unique:false)
        scope (nullable:true, unique:false)
        clientId ( blank:false, nullable:false, unique:false)
        state (nullable:true, unique:false)
        user ( blank:false, nullable:false)
    }


    String toString(){return tokenRender?.asString(this)}

    def beforeInsert = {
        tokenValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        tokenValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        tokenValidation.beforeDelete(this)
    }

    def beforeValidate = {
        tokenValidation.beforeValidate(this)
    }

    def afterInsert = {
        tokenValidation.afterInsert(this)
    }

    def afterUpdate = {
        tokenValidation.afterUpdate(this)
    }

    def afterDelete = {
        tokenValidation.afterDelete(this)
    }

    def onLoad = {
        tokenValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return tokenRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}