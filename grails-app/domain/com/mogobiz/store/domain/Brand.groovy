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
class Brand
    implements java.io.Serializable
{
    def brandValidation
    def brandRender

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
    java.lang.String website 
    /**
     * 
     */
    java.lang.String facebooksite 
    /**
     * 
     */
    boolean hide  = false 
    /**
     * 
     */
    java.lang.String description 
    /**
     * 
     */
    java.lang.String twitter 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    /**
     * 
     */
    com.mogobiz.store.domain.Ibeacon ibeacon 

    /**
     * 
     */
    com.mogobiz.store.domain.Brand parent 

    static transients = [ 'brandValidation', 'brandRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'brand'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        website column:"website",insertable:true,updateable:true,lazy:false,cache:false
        facebooksite column:"facebooksite",insertable:true,updateable:true,lazy:false,cache:false
        hide column:"hide",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        twitter column:"twitter",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ibeacon column:"ibeacon_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        parent column:"parent_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        website (nullable:true, unique:false)
        facebooksite (nullable:true, unique:false)
        hide ( blank:false, nullable:false, unique:false)
        description (nullable:true, unique:false)
        twitter (nullable:true, unique:false)
        company ( blank:false, nullable:false)
        ibeacon (nullable:true)
        parent (nullable:true)
    }


    String toString(){return brandRender?.asString(this)}

    def beforeInsert = {
        brandValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        brandValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        brandValidation.beforeDelete(this)
    }

    def beforeValidate = {
        brandValidation.beforeValidate(this)
    }

    def afterInsert = {
        brandValidation.afterInsert(this)
    }

    def afterUpdate = {
        brandValidation.afterUpdate(this)
    }

    def afterDelete = {
        brandValidation.afterDelete(this)
    }

    def onLoad = {
        brandValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return brandRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}