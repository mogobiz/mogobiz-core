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
class Product
    implements java.io.Serializable
{
    def productValidation
    def productRender

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
    java.lang.String code 
    /**
     * 
     */
    java.lang.String name 
    /**
     * 
     */
    com.mogobiz.store.domain.ProductType xtype 
    /**
     * <p>
     * Montant de base du produit (en centime)
     * </p>
     */
    long price 
    /**
     * 
     */
    com.mogobiz.store.domain.ProductState state 
    /**
     * <p>
     * Description détaillée du produit
     * </p>
     */
    java.lang.String description 
    /**
     * 
     */
    java.lang.String descriptionAsText 
    /**
     * 
     */
    long nbSales  = 0 
    /**
     * <p>
     * Url of the first picture associated to this product.
     * </p>
     */
    java.lang.String picture 
    /**
     * 
     */
    java.lang.Boolean stockDisplay  = java.lang.Boolean.valueOf(true) 
    /**
     * 
     */
    com.mogobiz.store.domain.ProductCalendar calendarType 
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
    java.util.Calendar modificationDate 
    /**
     * 
     */
    java.util.Calendar stopFeatureDate 
    /**
     * 
     */
    java.util.Calendar startFeatureDate 
    /**
     * 
     */
    java.lang.String keywords 
    /**
     * 
     */
    java.util.Calendar availabilityDate 
    /**
     * 
     */
    boolean deleted  = false 
    /**
     * 
     */
    long downloadMaxTimes  = 0 
    /**
     * 
     */
    long downloadMaxDelay  = 0 
    /**
     * 
     */
    long returnMaxDelay  = 0 
    /**
     * 
     */
    java.lang.Boolean stockAvailable 
    /**
     * 
     */
    java.lang.String i18n 
    /**
     * 
     */
    java.lang.Boolean publishable 
    /**
     * 
     */
    com.mogobiz.store.domain.MiraklSyncStatus miraklStatus 
    /**
     * 
     */
    java.lang.String miraklTrackingId 
    /**
     * 
     */
    java.util.Calendar stopDate 
    /**
     * 
     */
    java.util.Calendar startDate 
    /**
     * 
     */
    com.mogobiz.geolocation.domain.Poi poi 

    /**
     * 
     */
    com.mogobiz.store.domain.Category category 

    /**
     * 
     */
    com.mogobiz.store.domain.Brand brand 

    /**
     * 
     */
    com.mogobiz.store.domain.Company company 

    /**
     * 
     */
    com.mogobiz.store.domain.Seller seller 
    /**
     * 
     */
    com.mogobiz.store.domain.Shipping shipping

    /**
     * 
     */
    com.mogobiz.store.domain.TaxRate taxRate 

    /**
     * 
     */
    com.mogobiz.store.domain.Ibeacon ibeacon

    static hasMany = [
            tags:Tag,
            product2Resources:Product2Resource,
            ticketTypes: TicketType ,
            features: Feature,
            featureValues: FeatureValue,
            productProperties: ProductProperty,
            datePeriods: DatePeriod,
            intraDayPeriods: IntraDayPeriod
    ]

    static mappedBy = [
            ticketTypes: "product",
            features: "product",
            featureValues: "product",
            productProperties: "product",
            product2Resources: "product",
            datePeriods: "product",
            intraDayPeriods: "product"
    ]

    static transients = [ 'productValidation', 'productRender' ]


    static mapping = {

        autoTimestamp true

        uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy true

        discriminator value:'ProductImpl', column:[name:'class',length:255]
        table 'product'

        version false

        id name:'id',column:'id',generator:'native'
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        xtype column:"xtype",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false
        state column:"state",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        descriptionAsText column:"description_as_text",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        nbSales column:"nb_sales",insertable:true,updateable:true,lazy:false,cache:false
        picture column:"picture",insertable:true,updateable:true,lazy:false,cache:false
        stockDisplay column:"stock_display",insertable:true,updateable:true,lazy:false,cache:false
        calendarType column:"calendar_type",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        hide column:"hide",insertable:true,updateable:true,lazy:false,cache:false
        sanitizedName column:"sanitized_name",insertable:true,updateable:true,lazy:false,cache:false
        modificationDate column:"modification_date",insertable:true,updateable:true,lazy:false,cache:false
        stopFeatureDate column:"stop_feature_date",insertable:true,updateable:true,lazy:false,cache:false
        startFeatureDate column:"start_feature_date",insertable:true,updateable:true,lazy:false,cache:false
        keywords column:"keywords",insertable:true,updateable:true,lazy:false,cache:false
        availabilityDate column:"availability_date",insertable:true,updateable:true,lazy:false,cache:false
        deleted column:"deleted",insertable:true,updateable:true,lazy:false,cache:false
        downloadMaxTimes column:"download_max_times",insertable:true,updateable:true,lazy:false,cache:false
        downloadMaxDelay column:"download_max_delay",insertable:true,updateable:true,lazy:false,cache:false
        returnMaxDelay column:"return_max_delay",insertable:true,updateable:true,lazy:false,cache:false
        stockAvailable column:"stock_available",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        publishable column:"publishable",insertable:true,updateable:true,lazy:false,cache:false
        miraklStatus column:"mirakl_status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        miraklTrackingId column:"mirakl_tracking_id",insertable:true,updateable:true,lazy:false,cache:false
        stopDate column:"stop_date",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false


        poi column:"poi_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        category column:"category_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        brand column:"brand_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        tags column:"tags_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        seller column:"seller_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product2Resources column:"product2_resources_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        shipping column:"shipping_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'

        taxRate column:"tax_rate_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ibeacon column:"ibeacon_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        externalCode (nullable:true, unique:false)
        code ( blank:false, nullable:false, unique:false, validator:com.mogobiz.store.domain.ProductValidation.productCodeValidator)
        name ( blank:false, nullable:false, unique:false)
        xtype ( blank:false, nullable:false, unique:false)
        price ( blank:false, nullable:false, unique:false)
        state (nullable:true, unique:false)
        description (nullable:true, unique:false)
        descriptionAsText (nullable:true, unique:false)
        nbSales ( blank:false, nullable:false, unique:false)
        picture (nullable:true, unique:false)
        stockDisplay (nullable:true, unique:false)
        calendarType (nullable:true, unique:false)
        hide ( blank:false, nullable:false, unique:false)
        sanitizedName ( blank:false, nullable:false, unique:false)
        modificationDate (nullable:true, unique:false)
        stopFeatureDate (nullable:true, unique:false)
        startFeatureDate (nullable:true, unique:false)
        keywords (nullable:true, unique:false)
        availabilityDate (nullable:true, unique:false)
        deleted ( blank:false, nullable:false, unique:false)
        downloadMaxTimes (nullable:true, unique:false)
        downloadMaxDelay (nullable:true, unique:false)
        returnMaxDelay ( blank:false, nullable:false, unique:false)
        stockAvailable (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        publishable (nullable:true, unique:false)
        miraklStatus (nullable:true, unique:false)
        miraklTrackingId (nullable:true, unique:false)
        stopDate (nullable:true, unique:false)
        startDate (nullable:true, unique:false)
        poi (nullable:true)
        category ( blank:false, nullable:false)
        brand (nullable:true)
        company ( blank:false, nullable:false)
        seller (nullable:true)
        shipping (nullable:true)
        taxRate (nullable:true)
        ibeacon (nullable:true)
    }


    String toString(){return productRender?.asString(this)}

    def beforeInsert = {
        productValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        productValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        productValidation.beforeDelete(this)
    }

    def beforeValidate = {
        productValidation.beforeValidate(this)
    }

    def afterInsert = {
        productValidation.afterInsert(this)
    }

    def afterUpdate = {
        productValidation.afterUpdate(this)
    }

    def afterDelete = {
        productValidation.afterDelete(this)
    }

    def onLoad = {
        productValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return productRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}