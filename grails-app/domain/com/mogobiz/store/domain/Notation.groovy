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
class Notation
    implements java.io.Serializable
{
    def notationValidation
    def notationRender

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
    java.lang.Long productId 
    static transients = [ 'notationValidation', 'notationRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'notation'

        version false

        id name:'id',column:'id',generator:'native'
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        productId column:"product_id",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        extra ( blank:false, nullable:false, unique:false)
        productId ( blank:false, nullable:false, unique:false)
    }


    String toString(){return notationRender?.asString(this)}

    def beforeInsert = {
        notationValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        notationValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        notationValidation.beforeDelete(this)
    }

    def beforeValidate = {
        notationValidation.beforeValidate(this)
    }

    def afterInsert = {
        notationValidation.afterInsert(this)
    }

    def afterUpdate = {
        notationValidation.afterUpdate(this)
    }

    def afterDelete = {
        notationValidation.afterDelete(this)
    }

    def onLoad = {
        notationValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return notationRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}