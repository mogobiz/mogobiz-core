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
class Album
    implements java.io.Serializable
{
    def albumValidation
    def albumRender

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
    java.lang.String description 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    static transients = [ 'albumValidation', 'albumRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'album'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        description (nullable:true, unique:false)
        company (nullable:true)
    }


    String toString(){return albumRender?.asString(this)}

    def beforeInsert = {
        albumValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        albumValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        albumValidation.beforeDelete(this)
    }

    def beforeValidate = {
        albumValidation.beforeValidate(this)
    }

    def afterInsert = {
        albumValidation.afterInsert(this)
    }

    def afterUpdate = {
        albumValidation.afterUpdate(this)
    }

    def afterDelete = {
        albumValidation.afterDelete(this)
    }

    def onLoad = {
        albumValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return albumRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}