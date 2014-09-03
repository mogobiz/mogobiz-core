// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class LocalTaxRateRender
    extends RenderBase<LocalTaxRate>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.LocalTaxRate entity, String lang = 'fr') {
		if (included == null || included.size() == 0) {
			included = [ 'id', 'rate', 'active', 'countryCode']
		}
		return super.asMap(included, excluded, entity, lang);
	}

    def String asString(com.mogobiz.store.domain.LocalTaxRate entity){return "com.mogobiz.store.domain.LocalTaxRate : "+entity.id}

}
