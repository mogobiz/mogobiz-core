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
class Consumption
    implements java.io.Serializable
{
    def consumptionValidation
    def consumptionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.util.Calendar date 

    static transients = [ 'consumptionValidation', 'consumptionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'consumption'

        version false

        id name:'id',column:'id',generator:'native'
        date column:"xdate",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        date ( blank:false, nullable:false, unique:false)
    }

    String toString(){return consumptionRender?.asString(this)}

    def beforeInsert = {
        consumptionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        consumptionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        consumptionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        consumptionValidation.beforeValidate(this)
    }

    def afterInsert = {
        consumptionValidation.afterInsert(this)
    }

    def afterUpdate = {
        consumptionValidation.afterUpdate(this)
    }

    def afterDelete = {
        consumptionValidation.afterDelete(this)
    }

    def onLoad = {
        consumptionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return consumptionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}