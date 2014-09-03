//
//     Generated by: RenderBase.vsl in andromda-grails-cartridge.
package com.mogobiz

import groovy.json.JsonSlurper;

import com.mogobiz.store.domain.Translation

import java.text.NumberFormat;

class RenderBase<T extends Serializable> {
    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], T entity, String lang = 'fr') {
        return com.mogobiz.json.RenderUtil.asMapForJSON(null, included, excluded, entity)
    }
    String asString(T entity) {
        return entity.toString()
    }
	
	/**
	 * Translate the map rendering of the entity in the given language
	 * @param result
	 * @param entity
	 * @param lang
	 */
	void translate(Map result, T entity, String lang) {
		if (lang) {
			Translation translation = Translation.createCriteria().get {
				eq ("lang", lang)
				eq ("target", entity.id)
			}
			
			if (translation) {
				result << new JsonSlurper().parseText(translation.value)
			}
		}		
	}

    String formatAmount(double amount, String currencyCode, String lang) {
        Locale locale = new Locale(lang);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        numberFormat.setCurrency(Currency.getInstance(currencyCode));
        return numberFormat.format(amount);

    }
}