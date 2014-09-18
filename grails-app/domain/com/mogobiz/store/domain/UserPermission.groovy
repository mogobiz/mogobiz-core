// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEntity.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class UserPermission
    implements java.io.Serializable
{
    def userPermissionValidation
    def userPermissionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String target 
    /**
     * 
     */
    java.lang.String actions  = "*" 
    /**
     * 
     */
    com.mogobiz.store.domain.Permission permission 

    /**
     * 
     */
    com.mogobiz.store.domain.User user 

    static transients = [ 'userPermissionValidation', 'userPermissionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'user_permission'

        version false

        id name:'id',column:'id',generator:'native'
        target column:"target",insertable:true,updateable:true,lazy:false,cache:false
        actions column:"actions",insertable:true,updateable:true,lazy:false,cache:false


        permission column:"permission_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        user column:"user_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        target ( blank:false, nullable:false, unique:false, validator:com.mogobiz.store.domain.UserPermissionValidation.userPermissionTargetValidator)
        actions ( blank:false, nullable:false, unique:false)
        permission ( blank:false, nullable:false)
        user ( blank:false, nullable:false)
    }


    String toString(){return userPermissionRender?.asString(this)}

    def beforeInsert = {
        userPermissionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        userPermissionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        userPermissionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        userPermissionValidation.beforeValidate(this)
    }

    def afterInsert = {
        userPermissionValidation.afterInsert(this)
    }

    def afterUpdate = {
        userPermissionValidation.afterUpdate(this)
    }

    def afterDelete = {
        userPermissionValidation.afterDelete(this)
    }

    def onLoad = {
        userPermissionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return userPermissionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}