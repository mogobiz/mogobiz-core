// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.RenderBase

/**
 *
 */
class BOCartRender
    extends RenderBase<BOCart>
{

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.BOCart entity, String lang = 'fr') {
        if (included == null || included.size() == 0) {
            included = ["id", "transactionUuid", "date", "status", "buyer"]
        }
        Map r = super.asMap(included, excluded, entity, lang)
        r["price"] = formatAmount(entity.price * entity.currencyRate, entity.currencyCode, lang)
        return r;
    }

    def String asString(com.mogobiz.store.domain.BOCart entity){return "com.mogobiz.store.domain.BOCart : "+entity.id}

}
