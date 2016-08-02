package com.mogobiz.store.domain

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 *
 */
@Entity
@EqualsAndHashCode(includes="id")
class EsSync
    implements java.io.Serializable
{
    def esSyncValidation
    def esSyncRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()

    /**
     *
     */
    java.lang.String report
    /**
     *
     */
    Boolean success
    /**
     *
     */
    java.util.Date timestamp
    /**
     *
     */
    com.mogobiz.store.domain.EsEnv esEnv
    /**
     *
     */
    com.mogobiz.store.domain.Company company

    static transients = [ 'esSyncValidation', 'esSyncRender' ]

    static mapping = {

        autoTimestamp true

        uuid column: "uuid", insertable: true, updateable: false, lazy: false, cache: false

        cache usage: 'read-write'

        tablePerHierarchy false

        table 'es_sync'

        version false

        id name: 'id', column: 'id', generator: 'native'
        report column:"report",insertable:true,updateable:true,lazy:false,type:"text",cache:false
        success column:"success",insertable:true,updateable:true,lazy:false,cache:false
        timestamp column:"timestamp",insertable:true,updateable:true,lazy:false,cache:false


        esEnv column:"es_env_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
        company column:"company_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static hasMany = [categories: Category, products: Product, catalogs: Catalog]

    static constraints = {
        report ( nullable:true, unique:false)
        success ( nullable:true, unique:false)
        timestamp ( nullable:true, unique:false)
        company ( nullable:false, unique:false)
        esEnv ( nullable:false, unique:false)
    }
}
