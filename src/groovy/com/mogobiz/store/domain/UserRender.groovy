// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class UserRender extends RenderBase<User>
{

    Map asMap(List<String> included = [], List<String> excluded = [], User entity, String lang = 'fr') {
        def map = super.asMap(included, excluded, entity, lang)
        map << [roles: entity.roles.collect {it.asMapForJSON()}]
        map << [profiles: entity.profiles.collect {it.asMapForJSON()}]
        def permissions = []
        UserPermission.findAllByUser(entity).each {permissions << it.asMapForJSON()}
        map << [permissions: permissions]
        return map
    }

    def String asString(User entity){return "com.mogobiz.store.domain.User : "+entity.id}

}
