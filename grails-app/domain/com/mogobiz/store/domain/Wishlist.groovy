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
class Wishlist
    implements java.io.Serializable
{
    def wishlistValidation
    def wishlistRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String extra 
    static transients = [ 'wishlistValidation', 'wishlistRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'wishlist'

        version false

        id name:'id',column:'id',generator:'native'
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        extra ( blank:false, nullable:false, unique:false)
    }


    String toString(){return wishlistRender?.asString(this)}

    def beforeInsert = {
        wishlistValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        wishlistValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        wishlistValidation.beforeDelete(this)
    }

    def beforeValidate = {
        wishlistValidation.beforeValidate(this)
    }

    def afterInsert = {
        wishlistValidation.afterInsert(this)
    }

    def afterUpdate = {
        wishlistValidation.afterUpdate(this)
    }

    def afterDelete = {
        wishlistValidation.afterDelete(this)
    }

    def onLoad = {
        wishlistValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return wishlistRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}