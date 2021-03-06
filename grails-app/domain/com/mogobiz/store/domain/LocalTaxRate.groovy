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
class LocalTaxRate
    implements java.io.Serializable
{
    def localTaxRateValidation
    def localTaxRateRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    float rate  = 0 
    /**
     * 
     */
    boolean active  = true 
    /**
     * 
     */
    java.lang.String countryCode 
    /**
     * 
     */
    java.lang.String stateCode 
    static transients = [ 'localTaxRateValidation', 'localTaxRateRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'local_tax_rate'

        version false

        id name:'id',column:'id',generator:'native'
        rate column:"rate",insertable:true,updateable:true,lazy:false,cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        countryCode column:"country_code",insertable:true,updateable:true,lazy:false,cache:false
        stateCode column:"state_code",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        rate ( blank:false, nullable:false, unique:false)
        active ( blank:false, nullable:false, unique:false)
        countryCode (nullable:true, unique:false, validator:com.mogobiz.store.domain.LocalTaxRateValidation.localTaxRateCountryCodeValidator)
        stateCode (nullable:true, unique:false)
    }


    String toString(){return localTaxRateRender?.asString(this)}

    def beforeInsert = {
        localTaxRateValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        localTaxRateValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        localTaxRateValidation.beforeDelete(this)
    }

    def beforeValidate = {
        localTaxRateValidation.beforeValidate(this)
    }

    def afterInsert = {
        localTaxRateValidation.afterInsert(this)
    }

    def afterUpdate = {
        localTaxRateValidation.afterUpdate(this)
    }

    def afterDelete = {
        localTaxRateValidation.afterDelete(this)
    }

    def onLoad = {
        localTaxRateValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return localTaxRateRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}