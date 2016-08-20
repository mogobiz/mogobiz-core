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
@EqualsAndHashCode(includes="code")
class Country
    implements java.io.Serializable
{
    def countryValidation
    def countryRender

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
    boolean shipping
    /**
     *
     */
    boolean billing
    /**
     *
     */
    java.lang.String postalCodeRegex
    /**
     *
     */
    java.lang.String currencyCode
    /**
     *
     */
    java.lang.String currencyNumericCode
    /**
     *
     */
    java.lang.String currencyName
    /**
     *
     */
    java.lang.String phoneCode
    /**
     *
     */
    java.lang.String isoCode3
    /**
     *
     */
    java.lang.String isoNumericCode
    static transients = [ 'countryValidation', 'countryRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'country'

        version false

        id name:'code',column:'code',generator:'assigned'
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        shipping column:"shipping",insertable:true,updateable:true,lazy:false,cache:false
        billing column:"billing",insertable:true,updateable:true,lazy:false,cache:false
        postalCodeRegex column:"postal_code_regex",insertable:true,updateable:true,lazy:false,cache:false
        currencyCode column:"currency_code",insertable:true,updateable:true,lazy:false,cache:false
        currencyNumericCode column:"currency_numeric_code",insertable:true,updateable:true,lazy:false,cache:false
        currencyName column:"currency_name",insertable:true,updateable:true,lazy:false,cache:false
        phoneCode column:"phone_code",insertable:true,updateable:true,lazy:false,cache:false
        isoCode3 column:"iso_code3",insertable:true,updateable:true,lazy:false,cache:false
        isoNumericCode column:"iso_numeric_code",insertable:true,updateable:true,lazy:false,cache:false
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        code ( blank:false, nullable:false, unique:false)
        name ( blank:false, nullable:false, unique:false)
        shipping ( blank:false, nullable:false, unique:false)
        billing ( blank:false, nullable:false, unique:false)
        postalCodeRegex (nullable:true, unique:false)
        currencyCode (nullable:true, unique:false)
        currencyNumericCode (nullable:true, unique:false)
        currencyName (nullable:true, unique:false)
        phoneCode (nullable:true, unique:false)
        isoCode3 (nullable:true, unique:false)
        isoNumericCode (nullable:true, unique:false)
    }


    String toString(){return countryRender?.asString(this)}

    def beforeInsert = {
        countryValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        countryValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        countryValidation.beforeDelete(this)
    }

    def beforeValidate = {
        countryValidation.beforeValidate(this)
    }

    def afterInsert = {
        countryValidation.afterInsert(this)
    }

    def afterUpdate = {
        countryValidation.afterUpdate(this)
    }

    def afterDelete = {
        countryValidation.afterDelete(this)
    }

    def onLoad = {
        countryValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return countryRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
