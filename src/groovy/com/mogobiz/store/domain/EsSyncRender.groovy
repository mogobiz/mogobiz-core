// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class EsSyncRender
    extends RenderBase<EsSync>
{

    Map asMap(EsSync entity) {
        return super.asMap([
            'id',
            'report',
            'success',
            'timestamp',
            'esEnv',
            'esEnv.id',
            'esEnv.name',
            'catalogs',
            'catalogs.id',
            'catalogs.name',
            'categories',
            'categories.id',
            'categories.name',
            'products',
            'products.id',
            'products.name'
        ], [], entity)
    }

    def String asString(EsSync entity){return "com.mogobiz.store.domain.EsSync : "+entity.id}

}
