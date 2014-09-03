package com.mogobiz.service

import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.exception.CurrencyRateException

class SkuService {
	static transactional = true
		
	RateService rateService;
	TaxRateService taxRateService

	/**
	 * Renvoi true s'il existe au moins un sku associé au produit passé en paramètre
	 * @param product
	 * @return
	 */
	boolean existeSKUsForProduct(Product product)
	{
		return TicketType.findByProduct(product) != null;
	}
	
	/**
	 * Renvoie la liste des SKU triée par ordre croisant de position et respectant les critères<br/>
	 * - sku du produit passé en paramètre et faisant parti de la compagnie du vendeur authentifié<br/>
	 * - ET sku non privé (xprivate null ou false)<br/>
	 * - ET stock disponible (cad illimité ou supérieur au nombre de vente)
	 * @param idProduct
	 * @return
	 */
    List<Map> listSalableSKUByProduct(Locale locale, String currencyCode, long compagnyId, long idProduct) 
	{
		List<TicketType> liste = TicketType.createCriteria().list {
			'product' {
				idEq (idProduct.longValue())
				'company' {idEq (compagnyId)}
			}
			or {
				isNull ("xprivate")
				eq("xprivate", false)
			}
			// stock est embedded, on utilise donc une désignation pointée
			// et pas une jointure
			or {
				eq ("stock.stockUnlimited", true)
				and {
					isNotNull ("stock.stock")
					gtProperty("stock.stock", "nbSales")
				}
			}
			order("position");
		}
		List<Map> result = [];
		liste?.each { TicketType sku ->
			result << renderSku(locale, currencyCode, sku);
		}
		return result;
    }
	
	public Map getSku(Locale locale, String currencyCode, long compagnyId, long skuId) {
		TicketType sku = TicketType.createCriteria().get {
			'product' {
				'company' {idEq (compagnyId)}
			}
			idEq (skuId)
		}
		return renderSku(locale, currencyCode, sku);
	}

	
	/**
	 * Transforms the product into a map (with formated prices)
	 * @param locale
	 * @param currencyCode
	 * @param product
	 * @return
	 * @throws CurrencyRateException
	 */
	protected Map renderSku(Locale locale, String currencyCode, TicketType sku) throws CurrencyRateException {
		Map mapSku = sku.asMapForJSON(null, null, locale?.language)
		mapSku << renderSkuPrice(locale, currencyCode, sku);
		return mapSku
	}

	public Map renderSkuPrice(Locale locale, String currencyCode, TicketType sku) throws CurrencyRateException {
		// formatting price
		Float taxRate = taxRateService.findTaxRateByProduct(sku.product, locale?.country)
		Long endPrice = taxRateService.calculateEndPrix(sku.price, taxRate)
		Map price = [:]
		price["price"] = rateService.format(sku.price, currencyCode, locale);
		price["taxRate"] = taxRate
		price["endPrice"] = rateService.format(endPrice, currencyCode, locale);
		return price
	}

}
