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
class CountryAdmin
    implements java.io.Serializable
{
    def countryAdminValidation
    def countryAdminRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String code 
    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    int level 
    /**
     * 
     */
    com.mogobiz.store.domain.Country country 

    /**
     * 
     */
    com.mogobiz.store.domain.CountryAdmin parent 

    static transients = [ 'countryAdminValidation', 'countryAdminRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'country_admin'

        version false

        id name:'id',column:'id',generator:'native'
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        level column:"xlevel",insertable:true,updateable:true,lazy:false,cache:false


        country column:"country_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        parent column:"parent_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        code (nullable:true, unique:false)
        name (nullable:true, unique:false)
        level ( blank:false, nullable:false, unique:false)
        country ( blank:false, nullable:false)
        parent (nullable:true)
    }


    String toString(){return countryAdminRender?.asString(this)}

    def beforeInsert = {
        countryAdminValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        countryAdminValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        countryAdminValidation.beforeDelete(this)
    }

    def beforeValidate = {
        countryAdminValidation.beforeValidate(this)
    }

    def afterInsert = {
        countryAdminValidation.afterInsert(this)
    }

    def afterUpdate = {
        countryAdminValidation.afterUpdate(this)
    }

    def afterDelete = {
        countryAdminValidation.afterDelete(this)
    }

    def onLoad = {
        countryAdminValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return countryAdminRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}