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
class IntraDayPeriod
    implements java.io.Serializable
{
    def intraDayPeriodValidation
    def intraDayPeriodRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.util.Calendar startDate 
    /**
     * <p>
     * endDate = 31/12/2049 means open ended
     * </p>
     */
    java.util.Calendar endDate 
    /**
     * 
     */
    java.lang.Boolean weekday1 
    /**
     * 
     */
    java.lang.Boolean weekday2 
    /**
     * 
     */
    java.lang.Boolean weekday3 
    /**
     * 
     */
    java.lang.Boolean weekday4 
    /**
     * 
     */
    java.lang.Boolean weekday5 
    /**
     * 
     */
    java.lang.Boolean weekday6 
    /**
     * 
     */
    java.lang.Boolean weekday7 
    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    static transients = [ 'intraDayPeriodValidation', 'intraDayPeriodRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'intra_day_period'

        version false

        id name:'id',column:'id',generator:'native'
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        endDate column:"end_date",insertable:true,updateable:true,lazy:false,cache:false
        weekday1 column:"weekday1",insertable:true,updateable:true,lazy:false,cache:false
        weekday2 column:"weekday2",insertable:true,updateable:true,lazy:false,cache:false
        weekday3 column:"weekday3",insertable:true,updateable:true,lazy:false,cache:false
        weekday4 column:"weekday4",insertable:true,updateable:true,lazy:false,cache:false
        weekday5 column:"weekday5",insertable:true,updateable:true,lazy:false,cache:false
        weekday6 column:"weekday6",insertable:true,updateable:true,lazy:false,cache:false
        weekday7 column:"weekday7",insertable:true,updateable:true,lazy:false,cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        startDate (nullable:true, unique:false)
        endDate (nullable:true, unique:false)
        weekday1 ( blank:false, nullable:false, unique:false)
        weekday2 ( blank:false, nullable:false, unique:false)
        weekday3 ( blank:false, nullable:false, unique:false)
        weekday4 ( blank:false, nullable:false, unique:false)
        weekday5 ( blank:false, nullable:false, unique:false)
        weekday6 ( blank:false, nullable:false, unique:false)
        weekday7 ( blank:false, nullable:false, unique:false)
        product (nullable:true)
    }

    static java.util.Collection findByProduct(final long idProduct)
    {
        return IntraDayPeriod.findByProduct("FROM IntraDayPeriod WHERE product.id = :idProduct ", idProduct, 0, 0);
    }

    static java.util.Collection findByProduct(final long idProduct, int pageNumber, int pageSize)
    {
        return IntraDayPeriod.findByProduct("FROM IntraDayPeriod WHERE product.id = :idProduct ", idProduct, pageNumber, pageSize);
    }

    static java.util.Collection findByProduct(final java.lang.String queryString, final long idProduct, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? IntraDayPeriod.executeQuery(queryString, [idProduct:idProduct], paginateParams) : IntraDayPeriod.executeQuery(queryString, [idProduct:idProduct])
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return intraDayPeriodRender?.asString(this)}

    def beforeInsert = {
        intraDayPeriodValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        intraDayPeriodValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        intraDayPeriodValidation.beforeDelete(this)
    }

    def beforeValidate = {
        intraDayPeriodValidation.beforeValidate(this)
    }

    def afterInsert = {
        intraDayPeriodValidation.afterInsert(this)
    }

    def afterUpdate = {
        intraDayPeriodValidation.afterUpdate(this)
    }

    def afterDelete = {
        intraDayPeriodValidation.afterDelete(this)
    }

    def onLoad = {
        intraDayPeriodValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return intraDayPeriodRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}