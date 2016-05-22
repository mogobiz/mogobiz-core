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
class Category
    implements java.io.Serializable
{
    def categoryValidation
    def categoryRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String externalCode 
    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    java.lang.String description 
    /**
     * 
     */
    java.lang.String keywords 
    /**
     * 
     */
    int position 
    /**
     * 
     */
    Boolean hide  = false 
    /**
     * 
     */
    java.lang.String sanitizedName 
    /**
     * 
     */
    java.lang.String googleCategory 
    /**
     * 
     */
    boolean deleted  = false 
    /**
     * 
     */
    long returnMaxDelay  = 0 
    /**
     * 
     */
    String fullpath 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    /**
     * 
     */
    com.mogobiz.store.domain.Category parent 

    /**
     * 
     */
    com.mogobiz.store.domain.Catalog catalog 

    /**
     * 
     */
    com.mogobiz.store.domain.Ibeacon ibeacon 

    static transients = [ 'categoryValidation', 'categoryRender' ]

    static hasMany = [features: Feature]

    static mappedBy = [features: "category"]

    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'category'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        keywords column:"keywords",insertable:true,updateable:true,lazy:false,cache:false
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false
        hide column:"hide",insertable:true,updateable:true,lazy:false,cache:false
        sanitizedName column:"sanitized_name",insertable:true,updateable:true,lazy:false,cache:false
        googleCategory column:"google_category",insertable:true,updateable:true,lazy:false,cache:false
        deleted column:"deleted",insertable:true,updateable:true,lazy:false,cache:false
        returnMaxDelay column:"return_max_delay",insertable:true,updateable:true,lazy:false,cache:false
        fullpath column:"fullpath",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        parent column:"parent_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        catalog column:"catalog_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ibeacon column:"ibeacon_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        name ( blank:false, nullable:false, unique:false)
        description (nullable:true, unique:false)
        keywords (nullable:true, unique:false)
        position ( blank:false, nullable:false, unique:false)
        hide ( blank:false, nullable:false, unique:false)
        sanitizedName (nullable:true, unique:false)
        googleCategory (nullable:true, unique:false)
        deleted ( blank:false, nullable:false, unique:false)
        returnMaxDelay ( blank:false, nullable:false, unique:false)
        fullpath (nullable:true, unique:false)
        company ( blank:false, nullable:false)
        parent (nullable:true)
        catalog (nullable:true)
        ibeacon (nullable:true)
    }


    String toString(){return categoryRender?.asString(this)}

    def beforeInsert = {
        categoryValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        categoryValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        categoryValidation.beforeDelete(this)
    }

    def beforeValidate = {
        categoryValidation.beforeValidate(this)
    }

    def afterInsert = {
        categoryValidation.afterInsert(this)
    }

    def afterUpdate = {
        categoryValidation.afterUpdate(this)
    }

    def afterDelete = {
        categoryValidation.afterDelete(this)
    }

    def onLoad = {
        categoryValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return categoryRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}