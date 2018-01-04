// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class TicketTypeRender
    extends RenderBase<TicketType>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.TicketType entity, String lang = 'fr') {
		if (included == null || included.size() == 0) {
			included = [ 'id',
				'name',
				'price',
				'description',
				'minOrder',
				'maxOrder',
				'xprivate',
				'startDate',
				'stopDate',
                'availabilityDate',
				'product',
				'product.id',
				'product.name',
				'product.company',
				'product.company.name',
				'product.category',
				'product.category.catalog',
				'product.category.catalog.name',
				'sku',
                'nbSales',
				'stock',
				'stock.stock',
				'stock.stockUnlimited',
				'stock.stockOutSelling',
				'variation1',
				'variation1.id',
				'variation2',
				'variation2.id',
				'variation3',
				'variation3.id',
				'publishable'
			]
		}
		Map result = super.asMap(included, excluded, entity, lang)
        def picture = entity.picture
        if(picture && ResourceType.PICTURE.equals(picture.xtype)) {
            result['picture'] = picture.asMapForJSON(null, null, lang)
        }
		return result
	}

    def String asString(com.mogobiz.store.domain.TicketType entity){return "com.mogobiz.store.domain.TicketType : "+entity.id}

}