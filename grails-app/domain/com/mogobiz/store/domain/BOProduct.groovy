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
class BOProduct
    implements java.io.Serializable
{
    def BOProductValidation
    def BOProductRender

    java.util.Date dateCreated
    java.util.Date lastUpdated

    String uuid = java.util.UUID.randomUUID().toString()


    /**
     * 
     */
    boolean acquittement  = false 
    /**
     * 
     */
    boolean principal  = false 
    /**
     * <p>
     * correspond à la somme des montants des tickets types du BO
     * Product à laquelle on applique la réduction du la suggestion
     * (aucune réduction du principal)
     * </p>
     */
    long price 
    /**
     * 
     */
    com.mogobiz.store.domain.Product product 

    static hasMany = [  consumptions:com.mogobiz.store.domain.Consumption ]

    static transients = [ 'BOProductValidation', 'BOProductRender' ]


    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache usage:'read-write'

        tablePerHierarchy false

        table 'b_o_product'

        version false

        id name:'id',column:'id',generator:'native'
        acquittement column:"acquittement",insertable:true,updateable:true,lazy:false,cache:false
        principal column:"principal",insertable:true,updateable:true,lazy:false,cache:false
        price column:"price",insertable:true,updateable:true,lazy:false,cache:false


        consumptions column:"consumptions_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'

        product column:"product_fk",insertable:true,updateable:true,lazy:true,cache:'read-write'
    }

    static constraints = {
    uuid (nullable:false, unique:false)

        acquittement ( blank:false, nullable:false, unique:false)
        principal ( blank:false, nullable:false, unique:false)
        price ( blank:false, nullable:false, unique:false)
        product ( blank:false, nullable:false)
    }

    static com.mogobiz.store.domain.BOProduct findByOffreNonConsome(final long idBuyer, final java.lang.String codeOffre, final long idCompany, final java.util.Calendar dateConsomation)
    {
        return BOProduct.findByOffreNonConsome("SELECT boProduct FROM Buyer AS buyer JOIN buyer.sales AS sale JOIN sale.bOProducts AS boProduct JOIN boProduct.product AS product JOIN product.company AS company WHERE buyer.id = :idBuyer AND (sale.debut is null OR sale.debut < :dateConsomation) AND (sale.fin is null OR sale.fin > :dateConsomation) AND product.externalCode = :codeOffre AND company.id = :idCompany AND boProduct.consumptions.size < boProduct.nombreConsommationMax", idBuyer, codeOffre, idCompany, dateConsomation);
    }

    static com.mogobiz.store.domain.BOProduct findByOffreNonConsome(final java.lang.String queryString, final long idBuyer, final java.lang.String codeOffre, final long idCompany, final java.util.Calendar dateConsomation)
    {
        def ret = BOProduct.executeQuery(queryString, [idBuyer:idBuyer,codeOffre:codeOffre,idCompany:idCompany,dateConsomation:dateConsomation]).iterator()
        return ret.hasNext()?ret.next():null
    }

    String toString(){return BOProductRender?.asString(this)}

    def beforeInsert = {
        BOProductValidation.beforeInsert(this)
    }

    def beforeUpdate = {
        BOProductValidation.beforeUpdate(this)
    }

    def beforeDelete = {
        BOProductValidation.beforeDelete(this)
    }

    def beforeValidate = {
        BOProductValidation.beforeValidate(this)
    }

    def afterInsert = {
        BOProductValidation.afterInsert(this)
    }

    def afterUpdate = {
        BOProductValidation.afterUpdate(this)
    }

    def afterDelete = {
        BOProductValidation.afterDelete(this)
    }

    def onLoad = {
        BOProductValidation.onLoad(this)
    }

    java.util.Map asMapForJSON(java.util.List<String> included = [], java.util.List<String> excluded = [], String lang = 'fr') {return BOProductRender.asMap(included, excluded, this, lang)}

    // GrailsEntity.vsl merge-point
}