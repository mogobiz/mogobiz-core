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
class RolePermission
    implements java.io.Serializable
{
    def rolePermissionValidation
    def rolePermissionRender

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
    java.lang.String key 
    /**
     * 
     */
    com.mogobiz.store.domain.Role role 

    /**
     * 
     */
    com.mogobiz.store.domain.Permission permission 

    static transients = [ 'rolePermissionValidation', 'rolePermissionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'role_permission'

        version false

        id name:'id',column:'id',generator:'native'
        target column:"target",insertable:true,updateable:true,lazy:false,cache:false
        actions column:"actions",insertable:true,updateable:true,lazy:false,cache:false
        key column:"key",insertable:true,updateable:true,lazy:false,cache:false


        role column:"role_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        permission column:"permission_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        target ( blank:false, nullable:false, unique:false, validator:com.mogobiz.store.domain.RolePermissionValidation.rolePermissionTargetValidator)
        actions ( blank:false, nullable:false, unique:false)
        key (nullable:true, unique:false)
        role ( blank:false, nullable:false)
        permission ( blank:false, nullable:false)
    }


    String toString(){return rolePermissionRender?.asString(this)}

    def beforeInsert = {
        rolePermissionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        rolePermissionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        rolePermissionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        rolePermissionValidation.beforeValidate(this)
    }

    def afterInsert = {
        rolePermissionValidation.afterInsert(this)
    }

    def afterUpdate = {
        rolePermissionValidation.afterUpdate(this)
    }

    def afterDelete = {
        rolePermissionValidation.afterDelete(this)
    }

    def onLoad = {
        rolePermissionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return rolePermissionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}