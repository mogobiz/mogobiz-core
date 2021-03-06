// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase;

/**
 *
 */
class BrandRender
    extends RenderBase<Brand>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.Brand entity, String lang = 'fr') {
		if (included == null || included.size() == 0) {
			included = ["id", "name", "website", "facebooksite", "ibeacon", "ibeacon.id", "ibeacon.name"]
		}
		Map result = super.asMap(included, excluded, entity, lang);
		return result;
	}

    def String asString(com.mogobiz.store.domain.Brand entity){return "com.mogobiz.store.domain.Brand : "+entity.id}

}
