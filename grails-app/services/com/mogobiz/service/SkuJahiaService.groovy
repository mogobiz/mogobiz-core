package com.mogobiz.service

import com.mogobiz.store.domain.TicketType;
import com.mogobiz.store.exception.CurrencyRateException

class SkuJahiaService extends SkuService {

	/* (non-Javadoc)
	 * @see com.mogobiz.service.SkuService#renderSku(java.util.Locale, java.lang.String, com.mogobiz.store.domain.TicketType)
	 */
	@Override
	protected Map renderSku(Locale locale, String currencyCode, TicketType sku) throws CurrencyRateException {
		Map mapSku = sku.asMapForJSON(null, null, locale?.language)

		mapSku["taxRatesList"] = taxRateService.getAllActiveLocalTaxRateByProduct(sku.product);
		
		return mapSku
	}

}
