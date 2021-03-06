// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase
import com.mogobiz.json.RenderUtil

/**
 *
 */
class TranslationRender
    extends RenderBase<Translation>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.Translation entity, String lang = 'fr') {
		return RenderUtil.asMapForJSON(null, ["id", "target", "lang", "value", "type"], [], entity)
	}

    def String asString(com.mogobiz.store.domain.Translation entity){return "com.mogobiz.store.domain.Translation : "+entity.id}

}
