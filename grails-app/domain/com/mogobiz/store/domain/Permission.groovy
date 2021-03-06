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
class Permission
    implements java.io.Serializable
{
    def permissionValidation
    def permissionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String type  = "org.apache.shiro.authz.permission.WildcardPermission" 
    /**
     * 
     */
    java.lang.String possibleActions  = "*" 
    static transients = [ 'permissionValidation', 'permissionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'permission'

        version false

        id name:'id',column:'id',generator:'native'
        type column:"type",insertable:true,updateable:true,lazy:false,cache:false
        possibleActions column:"possible_actions",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        type ( blank:false, nullable:false, unique:false)
        possibleActions ( blank:false, nullable:false, unique:false)
    }


    String toString(){return permissionRender?.asString(this)}

    def beforeInsert = {
        permissionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        permissionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        permissionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        permissionValidation.beforeValidate(this)
    }

    def afterInsert = {
        permissionValidation.afterInsert(this)
    }

    def afterUpdate = {
        permissionValidation.afterUpdate(this)
    }

    def afterDelete = {
        permissionValidation.afterDelete(this)
    }

    def onLoad = {
        permissionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return permissionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}