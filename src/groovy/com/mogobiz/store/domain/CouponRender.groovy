// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.json.RenderUtil
import com.mogobiz.RenderBase

/**
 *
 */
class CouponRender
    extends RenderBase<Coupon>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.Coupon entity, String lang = 'fr') {
        return RenderUtil.asMapForJSON(["id", "name", "code", "pastille","active", "numberOfUses", "startDate", "endDate", "catalogWise", "anonymous",
                "consumed", "description",
                "catalogs", "catalogs.id", "catalogs.name",
                "categories", "categories.id", "categories.name",
                "products", "products.id", "products.name",
                "ticketTypes", "ticketTypes.id", "ticketTypes.name", "ticketTypes.product", "ticketTypes.product.name", "ticketTypes.product.company", "ticketTypes.product.company.name",
                "rules", "rules.xtype", "rules.quantityMin", "rules.quantityMax", "rules.discount", "rules.xPurchased", "rules.yOffered"
        ], entity)
    }

    def String asString(com.mogobiz.store.domain.Coupon entity){return "com.mogobiz.store.domain.Coupon : "+entity.id}

}
