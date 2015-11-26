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
class Ibeacon
    implements java.io.Serializable
{
    def ibeaconValidation
    def ibeaconRender

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
    java.util.Calendar startDate 
    /**
     * 
     */
    java.util.Calendar endDate 
    /**
     * 
     */
    boolean active 
    /**
     * 
     */
    String major  = 00000 
    /**
     * 
     */
    java.lang.String minor  = "00000" 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'ibeaconValidation', 'ibeaconRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'ibeacon'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        endDate column:"end_date",insertable:true,updateable:true,lazy:false,cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        major column:"major",insertable:true,updateable:true,lazy:false,cache:false
        minor column:"minor",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        startDate ( blank:false, nullable:false, unique:false)
        endDate ( blank:false, nullable:false, unique:false)
        active ( blank:false, nullable:false, unique:false)
        major ( blank:false, nullable:false, unique:false)
        minor ( blank:false, nullable:false, unique:false)
        company ( blank:false, nullable:false)
    }


    String toString(){return ibeaconRender?.asString(this)}

    def beforeInsert = {
        ibeaconValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        ibeaconValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        ibeaconValidation.beforeDelete(this)
    }

    def beforeValidate = {
        ibeaconValidation.beforeValidate(this)
    }

    def afterInsert = {
        ibeaconValidation.afterInsert(this)
    }

    def afterUpdate = {
        ibeaconValidation.afterUpdate(this)
    }

    def afterDelete = {
        ibeaconValidation.afterDelete(this)
    }

    def onLoad = {
        ibeaconValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return ibeaconRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}