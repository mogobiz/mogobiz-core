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
class Tag
    implements java.io.Serializable
{
    def tagValidation
    def tagRender

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
    com.mogobiz.store.domain.Ibeacon ibeacon 

    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'tagValidation', 'tagRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'tag'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false


        ibeacon column:"ibeacon_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        ibeacon (nullable:true)
        company ( blank:false, nullable:false)
    }


    String toString(){return tagRender?.asString(this)}

    def beforeInsert = {
        tagValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        tagValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        tagValidation.beforeDelete(this)
    }

    def beforeValidate = {
        tagValidation.beforeValidate(this)
    }

    def afterInsert = {
        tagValidation.afterInsert(this)
    }

    def afterUpdate = {
        tagValidation.afterUpdate(this)
    }

    def afterDelete = {
        tagValidation.afterDelete(this)
    }

    def onLoad = {
        tagValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return tagRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}