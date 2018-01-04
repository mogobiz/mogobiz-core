// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class RoleRender extends RenderBase<Role>
{

    Map asMap(List<String> included = [], List<String> excluded = [], Role entity, String lang = 'fr') {
        final map = super.asMap(included, excluded, entity, lang)
        def permissions = []
        RolePermission.findAllByRole(entity).each {permissions << it.asMapForJSON()}
        map << [permissions: permissions]
        return map
    }

    def String asString(Role entity){return "com.mogobiz.store.domain.Role : "+entity.id}

}