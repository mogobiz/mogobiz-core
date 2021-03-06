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
class Coupon
    implements java.io.Serializable
{
    def couponValidation
    def couponRender

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
    java.lang.String code
    /**
     *
     */
    boolean active  = true
    /**
     * <p>
     * Number of uses coupons. null means unlimited
     * </p>
     */
    java.lang.Long numberOfUses
    /**
     *
     */
    java.util.Calendar startDate
    /**
     *
     */
    java.util.Calendar endDate
    /**
     *
     */
    boolean catalogWise  = false
    /**
     *
     */
    boolean forSale  = false
    /**
     *
     */
    java.lang.String description
    /**
     *
     */
    java.lang.Boolean anonymous  = java.lang.Boolean.valueOf(false)
    /**
     *
     */
    java.lang.String pastille
    /**
     *
     */
    long consumed  = 0
    /**
     *
     */
    java.lang.String i18n
    /**
     *
     */
    com.mogobiz.store.domain.Company company
    /**
     *
     */
    java.lang.String externalCode

    static hasMany = [ categories:com.mogobiz.store.domain.Category , products:com.mogobiz.store.domain.Product , ticketTypes:com.mogobiz.store.domain.TicketType ,  rules:com.mogobiz.store.domain.ReductionRule , catalogs:com.mogobiz.store.domain.Catalog ]

    static transients = [ 'couponValidation', 'couponRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'coupon'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        code column:"code",insertable:true,updateable:true,lazy:false,cache:false
        active column:"active",insertable:true,updateable:true,lazy:false,cache:false
        numberOfUses column:"number_of_uses",insertable:true,updateable:true,lazy:false,cache:false
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        endDate column:"end_date",insertable:true,updateable:true,lazy:false,cache:false
        catalogWise column:"catalog_wise",insertable:true,updateable:true,lazy:false,cache:false
        forSale column:"for_sale",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        anonymous column:"anonymous",insertable:true,updateable:true,lazy:false,cache:false
        pastille column:"pastille",insertable:true,updateable:true,lazy:false,cache:false
        consumed column:"consumed",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        externalCode column:"external_code",insertable:true,updateable:true,lazy:false,cache:false


        categories column:"categories_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        products column:"products_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        ticketTypes column:"ticket_types_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        rules column:"rules_fk",cascade :'delete',insertable:true,updateable:true,lazy:true,cache:'read-write'

        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        catalogs column:"catalogs_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        name ( blank:false, nullable:false, unique:false)
        code ( blank:false, nullable:false, unique:false, validator:com.mogobiz.store.domain.CouponValidation.couponCodeValidator)
        active ( blank:false, nullable:false, unique:false)
        numberOfUses (nullable:true, unique:false)
        startDate (nullable:true, unique:false)
        endDate (nullable:true, unique:false)
        catalogWise ( blank:false, nullable:false, unique:false)
        forSale ( blank:false, nullable:false, unique:false)
        description (nullable:true, unique:false)
        anonymous ( blank:false, nullable:false, unique:false)
        pastille (nullable:true, unique:false)
        consumed ( blank:false, nullable:false, unique:false)
        i18n (nullable:true, unique:false)
        company ( blank:false, nullable:false)
        externalCode (nullable:true, unique:false)
    }


    String toString(){return couponRender?.asString(this)}

    def beforeInsert = {
        couponValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        couponValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        couponValidation.beforeDelete(this)
    }

    def beforeValidate = {
        couponValidation.beforeValidate(this)
    }

    def afterInsert = {
        couponValidation.afterInsert(this)
    }

    def afterUpdate = {
        couponValidation.afterUpdate(this)
    }

    def afterDelete = {
        couponValidation.afterDelete(this)
    }

    def onLoad = {
        couponValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return couponRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}
