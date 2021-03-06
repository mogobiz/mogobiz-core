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
class Company
    implements java.io.Serializable
{
    def companyValidation
    def companyRender

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
    java.lang.String externalCode 
    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    java.lang.String email 
    /**
     * 
     */
    java.lang.String website 
    /**
     * 
     */
    java.lang.String phone 
    /**
     * 
     */
    java.lang.String apiKey 
    /**
     * 
     */
    java.util.Calendar startDate 
    /**
     * 
     */
    java.util.Calendar stopDate 
    /**
     * <p>
     * The default currency of the company
     * </p>
     */
    java.lang.String currencyCode  = "EUR" 
    /**
     * <p>
     * The default country of the company (use for retreive tax)
     * </p>
     */
    java.lang.String countryCode  = "FR"     /**
     * 
     */
    com.mogobiz.store.domain.ShippingCarriers shippingCarriers

    /**
     * 
     */
    com.mogobiz.store.domain.WeightUnit weightUnit 
    /**
     * <p>
     * 1 Business Day
     * </p>
     * <p>
     * 2 Business Day
     * </p>
     * <p>
     * ...
     * </p>
     */
    java.lang.Integer handlingTime  = java.lang.Integer.valueOf(1) 
    /**
     * <p>
     * Return accepted within 7 days
     * </p>
     * <p>
     * Return accepted within 14 days
     * </p>
     * <p>
     * A value of 0 means No return accepted
     * </p>
     */
    java.lang.Integer returnPolicy  = java.lang.Integer.valueOf(0) 
    /**
     * 
     */
    com.mogobiz.store.domain.RefundPolicy refundPolicy 
    /**
     * 
     */
    boolean shippingInternational  = false 
    /**
     * 
     */
    java.lang.String tempSessionId 
    /**
     * 
     */
    java.lang.String aesPassword 
    /**
     * 
     */
    java.lang.Boolean onlineValidation  = java.lang.Boolean.valueOf(true) 
    /**
     * 
     */
    java.lang.String gakey 
    /**
     * 
     */
    com.mogobiz.store.domain.MapProvider mapProvider 
    /**
     * 
     */
    java.lang.String defaultLanguage  = "fr"     /**
     * 
     */
    com.mogobiz.geolocation.domain.Location shipFrom
    /**
     * 
     */
    com.mogobiz.geolocation.domain.Location location
    /**
     * 
     */
    com.mogobiz.store.domain.GoogleContent googleContent
    /**
     * 
     */
    com.mogobiz.store.domain.GoogleEnv googleEnv

    static embedded = [  'shippingCarriers' ]

    static transients = [ 'companyValidation', 'companyRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'company'

        version false

        id name:'id',column:'id',generator:'native'
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        email column:"email",insertable:true,updateable:true,lazy:false,cache:false
        website column:"website",insertable:true,updateable:true,lazy:false,cache:false
        phone column:"phone",insertable:true,updateable:true,lazy:false,cache:false
        apiKey column:"api_key",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        stopDate column:"stop_date",insertable:true,updateable:true,lazy:false,cache:false
        currencyCode column:"currency_code",insertable:true,updateable:true,lazy:false,cache:false
        countryCode column:"country_code",insertable:true,updateable:true,lazy:false,cache:false
        shippingCarriers column:"shipping_carriers",insertable:true,updateable:true,lazy:false,cache:false
        weightUnit column:"weight_unit",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        handlingTime column:"handling_time",insertable:true,updateable:true,lazy:false,cache:false
        returnPolicy column:"return_policy",insertable:true,updateable:true,lazy:false,cache:false
        refundPolicy column:"refund_policy",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        shippingInternational column:"shipping_international",insertable:true,updateable:true,lazy:false,cache:false
        tempSessionId column:"temp_session_id",insertable:true,updateable:true,lazy:false,cache:false
        aesPassword column:"aes_password",insertable:true,updateable:true,lazy:false,cache:false
        onlineValidation column:"online_validation",insertable:true,updateable:true,lazy:false,cache:false
        gakey column:"gakey",insertable:true,updateable:true,lazy:false,cache:false
        mapProvider column:"map_provider",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        defaultLanguage column:"default_language",insertable:true,updateable:true,lazy:false,cache:false


        shipFrom column:"ship_from_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'

        location column:"location_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'

        googleContent column:"google_content_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'

        googleEnv column:"google_env_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        code ( blank:false, nullable:false, unique:false)
        externalCode (nullable:true, unique:false)
        name ( blank:false, nullable:false, unique:false)
        email (nullable:true, unique:false, email:true)
        website (nullable:true, unique:false, url:true)
        phone (nullable:true, unique:false)
        apiKey (nullable:true, unique:false)
        startDate (nullable:true, unique:false)
        stopDate (nullable:true, unique:false)
        currencyCode (nullable:true, unique:false)
        countryCode (nullable:true, unique:false)
        shippingCarriers (nullable:true, unique:false)
        weightUnit (nullable:true, unique:false)
        handlingTime (nullable:true, unique:false)
        returnPolicy (nullable:true, unique:false)
        refundPolicy (nullable:true, unique:false)
        shippingInternational (nullable:true, unique:false)
        tempSessionId (nullable:true, unique:false)
        aesPassword ( blank:false, nullable:false, unique:false)
        onlineValidation ( blank:false, nullable:false, unique:false)
        gakey (nullable:true, unique:false)
        mapProvider (nullable:true, unique:false)
        defaultLanguage ( blank:false, nullable:false, unique:false)
        shipFrom (nullable:true)
        location (nullable:true)
        googleContent (nullable:true)
        googleEnv (nullable:true)
    }

    static com.mogobiz.store.domain.Company findByName(final java.lang.String name)
    {
        return Company.findByName("from com.mogobiz.store.domain.Company as company where company.name = :name", name);
    }

    static com.mogobiz.store.domain.Company findByName(final java.lang.String queryString, final java.lang.String name)
    {
        def ret = Company.executeQuery(queryString, [name:name]).iterator()
        return ret.hasNext()?ret.next():null
    }
    static com.mogobiz.store.domain.Company findByWebsite(final java.lang.String website)
    {
        return Company.findByWebsite("from com.mogobiz.store.domain.Company as company where company.website = :website", website);
    }

    static com.mogobiz.store.domain.Company findByWebsite(final java.lang.String queryString, final java.lang.String website)
    {
        def ret = Company.executeQuery(queryString, [website:website]).iterator()
        return ret.hasNext()?ret.next():null
    }
    static com.mogobiz.store.domain.Company findByKey(final java.lang.String key)
    {
        return Company.findByKey("from com.mogobiz.store.domain.Company as company where company.key = :key", key);
    }

    static com.mogobiz.store.domain.Company findByKey(final java.lang.String queryString, final java.lang.String key)
    {
        def ret = Company.executeQuery(queryString, [key:key]).iterator()
        return ret.hasNext()?ret.next():null
    }

    String toString(){return companyRender?.asString(this)}

    def beforeInsert = {
        companyValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        companyValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        companyValidation.beforeDelete(this)
    }

    def beforeValidate = {
        companyValidation.beforeValidate(this)
    }

    def afterInsert = {
        companyValidation.afterInsert(this)
    }

    def afterUpdate = {
        companyValidation.afterUpdate(this)
    }

    def afterDelete = {
        companyValidation.afterDelete(this)
    }

    def onLoad = {
        companyValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return companyRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}