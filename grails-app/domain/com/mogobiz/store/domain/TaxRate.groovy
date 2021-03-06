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
class TaxRate
    implements java.io.Serializable
{
    def taxRateValidation
    def taxRateRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static hasMany = [  localTaxRates:com.mogobiz.store.domain.LocalTaxRate ]

    static transients = [ 'taxRateValidation', 'taxRateRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'tax_rate'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        localTaxRates column:"local_tax_rates_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        company (nullable:true)
    }


    String toString(){return taxRateRender?.asString(this)}

    def beforeInsert = {
        taxRateValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        taxRateValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        taxRateValidation.beforeDelete(this)
    }

    def beforeValidate = {
        taxRateValidation.beforeValidate(this)
    }

    def afterInsert = {
        taxRateValidation.afterInsert(this)
    }

    def afterUpdate = {
        taxRateValidation.afterUpdate(this)
    }

    def afterDelete = {
        taxRateValidation.afterDelete(this)
    }

    def onLoad = {
        taxRateValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return taxRateRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}