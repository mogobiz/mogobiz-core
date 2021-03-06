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
class TicketType
    implements java.io.Serializable
{
    def ticketTypeValidation
    def ticketTypeRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     *
     */
    java.lang.String sku
    /**
     *
     */
    java.lang.String externalCode
    /**
     * <p>
     * Montant unitaire du SKU
     * </p>
     */
    long price
    /**
     * <p>
     * Quantité minimum lors de l'achat
     * </p>
     */
    int minOrder  = 1
    /**
     * <p>
     * Quantité maximum lors de l'achat (-1 = infini)
     * </p>
     */
    int maxOrder  = -1
    /**
     *
     */
    long nbSales  = 0
    /**
     * <p>
     * Période de mise en vente de ce type de ticket
     * </p>
     */
    java.util.Calendar startDate
    /**
     * <p>
     * Période de mise en vente de ce type de ticket
     * </p>
     */
    java.util.Calendar stopDate
    /**
     * <p>
     * privée, non disponible à la vente
     * </p>
     */
    java.lang.Boolean xprivate
    /**
     *
     */
    java.lang.String name     /**
     * <p>
     * quantité de ticket type disponible à la vente
     * </p>
     */
    com.mogobiz.store.domain.Stock stock

    /**
     *
     */
    java.lang.String description
    /**
     *
     */
    java.lang.Integer position
    /**
     * <p>
     * Global Trade Item Numbers (GTINs) submitted through the 'gtin'
     * attribute. GTINs include UPC, EAN (in Europe), JAN (in Japan),
     * and ISBN
     * </p>
     */
    java.lang.String gtin
    /**
     * <p>
     * Manufacturer Part Number (MPN) submitted through the 'mpn'
     * attribute
     * </p>
     */
    java.lang.String mpn
    /**
     *
     */
    java.util.Calendar availabilityDate
    /**
     *
     */
    java.lang.String filename
    /**
     *
     */
    java.lang.Boolean available
    /**
     *
     */
    java.lang.String byDateTimes
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
    com.mogobiz.store.domain.MiraklSyncStatus miraklProductStatus
    /**
     *
     */
    java.lang.String miraklProductTrackingId
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
    com.mogobiz.store.domain.VariationValue variation1

    /**
     *
     */
    com.mogobiz.store.domain.VariationValue variation2

    /**
     *
     */
    com.mogobiz.store.domain.Product product

    /**
     *
     */
    com.mogobiz.store.domain.VariationValue variation3
    /**
     *
     */
    com.mogobiz.store.domain.Resource picture

    static embedded = [  'stock' ]

    static transients = [ 'ticketTypeValidation', 'ticketTypeRender' ]

    static belongsTo = [product: Product]

    static hasMany = [stockCalendars: StockCalendar]

    static mappedBy = [stockCalendars: "ticketType"]

    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'ticket_type'

        version false

        id name:'id',column:'id',generator:'native'
        sku column:"sku",insertable:true,updateable:true,lazy:false,cache:false
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false
        minOrder column:"min_order",insertable:true,updateable:true,lazy:false,cache:false
        maxOrder column:"max_order",insertable:true,updateable:true,lazy:false,cache:false
        nbSales column:"nb_sales",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        stopDate column:"stop_date",insertable:true,updateable:true,lazy:false,cache:false
        xprivate column:"xprivate",insertable:true,updateable:true,lazy:false,cache:false
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        stock column:"stock",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false
        gtin column:"gtin",insertable:true,updateable:true,lazy:false,cache:false
        mpn column:"mpn",insertable:true,updateable:true,lazy:false,cache:false
        availabilityDate column:"availability_date",insertable:true,updateable:true,lazy:false,cache:false
        filename column:"filename",insertable:true,updateable:true,lazy:false,cache:false
        available column:"available",insertable:true,updateable:true,lazy:false,cache:false
        byDateTimes column:"by_date_times",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        publishable column:"publishable",insertable:true,updateable:true,lazy:false,cache:false
        miraklStatus column:"mirakl_status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        miraklTrackingId column:"mirakl_tracking_id",insertable:true,updateable:true,lazy:false,cache:false
        miraklProductStatus column:"mirakl_product_status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        miraklProductTrackingId column:"mirakl_product_tracking_id",insertable:true,updateable:true,lazy:false,cache:false


        variation1 column:"variation1_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        variation2 column:"variation2_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        variation3 column:"variation3_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        picture column:"picture_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        sku ( blank:false, nullable:false, unique:false)
        externalCode (nullable:true, unique:false)
        price ( blank:false, nullable:false, unique:false)
        minOrder ( blank:false, nullable:false, unique:false)
        maxOrder ( blank:false, nullable:false, unique:false)
        nbSales ( blank:false, nullable:false, unique:false)
        startDate (nullable:true, unique:false)
        stopDate (nullable:true, unique:false)
        xprivate (nullable:true, unique:false)
        name ( blank:false, nullable:false, unique:false)
        stock (nullable:true, unique:false)
        description (nullable:true, unique:false)
        position (nullable:true, unique:false)
        gtin (nullable:true, unique:false)
        mpn (nullable:true, unique:false)
        availabilityDate (nullable:true, unique:false)
        filename (nullable:true, unique:false)
        available (nullable:true, unique:false)
        byDateTimes (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        publishable (nullable:true, unique:false)
        miraklStatus (nullable:true, unique:false)
        miraklTrackingId (nullable:true, unique:false)
        variation1 (nullable:true)
        variation2 (nullable:true)
        product ( blank:false, nullable:false)
        variation3 (nullable:true)
        picture (nullable:true)
        miraklProductStatus (nullable:true, unique:false)
        miraklProductTrackingId (nullable:true, unique:false)
    }

    static java.util.Collection findSalableByProduct(final long idProduct)
    {
        return TicketType.findSalableByProduct("FROM TicketType WHERE product.id = :idProduct and (startDate is null OR startDate <= current_date()) and (stopDate is null OR current_date() <= stopDate)", idProduct, 0, 0);
    }

    static java.util.Collection findSalableByProduct(final long idProduct, int pageNumber, int pageSize)
    {
        return TicketType.findSalableByProduct("FROM TicketType WHERE product.id = :idProduct and (startDate is null OR startDate <= current_date()) and (stopDate is null OR current_date() <= stopDate)", idProduct, pageNumber, pageSize);
    }

    static java.util.Collection findSalableByProduct(final java.lang.String queryString, final long idProduct, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? TicketType.executeQuery(queryString, [idProduct:idProduct], paginateParams) : TicketType.executeQuery(queryString, [idProduct:idProduct])
    }
    static com.mogobiz.store.domain.TicketType findByProductAndName(final long idProduct, final java.lang.String name)
    {
        return TicketType.findByProductAndName("FROM TicketType WHERE product.id = :idProduct and name = :name", idProduct, name);
    }

    static com.mogobiz.store.domain.TicketType findByProductAndName(final java.lang.String queryString, final long idProduct, final java.lang.String name)
    {
        def ret = TicketType.executeQuery(queryString, [idProduct:idProduct,name:name]).iterator()
        return ret.hasNext()?ret.next():null
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }


    String toString(){return ticketTypeRender?.asString(this)}

    def beforeInsert = {
        ticketTypeValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        ticketTypeValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        ticketTypeValidation.beforeDelete(this)
    }

    def beforeValidate = {
        ticketTypeValidation.beforeValidate(this)
    }

    def afterInsert = {
        ticketTypeValidation.afterInsert(this)
    }

    def afterUpdate = {
        ticketTypeValidation.afterUpdate(this)
    }

    def afterDelete = {
        ticketTypeValidation.afterDelete(this)
    }

    def onLoad = {
        ticketTypeValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return ticketTypeRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
