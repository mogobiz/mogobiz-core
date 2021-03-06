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
class DatePeriod
    implements java.io.Serializable
{
    def datePeriodValidation
    def datePeriodRender

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
    com.mogobiz.store.domain.Product product 

    static transients = [ 'datePeriodValidation', 'datePeriodRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'date_period'

        version false

        id name:'id',column:'id',generator:'native'
        startDate column:"start_date",insertable:true,updateable:true,lazy:false,cache:false
        endDate column:"end_date",insertable:true,updateable:true,lazy:false,cache:false


        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        startDate ( blank:false, nullable:false, unique:false)
        endDate ( blank:false, nullable:false, unique:false)
        product (nullable:true)
    }

    static java.util.Collection findByProductAndDate(final long idProduct, final java.util.Calendar date)
    {
        return DatePeriod.findByProductAndDate("FROM DatePeriod WHERE product.id = :idProduct AND startDate <= :date AND :date <= endDate", idProduct, date, 0, 0);
    }

    static java.util.Collection findByProductAndDate(final long idProduct, final java.util.Calendar date, int pageNumber, int pageSize)
    {
        return DatePeriod.findByProductAndDate("FROM DatePeriod WHERE product.id = :idProduct AND startDate <= :date AND :date <= endDate", idProduct, date, pageNumber, pageSize);
    }

    static java.util.Collection findByProductAndDate(final java.lang.String queryString, final long idProduct, final java.util.Calendar date, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? DatePeriod.executeQuery(queryString, [idProduct:idProduct,date:date], paginateParams) : DatePeriod.executeQuery(queryString, [idProduct:idProduct,date:date])
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return datePeriodRender?.asString(this)}

    def beforeInsert = {
        datePeriodValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        datePeriodValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        datePeriodValidation.beforeDelete(this)
    }

    def beforeValidate = {
        datePeriodValidation.beforeValidate(this)
    }

    def afterInsert = {
        datePeriodValidation.afterInsert(this)
    }

    def afterUpdate = {
        datePeriodValidation.afterUpdate(this)
    }

    def afterDelete = {
        datePeriodValidation.afterDelete(this)
    }

    def onLoad = {
        datePeriodValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return datePeriodRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}