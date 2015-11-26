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
class ExternalAuthLogin
    implements java.io.Serializable
{
    def externalAuthLoginValidation
    def externalAuthLoginRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    com.mogobiz.store.domain.AccountType accountType 
    /**
     * 
     */
    java.lang.String login 
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
    java.util.Calendar instant 
    /**
     * 
     */
    java.lang.String firstName 
    /**
     * 
     */
    java.lang.String lastName 
    /**
     * 
     */
    java.lang.String externalId 
    /**
     * 
     */
    java.lang.String email 
    /**
     * 
     */
    java.lang.String mobile 
    /**
     * 
     */
    java.lang.String gender 
    /**
     * <p>
     * sna2
     * </p>
     */
    java.lang.String road1 
    /**
     * <p>
     * sna3
     * </p>
     */
    java.lang.String road2 
    /**
     * <p>
     * sna4
     * </p>
     */
    java.lang.String road3 
    /**
     * <p>
     * sna5
     * </p>
     */
    java.lang.String road4 
    /**
     * <p>
     * Code postale
     * </p>
     */
    java.lang.String postalCode 
    /**
     * <p>
     * Ville
     * </p>
     */
    java.lang.String city 
    /**
     * <p>
     * Pays
     * </p>
     */
    java.lang.String country 
    /**
     * 
     */
    java.lang.String birthDate 
    static transients = [ 'externalAuthLoginValidation', 'externalAuthLoginRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'external_auth_login'

        version false

        id name:'id',column:'id',generator:'native'
        accountType column:"account_type",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        login column:"login",insertable:true,updateable:true,lazy:false,cache:false
        token column:"token",insertable:true,updateable:true,lazy:false,cache:false
        tokenSecret column:"token_secret",insertable:true,updateable:true,lazy:false,cache:false
        instant column:"instant",insertable:true,updateable:true,lazy:false,cache:false
        firstName column:"first_name",insertable:true,updateable:true,lazy:false,cache:false
        lastName column:"last_name",insertable:true,updateable:true,lazy:false,cache:false
        externalId column:"external_id",insertable:true,updateable:true,lazy:false,cache:false
        email column:"email",insertable:true,updateable:true,lazy:false,cache:false
        mobile column:"mobile",insertable:true,updateable:true,lazy:false,cache:false
        gender column:"gender",insertable:true,updateable:true,lazy:false,cache:false
        road1 column:"road1",insertable:true,updateable:true,lazy:false,cache:false
        road2 column:"road2",insertable:true,updateable:true,lazy:false,cache:false
        road3 column:"road3",insertable:true,updateable:true,lazy:false,cache:false
        road4 column:"road4",insertable:true,updateable:true,lazy:false,cache:false
        postalCode column:"postal_code",insertable:true,updateable:true,lazy:false,cache:false
        city column:"city",insertable:true,updateable:true,lazy:false,cache:false
        country column:"country",insertable:true,updateable:true,lazy:false,cache:false
        birthDate column:"birth_date",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        accountType ( blank:false, nullable:false, unique:false)
        login ( blank:false, nullable:false, unique:false)
        token (nullable:true, unique:false)
        tokenSecret (nullable:true, unique:false)
        instant (nullable:true, unique:false)
        firstName (nullable:true, unique:false)
        lastName (nullable:true, unique:false)
        externalId (nullable:true, unique:false)
        email (nullable:true, unique:false)
        mobile (nullable:true, unique:false)
        gender (nullable:true, unique:false)
        road1 (nullable:true, unique:false)
        road2 (nullable:true, unique:false)
        road3 (nullable:true, unique:false)
        road4 (nullable:true, unique:false)
        postalCode (nullable:true, unique:false)
        city (nullable:true, unique:false)
        country (nullable:true, unique:false)
        birthDate (nullable:true, unique:false)
    }


    String toString(){return externalAuthLoginRender?.asString(this)}

    def beforeInsert = {
        externalAuthLoginValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        externalAuthLoginValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        externalAuthLoginValidation.beforeDelete(this)
    }

    def beforeValidate = {
        externalAuthLoginValidation.beforeValidate(this)
    }

    def afterInsert = {
        externalAuthLoginValidation.afterInsert(this)
    }

    def afterUpdate = {
        externalAuthLoginValidation.afterUpdate(this)
    }

    def afterDelete = {
        externalAuthLoginValidation.afterDelete(this)
    }

    def onLoad = {
        externalAuthLoginValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return externalAuthLoginRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}