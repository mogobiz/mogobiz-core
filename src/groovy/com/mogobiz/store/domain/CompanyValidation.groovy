// license-header java merge-point
//
// Generated by: GrailsEntityValidation.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.ValidationBase
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.SecureCodec

/**
 *
 */
class CompanyValidation
    extends ValidationBase<Company>
{

    def beforeInsert(com.mogobiz.store.domain.Company entity) {
        if (entity.aesPassword == null)
            entity.aesPassword = SecureCodec.genKey()
        if (!entity.code) entity.code = IperUtil.normalizeName(entity.name)
        else entity.code = IperUtil.normalizeName(entity.code)
        super.beforeInsert(entity)
    }

    def beforeUpdate(com.mogobiz.store.domain.Company entity) {
        if (!entity.code) entity.code = IperUtil.normalizeName(entity.name)
        else entity.code = IperUtil.normalizeName(entity.code)
        super.beforeUpdate(entity)
    }

    def beforeDelete(com.mogobiz.store.domain.Company entity) {
        super.beforeDelete(entity)
    }

    def afterInsert(com.mogobiz.store.domain.Company entity) {
        super.afterInsert(entity)
    }

    def afterUpdate(com.mogobiz.store.domain.Company entity) {
        super.afterUpdate(entity)
    }

    def afterDelete(com.mogobiz.store.domain.Company entity) {
        super.afterDelete(entity)
    }

    def onLoad(com.mogobiz.store.domain.Company entity) {
        super.onLoad(entity)
    }


}
