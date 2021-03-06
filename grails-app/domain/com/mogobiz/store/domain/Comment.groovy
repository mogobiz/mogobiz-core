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
class Comment
    implements java.io.Serializable
{
    def commentValidation
    def commentRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String extra 
    static transients = [ 'commentValidation', 'commentRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'comment'

        version false

        id name:'id',column:'id',generator:'native'
        extra column:"extra",insertable:true,updateable:true,lazy:false,type:"text",cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        extra ( blank:false, nullable:false, unique:false)
    }


    String toString(){return commentRender?.asString(this)}

    def beforeInsert = {
        commentValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        commentValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        commentValidation.beforeDelete(this)
    }

    def beforeValidate = {
        commentValidation.beforeValidate(this)
    }

    def afterInsert = {
        commentValidation.afterInsert(this)
    }

    def afterUpdate = {
        commentValidation.afterUpdate(this)
    }

    def afterDelete = {
        commentValidation.afterDelete(this)
    }

    def onLoad = {
        commentValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return commentRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}