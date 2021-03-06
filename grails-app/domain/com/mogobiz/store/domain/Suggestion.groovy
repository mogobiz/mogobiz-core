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
class Suggestion
    implements java.io.Serializable
{
    def suggestionValidation
    def suggestionRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    boolean required  = false 
    /**
     * 
     */
    int position  = 0 
    /**
     * <p>
     * +10
     * </p>
     * <p>
     * -10
     * </p>
     * <p>
     * 10
     * </p>
     * <p>
     * %10%
     * </p>
     * <p>
     * -10%
     * </p>
     */
    java.lang.String discount  = "0" 
    /**
     * 
     */
    com.mogobiz.store.domain.Product pack 

    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    static transients = [ 'suggestionValidation', 'suggestionRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'suggestion'

        version false

        id name:'id',column:'id',generator:'native'
        required column:"required",insertable:true,updateable:true,lazy:false,cache:false
        position column:"position",insertable:true,updateable:true,lazy:false,cache:false
        discount column:"discount",insertable:true,updateable:true,lazy:false,cache:false


        pack column:"pack_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        required ( blank:false, nullable:false, unique:false)
        position ( blank:false, nullable:false, unique:false)
        discount (nullable:true, unique:false)
        pack ( blank:false, nullable:false)
        product ( blank:false, nullable:false)
    }

    static java.util.Collection findRequiredByPack(final long idProduit)
    {
        return Suggestion.findRequiredByPack("FROM Suggestion WHERE pack.id = :idProduit AND required = true", idProduit, 0, 0);
    }

    static java.util.Collection findRequiredByPack(final long idProduit, int pageNumber, int pageSize)
    {
        return Suggestion.findRequiredByPack("FROM Suggestion WHERE pack.id = :idProduit AND required = true", idProduit, pageNumber, pageSize);
    }

    static java.util.Collection findRequiredByPack(final java.lang.String queryString, final long idProduit, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? Suggestion.executeQuery(queryString, [idProduit:idProduit], paginateParams) : Suggestion.executeQuery(queryString, [idProduit:idProduit])
    }
    static com.mogobiz.store.domain.Suggestion findByPackAndProduct(final long idPack, final long idProduct)
    {
        return Suggestion.findByPackAndProduct("FROM Suggestion WHERE pack.id = :idPack AND product.id = :idProduct", idPack, idProduct);
    }

    static com.mogobiz.store.domain.Suggestion findByPackAndProduct(final java.lang.String queryString, final long idPack, final long idProduct)
    {
        def ret = Suggestion.executeQuery(queryString, [idPack:idPack,idProduct:idProduct]).iterator()
        return ret.hasNext()?ret.next():null
    }
    static com.mogobiz.store.domain.Suggestion findTransportByPack(final long idPack)
    {
        return Suggestion.findTransportByPack("FROM Suggestion WHERE pack.id = :idPack AND product.xtype = 'TRANSPORT'", idPack);
    }

    static com.mogobiz.store.domain.Suggestion findTransportByPack(final java.lang.String queryString, final long idPack)
    {
        def ret = Suggestion.executeQuery(queryString, [idPack:idPack]).iterator()
        return ret.hasNext()?ret.next():null
    }
    static com.mogobiz.store.domain.Suggestion findByPackAndTag(final long idPack, final java.lang.String identifiantTag)
    {
        return Suggestion.findByPackAndTag("FROM Suggestion WHERE pack.id = :idPack AND product.poi.name = :identifiantTag", idPack, identifiantTag);
    }

    static com.mogobiz.store.domain.Suggestion findByPackAndTag(final java.lang.String queryString, final long idPack, final java.lang.String identifiantTag)
    {
        def ret = Suggestion.executeQuery(queryString, [idPack:idPack,identifiantTag:identifiantTag]).iterator()
        return ret.hasNext()?ret.next():null
    }
    static com.mogobiz.store.domain.Suggestion findByPackAndPosition(final long idPack, final int position)
    {
        return Suggestion.findByPackAndPosition("FROM Suggestion WHERE pack.id = :idPack AND position = :position", idPack, position);
    }

    static com.mogobiz.store.domain.Suggestion findByPackAndPosition(final java.lang.String queryString, final long idPack, final int position)
    {
        def ret = Suggestion.executeQuery(queryString, [idPack:idPack,position:position]).iterator()
        return ret.hasNext()?ret.next():null
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return suggestionRender?.asString(this)}

    def beforeInsert = {
        suggestionValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        suggestionValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        suggestionValidation.beforeDelete(this)
    }

    def beforeValidate = {
        suggestionValidation.beforeValidate(this)
    }

    def afterInsert = {
        suggestionValidation.afterInsert(this)
    }

    def afterUpdate = {
        suggestionValidation.afterUpdate(this)
    }

    def afterDelete = {
        suggestionValidation.afterDelete(this)
    }

    def onLoad = {
        suggestionValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return suggestionRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}