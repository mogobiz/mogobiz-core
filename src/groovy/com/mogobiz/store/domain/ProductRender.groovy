// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase
import com.mogobiz.service.ProductService
import com.mogobiz.service.TaxRateService

/**
 *
 */
class ProductRender
    extends RenderBase<Product>
{

	ProductService productService
	TaxRateService taxRateService
	
    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.Product entity, String lang = 'fr') {
		// the price is set by service because it's must be formated
		if (included == null || included.size() == 0) {
			included = [
				'id',
				'dateCreated',
				'lastUpdated',
                'availabilityDate',
				'name',
				'code',
				'externalCode',
				'state',
				'deleted',
				'description',
				'descriptionAsText',
                'keywords',
				'picture',
				'video',
				'modificationDate',
				'startDate',
				'stopDate',
				'uuid',
				'price',
                'nbSales',
				// feature
				'startFeatureDate',
				'stopFeatureDate',
				'stockDisplay',
				'xtype',
				'content',
				'url',
				'poi',
				'poi.id',
				'poi.road1',
				'poi.road2',
				'poi.road3',
				'poi.roadNum',
				'poi.postalCode',
				'poi.city',
				'poi.state',
				'poi.countryCode',
				'creation.date',
				'creation.user',
				'creation.user.firstName',
				'creation.user.LastName',
				'creation.user.email',
				// product resources
				'product2Resources',
				'product2Resources.resource',
				'product2Resources.resource.id',
				'product2Resources.resource.name',
				'product2Resources.resource.url',
				'product2Resources.resource.xtype',
				'product2Resources.montant',
				'product2Resources.position',
				// categories
				'category',
				'category.id',
				'category.name',
				// tags
				'tags',
				'tags.id',
				'tags.name',
				//calendar
				'calendarType',
				// shipping policy
				'shipping',
				'shipping.id',
				'shipping.name',
				// brand
				'brand',
				'brand.id',
				'brand.name',
                // shipping
                "shipping",
                "shipping.weight",
                "shipping.weightUnit",
                "shipping.width",
                "shipping.height",
                "shipping.depth",
                "shipping.linearUnit",
                "shipping.amount",
                "shipping.free",
                    //company
				'company',
				'company.id',
				'company.name',
                // Ibeacon
                "ibeacon",
                "ibeacon.id",
                "ibeacon.name"
			]
		}
		Map result = super.asMap(included, excluded, entity, lang);
		def resource = productService.retrievePicture(entity)
		if(resource) {
			result['picture'] = resource.asMapForJSON(null, null, lang); 
		}
		TaxRate taxRate = taxRateService.findTaxRateOfProduct(entity);
		if (taxRate != null) {
			result['taxRateId'] = taxRate.id;
		}

        def properties = []
        ProductProperty.findAllByProduct(entity).each {ProductProperty property ->
            properties << property.asMapForJSON(null, null, lang)
        }

        result << [properties:properties]

		return result
	}

    def String asString(com.mogobiz.store.domain.Product entity){return "com.mogobiz.store.domain.Product : "+entity.id}

}
