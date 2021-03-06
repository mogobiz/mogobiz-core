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
class ProductProperty
    implements java.io.Serializable
{
    def productPropertyValidation
    def productPropertyRender

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
    java.lang.String i18n 
    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    static transients = [ 'productPropertyValidation', 'productPropertyRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'product_property'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        value column:"value",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        value (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        product ( blank:false, nullable:false)
    }


    String toString(){return productPropertyRender?.asString(this)}

    def beforeInsert = {
        productPropertyValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        productPropertyValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        productPropertyValidation.beforeDelete(this)
    }

    def beforeValidate = {
        productPropertyValidation.beforeValidate(this)
    }

    def afterInsert = {
        productPropertyValidation.afterInsert(this)
    }

    def afterUpdate = {
        productPropertyValidation.afterUpdate(this)
    }

    def afterDelete = {
        productPropertyValidation.afterDelete(this)
    }

    def onLoad = {
        productPropertyValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return productPropertyRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}