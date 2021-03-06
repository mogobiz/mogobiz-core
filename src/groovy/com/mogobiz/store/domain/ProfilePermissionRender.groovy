// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase
import com.mogobiz.utils.ProfileUtils

/**
 *
 */
class ProfilePermissionRender extends RenderBase<ProfilePermission> {

    Map asMap(List<String> included = [], List<String> excluded = [], ProfilePermission entity, String lang = 'fr') {
        def map = super.asMap(included && !included.isEmpty() ? included : ['id', 'profile', 'profile.id', 'profile.name', 'target'], excluded, entity, lang)
        map << [key: entity.key ?: ProfileUtils.retrievePermissionFrom(entity.target)?.key]
        map
    }

    def String asString(ProfilePermission entity){return "com.mogobiz.store.domain.ProfilePermission : "+entity.id}

}
