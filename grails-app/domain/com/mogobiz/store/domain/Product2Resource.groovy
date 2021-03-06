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
class Product2Resource
    implements java.io.Serializable
{
    def product2ResourceValidation
    def product2ResourceRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    int position 
    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    /**
     * 
     */
    com.mogobiz.store.domain.Resource resource 

    static transients = [ 'product2ResourceValidation', 'product2ResourceRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'product2_resource'

        version false

        id name:'id',column:'id',generator:'native'
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        resource column:"resource_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        position ( blank:false, nullable:false, unique:false)
        product ( blank:false, nullable:false)
        resource ( blank:false, nullable:false)
    }


    String toString(){return product2ResourceRender?.asString(this)}

    def beforeInsert = {
        product2ResourceValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        product2ResourceValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        product2ResourceValidation.beforeDelete(this)
    }

    def beforeValidate = {
        product2ResourceValidation.beforeValidate(this)
    }

    def afterInsert = {
        product2ResourceValidation.afterInsert(this)
    }

    def afterUpdate = {
        product2ResourceValidation.afterUpdate(this)
    }

    def afterDelete = {
        product2ResourceValidation.afterDelete(this)
    }

    def onLoad = {
        product2ResourceValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return product2ResourceRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}