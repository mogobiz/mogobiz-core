/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.geolocation.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class Poi
    extends com.mogobiz.geolocation.domain.Location
    implements java.io.Serializable
{
    def poiValidation
    def poiRender






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
    com.mogobiz.geolocation.domain.VisibilityType visibility 
    /**
     * 
     */
    java.lang.String picture 
    /**
     * 
     */
    java.lang.String video 
    /**
     * 
     */
    boolean isMain  = false 
    /**
     * 
     */
    java.lang.String pictureType 
    /**
     * 
     */
    java.lang.String externalId 
    /**
     * 
     */
    java.lang.String source 
    /**
     * 
     */
    java.lang.String detail 
    /**
     * 
     */
    java.lang.Float maxPrice 
    /**
     * 
     */
    java.lang.Float minPrice 
    /**
     * 
     */
    java.lang.String i18n 
    /**
     * 
     */
    com.mogobiz.geolocation.domain.PoiType poiType 

    static transients = [ 'poiValidation', 'poiRender' ]


    static mapping = {







        cache usage:'read-write'

        discriminator value:'POI'
        table 'poi'

        version false

        id name:'id',column:'id',generator:'native'
        name column:"name",insertable:true,updateable:true,lazy:false,cache:false
        description column:"description",insertable:true,updateable:true,lazy:false,cache:false
        visibility column:"visibility",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false
        picture column:"picture",insertable:true,updateable:true,lazy:false,cache:false
        video column:"video",insertable:true,updateable:true,lazy:false,cache:false
        isMain column:"is_main",insertable:true,updateable:true,lazy:false,cache:false
        pictureType column:"picture_type",insertable:true,updateable:true,lazy:false,cache:false
        externalId column:"external_id",insertable:true,updateable:true,lazy:false,cache:false
        source column:"source",insertable:true,updateable:true,lazy:false,cache:false
        detail column:"detail",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        maxPrice column:"max_price",insertable:true,updateable:true,lazy:false,cache:false
        minPrice column:"min_price",insertable:true,updateable:true,lazy:false,cache:false
        i18n column:"i18n",insertable:true,updateable:true,lazy:false,type:"text",cache:false

        poiType column:"poi_type_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {


        name (nullable:true, unique:false)
        description (nullable:true, unique:false)
        visibility (nullable:true, unique:false)
        picture (nullable:true, unique:false)
        video (nullable:true, unique:false)
        isMain (nullable:true, unique:false)
        pictureType (nullable:true, unique:false)
        externalId (nullable:true, unique:false)
        source (nullable:true, unique:false)
        detail (nullable:true, unique:false)
        maxPrice (nullable:true, unique:false)
        minPrice (nullable:true, unique:false)
        i18n (nullable:true, unique:false)
        poiType (nullable:true)
    }

    static java.util.Collection findLocationsOfPack(final java.lang.String codePack)
    {
        return Poi.findLocationsOfPack("SELECT DISTINCT poi FROM Poi poi WHERE poi.latitude IS NOT NULL AND poi.longitude IS NOT NULL AND ( poi in (SELECT pack.poi FROM Product as pack WHERE pack.code = :codePack AND pack.poi is not null) OR poi in (SELECT sug.product.poi FROM Suggestion as sug WHERE sug.pack.code = :codePack AND sug.product.poi is not null) )", codePack, 0, 0);
    }

    static java.util.Collection findLocationsOfPack(final java.lang.String codePack, int pageNumber, int pageSize)
    {
        return Poi.findLocationsOfPack("SELECT DISTINCT poi FROM Poi poi WHERE poi.latitude IS NOT NULL AND poi.longitude IS NOT NULL AND ( poi in (SELECT pack.poi FROM Product as pack WHERE pack.code = :codePack AND pack.poi is not null) OR poi in (SELECT sug.product.poi FROM Suggestion as sug WHERE sug.pack.code = :codePack AND sug.product.poi is not null) )", codePack, pageNumber, pageSize);
    }

    static java.util.Collection findLocationsOfPack(final java.lang.String queryString, final java.lang.String codePack, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? Poi.executeQuery(queryString, [codePack:codePack], paginateParams) : Poi.executeQuery(queryString, [codePack:codePack])
    }
    static java.util.Collection findLocationsAround(final double x, final double y, final int rayon)
    {
        return Poi.findLocationsAround("SELECT poi FROM Poi poi where evaluate_earth_distance(:x, :y, poi.latitude, poi.longitude) <= :rayon", x, y, rayon, 0, 0);
    }

    static java.util.Collection findLocationsAround(final double x, final double y, final int rayon, int pageNumber, int pageSize)
    {
        return Poi.findLocationsAround("SELECT poi FROM Poi poi where evaluate_earth_distance(:x, :y, poi.latitude, poi.longitude) <= :rayon", x, y, rayon, pageNumber, pageSize);
    }

    static java.util.Collection findLocationsAround(final java.lang.String queryString, final double x, final double y, final int rayon, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? Poi.executeQuery(queryString, [x:x,y:y,rayon:rayon], paginateParams) : Poi.executeQuery(queryString, [x:x,y:y,rayon:rayon])
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return poiRender?.asString(this)}

    def beforeInsert = {
        poiValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        poiValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        poiValidation.beforeDelete(this)
    }

    def beforeValidate = {
        poiValidation.beforeValidate(this)
    }

    def afterInsert = {
        poiValidation.afterInsert(this)
    }

    def afterUpdate = {
        poiValidation.afterUpdate(this)
    }

    def afterDelete = {
        poiValidation.afterDelete(this)
    }

    def onLoad = {
        poiValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return poiRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}