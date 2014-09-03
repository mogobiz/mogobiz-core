// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service

import com.mogobiz.store.domain.Product
import com.mogobiz.store.exception.CurrencyRateException
import com.mogobiz.utils.JahiaQueryUtil

/**
 * @see com.mogobiz.service.IProductService
 */
class ProductJahiaService extends ProductService {

	/* (non-Javadoc)
	 * @see com.mogobiz.service.ProductService#renderProduct(java.util.Locale, java.lang.String, com.mogobiz.store.domain.Product)
	 */
	@Override
	protected Map renderProduct(Locale locale, String currencyCode, Product product) throws CurrencyRateException {
		Map mapProduct = product.asMapForJSON(null, null, locale?.language)
		
		// Ajout du taux de taxe par pays
		mapProduct["taxRatesList"] = taxRateService.getAllActiveLocalTaxRateByProduct(product);
		
		return mapProduct
	}
	
	public List<Map> getProductsByJahiaQuery(Locale locale, String currencyCode, long companyId, String queryJahia, String queryOrderBy, Map pagination) {
		String query = JahiaQueryUtil.transformeJahiaQuery(queryJahia);
		boolean joinWithBrand = query.contains("product.brand");
		query = query.replaceAll("product\\.brand", "brand");
		query = query.replaceAll("product\\.category", "category");

		String requete = "SELECT product FROM Product as product INNER JOIN product.company as company ";
		if (joinWithBrand) {
			requete += "INNER JOIN product.brand as brand "
		}
		requete += "INNER JOIN product.category as category WHERE company.id = :company" + ((query.size() > 0) ? " AND (${query})" : "");
		requete += JahiaQueryUtil.transformeJahiaOrderBy(queryOrderBy);
		List<Product> productsList = Product.executeQuery(requete, [company: companyId], pagination)
		List<Map> result = []
		productsList.each { Product p ->
			result << renderProduct(locale, currencyCode, p);
		}
		return result;
	}

}