// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class ProfileRender extends RenderBase<Profile> {

    Map asMap(List<String> included = [], List<String> excluded = [], Profile entity, String lang = 'fr') {
        def map = super.asMap(included && !included.isEmpty() ? included : ['id', 'name', 'parent', 'parent.id', 'parent.name', 'company', 'company.id'], excluded, entity, lang)
        def permissions = []
        ProfilePermission.findAllByProfile(entity).each {permissions << it.asMapForJSON()}
        map << [permissions: permissions]
        map
    }

    def String asString(Profile entity){return "com.mogobiz.store.domain.Profile : "+entity.id}

}
