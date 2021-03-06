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
class Location
    implements java.io.Serializable
{
    def locationValidation
    def locationRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    java.lang.Double latitude 
    /**
     * 
     */
    java.lang.Double longitude 
    /**
     * 
     */
    java.lang.String road1 
    /**
     * 
     */
    java.lang.String road2 
    /**
     * 
     */
    java.lang.String road3 
    /**
     * 
     */
    java.lang.String roadNum 
    /**
     * 
     */
    java.lang.String postalCode 
    /**
     * 
     */
    java.lang.String state 
    /**
     * 
     */
    java.lang.String city 
    /**
     * 
     */
    java.lang.String countryCode 
    static transients = [ 'locationValidation', 'locationRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy true

        discriminator value:'LOCATION', column:[name:'class',length:255]
        table 'location'

        version false

        id name:'id',column:'id',generator:'native'
        latitude column:"latitude",insertable:true,updateable:true,lazy:false,cache:false
        longitude column:"longitude",insertable:true,updateable:true,lazy:false,cache:false
        road1 column:"road1",insertable:true,updateable:true,lazy:false,cache:false
        road2 column:"road2",insertable:true,updateable:true,lazy:false,cache:false
        road3 column:"road3",insertable:true,updateable:true,lazy:false,cache:false
        roadNum column:"road_num",insertable:true,updateable:true,lazy:false,cache:false
        postalCode column:"postal_code",insertable:true,updateable:true,lazy:false,cache:false
        state column:"state",insertable:true,updateable:true,lazy:false,cache:false
        city column:"city",insertable:true,updateable:true,lazy:false,cache:false
        countryCode column:"country_code",insertable:true,updateable:true,lazy:false,cache:false

    }

    static constraints = {
    uuid (nullable:false, unique:false)

        latitude (nullable:true, unique:false)
        longitude (nullable:true, unique:false)
        road1 (nullable:true, unique:false)
        road2 (nullable:true, unique:false)
        road3 (nullable:true, unique:false)
        roadNum (nullable:true, unique:false)
        postalCode (nullable:true, unique:false)
        state (nullable:true, unique:false)
        city (nullable:true, unique:false)
        countryCode ( blank:false, nullable:false, unique:false)
    }

    static java.util.Collection findLocationsWithinNeighborhood(final java.lang.Long idNeighborhood)
    {
        return Location.findLocationsWithinNeighborhood("SELECT DISTINCT location FROM Location location WHERE location.neighborhood.id=:idNeighborhood", idNeighborhood, 0, 0);
    }

    static java.util.Collection findLocationsWithinNeighborhood(final java.lang.Long idNeighborhood, int pageNumber, int pageSize)
    {
        return Location.findLocationsWithinNeighborhood("SELECT DISTINCT location FROM Location location WHERE location.neighborhood.id=:idNeighborhood", idNeighborhood, pageNumber, pageSize);
    }

    static java.util.Collection findLocationsWithinNeighborhood(final java.lang.String queryString, final java.lang.Long idNeighborhood, int pageNumber, int pageSize)
    {
        def paginateParams = null
        if (pageNumber > 0 && pageSize > 0) {
            paginateParams = [offset : calculateFirstResult(pageNumber, pageSize), max : pageSize]
        }
        return paginateParams ? Location.executeQuery(queryString, [idNeighborhood:idNeighborhood], paginateParams) : Location.executeQuery(queryString, [idNeighborhood:idNeighborhood])
    }

    static int calculateFirstResult(int pageNumber, int pageSize) {
        int firstResult = 0
        if (pageNumber > 0) {
            firstResult = (pageNumber - 1) * pageSize
        }
        return firstResult
    }
    

    String toString(){return locationRender?.asString(this)}

    def beforeInsert = {
        locationValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        locationValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        locationValidation.beforeDelete(this)
    }

    def beforeValidate = {
        locationValidation.beforeValidate(this)
    }

    def afterInsert = {
        locationValidation.afterInsert(this)
    }

    def afterUpdate = {
        locationValidation.afterUpdate(this)
    }

    def afterDelete = {
        locationValidation.afterDelete(this)
    }

    def onLoad = {
        locationValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return locationRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}