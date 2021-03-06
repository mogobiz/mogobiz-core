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
class CompanyProperty
    implements java.io.Serializable
{
    def companyPropertyValidation
    def companyPropertyRender

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
    java.lang.String value 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'companyPropertyValidation', 'companyPropertyRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'company_property'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        value ( blank:false, nullable:false, unique:false)
        company ( blank:false, nullable:false)
    }


    String toString(){return companyPropertyRender?.asString(this)}

    def beforeInsert = {
        companyPropertyValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        companyPropertyValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        companyPropertyValidation.beforeDelete(this)
    }

    def beforeValidate = {
        companyPropertyValidation.beforeValidate(this)
    }

    def afterInsert = {
        companyPropertyValidation.afterInsert(this)
    }

    def afterUpdate = {
        companyPropertyValidation.afterUpdate(this)
    }

    def afterDelete = {
        companyPropertyValidation.afterDelete(this)
    }

    def onLoad = {
        companyPropertyValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return companyPropertyRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}