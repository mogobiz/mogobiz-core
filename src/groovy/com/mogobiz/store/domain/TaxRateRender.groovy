// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class TaxRateRender
    extends RenderBase<TaxRate>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.TaxRate entity, String lang = 'fr') {
		if (included == null || included.size() == 0) {
			included = [ 'id', 'name']
		}
		return super.asMap(included, excluded, entity, lang);
	}

    def String asString(com.mogobiz.store.domain.TaxRate entity){return "com.mogobiz.store.domain.TaxRate : "+entity.id}

}
