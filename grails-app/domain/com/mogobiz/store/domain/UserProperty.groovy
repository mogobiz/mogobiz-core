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
class UserProperty
    implements java.io.Serializable
{
    def userPropertyValidation
    def userPropertyRender

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
    com.mogobiz.store.domain.User user 

    static transients = [ 'userPropertyValidation', 'userPropertyRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'user_property'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false


        user column:"user_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        value (nullable:true, unique:false)
        user ( blank:false, nullable:false)
    }


    String toString(){return userPropertyRender?.asString(this)}

    def beforeInsert = {
        userPropertyValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        userPropertyValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        userPropertyValidation.beforeDelete(this)
    }

    def beforeValidate = {
        userPropertyValidation.beforeValidate(this)
    }

    def afterInsert = {
        userPropertyValidation.afterInsert(this)
    }

    def afterUpdate = {
        userPropertyValidation.afterUpdate(this)
    }

    def afterDelete = {
        userPropertyValidation.afterDelete(this)
    }

    def onLoad = {
        userPropertyValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return userPropertyRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}