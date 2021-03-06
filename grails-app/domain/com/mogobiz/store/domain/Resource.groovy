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
class Resource
    implements java.io.Serializable
{
    def resourceValidation
    def resourceRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.String code 
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
    com.mogobiz.store.domain.ResourceType xtype 
    /**
     * 
     */
    java.lang.String url 
    /**
     * 
     */
    java.lang.String content 
    /**
     * 
     */
    boolean active  = false 
    /**
     * 
     */
    boolean deleted  = false 
    /**
     * 
     */
    boolean uploaded  = false 
    /**
     * 
     */
    java.lang.String contentType 
    /**
     * 
     */
    com.mogobiz.store.domain.ResourceAccountType accountType 
    /**
     * 
     */
    java.lang.String smallPicture 
    /**
     * 
     */
    java.lang.String externalCode 
    /**
     * 
     */
    java.lang.String sanitizedName 
    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    /**
     * 
     */
    com.mogobiz.geolocation.domain.Poi poi 

    /**
     * 
     */
    com.mogobiz.store.domain.Album album 

    static hasMany = [  product2Resources:com.mogobiz.store.domain.Product2Resource ]

    static transients = [ 'resourceValidation', 'resourceRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy true

        discriminator value:'ResourceImpl', column:[name:'class',length:255]
        table 'xresource'

        version false

        id name:'id',column:'id',generator:'native'
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        xtype column:"xtype",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        url column:"url",insertable:true,updateable:true,lazy:false,cache:false
        content column:"content",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        deleted column:"deleted",insertable:true,updateable:true,lazy:false,cache:false
        uploaded column:"uploaded",insertable:true,updateable:true,lazy:false,cache:false
        contentType column:"content_type",insertable:true,updateable:true,lazy:false,cache:false
        accountType column:"account_type",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        smallPicture column:"small_picture",insertable:true,updateable:true,lazy:false,cache:false
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        sanitizedName column:"sanitized_name",insertable:true,updateable:true,lazy:false,cache:false


        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product2Resources column:"product2_resources_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        poi column:"poi_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        album column:"album_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        code (nullable:true, unique:false)
        name (nullable:true, unique:false)
        description (nullable:true, unique:false)
        xtype ( blank:false, nullable:false, unique:false)
        url (nullable:true, unique:false)
        content (nullable:true, unique:false)
        active ( blank:false, nullable:false, unique:false)
        deleted ( blank:false, nullable:false, unique:false)
        uploaded ( blank:false, nullable:false, unique:false)
        contentType (nullable:true, unique:false)
        accountType (nullable:true, unique:false)
        smallPicture (nullable:true, unique:false)
        externalCode (nullable:true, unique:false)
        sanitizedName ( blank:false, nullable:false, unique:false)
        company (nullable:true)
        poi (nullable:true)
        album (nullable:true)
    }


    String toString(){return resourceRender?.asString(this)}

    def beforeInsert = {
        resourceValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        resourceValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        resourceValidation.beforeDelete(this)
    }

    def beforeValidate = {
        resourceValidation.beforeValidate(this)
    }

    def afterInsert = {
        resourceValidation.afterInsert(this)
    }

    def afterUpdate = {
        resourceValidation.afterUpdate(this)
    }

    def afterDelete = {
        resourceValidation.afterDelete(this)
    }

    def onLoad = {
        resourceValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return resourceRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}