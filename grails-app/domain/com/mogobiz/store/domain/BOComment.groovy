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
class BOComment
    implements java.io.Serializable
{
    def BOCommentValidation
    def BOCommentRender

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
    static transients = [ 'BOCommentValidation', 'BOCommentRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_comment'

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


    String toString(){return BOCommentRender?.asString(this)}

    def beforeInsert = {
        BOCommentValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOCommentValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOCommentValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOCommentValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOCommentValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOCommentValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOCommentValidation.afterDelete(this)
    }

    def onLoad = {
        BOCommentValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOCommentRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}