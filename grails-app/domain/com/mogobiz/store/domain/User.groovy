// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEntity.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class User
    implements java.io.Serializable
{
    def userValidation
    def userRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * <p>
     * Pour les vendeurs correspondra à l'adresse mail, pour les
     * acheteurs, correspondra à la concaténation de l'adresse mail et
     * de l'identifiant calypso
     * </p>
     */
    java.lang.String login 
    /**
     * 
     */
    java.lang.String email 
    /**
     * 
     */
    java.lang.String password 
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
    com.mogobiz.store.domain.CivilityType civility 
    /**
     * 
     */
    boolean active  = false 
    /**
     * 
     */
    java.lang.String phone 
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
    java.util.Date birthdate 
    /**
     * 
     */
    boolean autosign  = false     /**
     * 
     */
    com.mogobiz.geolocation.domain.Location location

    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static hasMany = [ roles:com.mogobiz.store.domain.Role , profiles:com.mogobiz.store.domain.Profile ]

    static transients = [ 'userValidation', 'userRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'account'

        version false

        id name:'id',column:'id',generator:'native'
        login column:"login",insertable:true,updateable:true,lazy:false,cache:false
        email column:"email",insertable:true,updateable:true,lazy:false,cache:false
        password column:"password",insertable:true,updateable:true,lazy:false,cache:false
        firstName column:"first_name",insertable:true,updateable:true,lazy:false,cache:false
        lastName column:"last_name",insertable:true,updateable:true,lazy:false,cache:false
        civility column:"civility",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        phone column:"phone",insertable:true,updateable:true,lazy:false,cache:false
        accountType column:"account_type",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        token column:"token",insertable:true,updateable:true,lazy:false,cache:false
        tokenSecret column:"token_secret",insertable:true,updateable:true,lazy:false,cache:false
        externalId column:"external_id",insertable:true,updateable:true,lazy:false,cache:false
        birthdate column:"birthdate",insertable:true,updateable:true,lazy:false,cache:false
        autosign column:"autosign",insertable:true,updateable:true,lazy:false,cache:false


        location column:"location_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'

        roles column:"roles_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        profiles column:"profiles_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        login ( blank:false, nullable:false, unique:true)
        email ( blank:false, nullable:false, unique:true, email:true)
        password ( blank:false, nullable:false, unique:false)
        firstName (nullable:true, unique:false)
        lastName (nullable:true, unique:false)
        civility (nullable:true, unique:false)
        active ( blank:false, nullable:false, unique:false)
        phone (nullable:true, unique:false)
        accountType (nullable:true, unique:false)
        token (nullable:true, unique:false)
        tokenSecret (nullable:true, unique:false)
        externalId (nullable:true, unique:false)
        birthdate (nullable:true, unique:false)
        autosign ( blank:false, nullable:false, unique:false)
        location (nullable:true)
        company (nullable:true)
    }


    String toString(){return userRender?.asString(this)}

    def beforeInsert = {
        userValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        userValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        userValidation.beforeDelete(this)
    }

    def beforeValidate = {
        userValidation.beforeValidate(this)
    }

    def afterInsert = {
        userValidation.afterInsert(this)
    }

    def afterUpdate = {
        userValidation.afterUpdate(this)
    }

    def afterDelete = {
        userValidation.afterDelete(this)
    }

    def onLoad = {
        userValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return userRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}