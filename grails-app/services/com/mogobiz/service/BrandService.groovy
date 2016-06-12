/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service

import com.mogobiz.store.domain.Brand
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductState

/**
 * Management service brands
 */
class BrandService
{

	/**
	 * This method returns all visible brands of the merchant's site
	 * @param locale
	 * @param companyId
	 * @return
	 */
    List<Map> listVisibleByCompany(Locale locale, long companyId)
    {
		List<Brand> liste = Brand.createCriteria().list {
			company { eq("id", companyId) }
			eq("hide", false)
		}
		
		List<Map> result = [];
		liste.each { Brand b ->
			result << b.asMapForJSON(null, null, locale?.language)
		}
		return result;
    }

	/**
	 * This method returns the active brands of the merchant's site for which there is at least one active product associated with the given category.
	 * A brand is active if it is not hidden and if it is referenced by a active product
	 * @param locale
	 * @param companyId
	 * @param categoryId
	 * @return
	 */
    List<Map> listActiveByCategory(Locale locale, long companyId, long categoryId)
    {
		List<Brand> liste = Product.createCriteria().list {
			company { eq("id", companyId) }
			category { eq("id", categoryId) }
			brand { eq("hide", false) }
			eq ("state", ProductState.ACTIVE)
			projections {
				distinct("brand")
			}
		}
		
		List<Map> result = [];
		liste.each { Brand b ->
			result << b.asMapForJSON(null, null, locale?.language)
		}
		return result;
    }
}