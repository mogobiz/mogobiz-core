// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEntity.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * 
 */
@Entity
@EqualsAndHashCode(includes="id")
class BOReturnedItem
    implements java.io.Serializable
{
    def BOReturnedItemValidation
    def BOReturnedItemRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * <p>
     * nombre de consommation nécessaire pour consommer totalement la
     * vente.<br/>
     * </p>
     * <p>
     * Correspond à la somme des quantité des types de tickets  (1 si
     * aucun type de ticket) multiplié par le nombre de suggestion
     * </p>
     */
    int quantity  = 1 
    /**
     * 
     */
    java.lang.Long refunded 
    /**
     * 
     */
    long totalRefunded 
    /**
     * 
     */
    com.mogobiz.store.domain.ReturnedIntemStatus status 
    /**
     * 
     */
    com.mogobiz.store.domain.BOCartItem bOCartItem 

    static transients = [ 'BOReturnedItemValidation', 'BOReturnedItemRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_returned_item'

        version false

        id name:'id',column:'id',generator:'native'
        quantity column:"quantity",insertable:true,updateable:true,lazy:false,cache:false
        refunded column:"refunded",insertable:true,updateable:true,lazy:false,cache:false
        totalRefunded column:"total_refunded",insertable:true,updateable:true,lazy:false,cache:false
        status column:"status",enumType:"string",insertable:true,updateable:true,lazy:false,cache:false


        bOCartItem column:"b_o_cart_item_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        quantity ( blank:false, nullable:false, unique:false)
        refunded ( blank:false, nullable:false, unique:false)
        totalRefunded ( blank:false, nullable:false, unique:false)
        status ( blank:false, nullable:false, unique:false)
        bOCartItem (nullable:true)
    }


    String toString(){return BOReturnedItemRender?.asString(this)}

    def beforeInsert = {
        BOReturnedItemValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOReturnedItemValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOReturnedItemValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOReturnedItemValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOReturnedItemValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOReturnedItemValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOReturnedItemValidation.afterDelete(this)
    }

    def onLoad = {
        BOReturnedItemValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOReturnedItemRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}