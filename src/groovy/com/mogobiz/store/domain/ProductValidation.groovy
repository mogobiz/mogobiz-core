// license-header java merge-point
//
// Generated by: GrailsEntityValidation.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.ValidationBase

/**
 *
 */
class ProductValidation
    extends ValidationBase<Product>
{

    def beforeInsert(com.mogobiz.store.domain.Product entity) {
        super.beforeInsert(entity)
    }

    def beforeUpdate(com.mogobiz.store.domain.Product entity) {
        super.beforeUpdate(entity)
    }

    def beforeDelete(com.mogobiz.store.domain.Product entity) {
        super.beforeDelete(entity)
    }

    def afterInsert(com.mogobiz.store.domain.Product entity) {
        super.afterInsert(entity)
    }

    def afterUpdate(com.mogobiz.store.domain.Product entity) {
        super.afterUpdate(entity)
    }

    def afterDelete(com.mogobiz.store.domain.Product entity) {
        super.afterDelete(entity)
    }

    def onLoad(com.mogobiz.store.domain.Product entity) {
        super.onLoad(entity)
    }

	def static productCodeValidator = {  value, product ->
        List<Catalog> catalogs = Catalog.executeQuery("select d from Product p, Category c, Catalog d where p.category = c and c.catalog = d and p.code = :code and p.company = :company", [code:value, company:product.company])
        List<Long> ids = []
        catalogs.each {
            ids << it.id
        }
        Map res = ids.groupBy { it }.findAll { it.value.size() > 1}
        return res.keySet().size() == 0
	}

}
