// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.geolocation.domain

import com.mogobiz.RenderBase

/**
 *
 */
class LocationRender
    extends RenderBase<com.mogobiz.geolocation.domain.Location>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.geolocation.domain.Location entity, String lang = 'fr') {return super.asMap(included, excluded, entity, lang)}

    def String asString(com.mogobiz.geolocation.domain.Location entity){return "com.mogobiz.geolocation.domain.Location : "+entity.id}

}
