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
class BOWishlist
    implements java.io.Serializable
{
    def BOWishlistValidation
    def BOWishlistRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String extra 
    /**
     * 
     */
    java.lang.String company 
    static transients = [ 'BOWishlistValidation', 'BOWishlistRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_wishlist'

        version false

        id name:'id',column:'id',generator:'native'
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        company column:"company",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        extra (nullable:true, unique:false)
        company (nullable:true, unique:false)
    }


    String toString(){return BOWishlistRender?.asString(this)}

    def beforeInsert = {
        BOWishlistValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOWishlistValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOWishlistValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOWishlistValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOWishlistValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOWishlistValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOWishlistValidation.afterDelete(this)
    }

    def onLoad = {
        BOWishlistValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOWishlistRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}