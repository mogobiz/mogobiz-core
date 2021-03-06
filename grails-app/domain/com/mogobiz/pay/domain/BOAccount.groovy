/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.pay.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class BOAccount
    implements java.io.Serializable
{
    def BOAccountValidation
    def BOAccountRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String email 
    /**
     * 
     */
    java.lang.String company 
    /**
     * 
     */
    java.lang.String extra 
    static transients = [ 'BOAccountValidation', 'BOAccountRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_account'

        version false

        id name:'id',column:'id',generator:'native'
        email column:"email",insertable:true,updateable:true,lazy:false,cache:false
        company column:"company",insertable:true,updateable:true,lazy:false,cache:false
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        email ( blank:false, nullable:false, unique:false)
        company (nullable:true, unique:false)
        extra (nullable:true, unique:false)
    }


    String toString(){return BOAccountRender?.asString(this)}

    def beforeInsert = {
        BOAccountValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOAccountValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOAccountValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOAccountValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOAccountValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOAccountValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOAccountValidation.afterDelete(this)
    }

    def onLoad = {
        BOAccountValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOAccountRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}