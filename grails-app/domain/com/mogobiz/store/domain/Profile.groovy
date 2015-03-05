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
class Profile
    implements java.io.Serializable
{
    def profileValidation
    def profileRender

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
    com.mogobiz.store.domain.Company company 

    static transients = [ 'profileValidation', 'profileRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'profile'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        company (nullable:true)
    }


    String toString(){return profileRender?.asString(this)}

    def beforeInsert = {
        profileValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        profileValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        profileValidation.beforeDelete(this)
    }

    def beforeValidate = {
        profileValidation.beforeValidate(this)
    }

    def afterInsert = {
        profileValidation.afterInsert(this)
    }

    def afterUpdate = {
        profileValidation.afterUpdate(this)
    }

    def afterDelete = {
        profileValidation.afterDelete(this)
    }

    def onLoad = {
        profileValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return profileRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}