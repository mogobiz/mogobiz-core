// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class ConsumptionRender
    extends RenderBase<Consumption>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.Consumption entity, String lang = 'fr') {return super.asMap(included, excluded, entity, lang)}

    def String asString(com.mogobiz.store.domain.Consumption entity){return "com.mogobiz.store.domain.Consumption : "+entity.id}

}