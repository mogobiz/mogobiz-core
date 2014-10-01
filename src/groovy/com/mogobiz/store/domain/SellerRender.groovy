// license-header java merge-point
//
// Generated by: GrailsEntityRender.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain
/**
 *
 */
class SellerRender
        extends com.mogobiz.store.domain.UserRender {

    java.util.Map asMap(java.util.List<String> included = [], java.util.List<String> excluded = [], com.mogobiz.store.domain.Seller entity, String lang = 'fr') {
        if (included == null || included.size() == 0) {
            included = [
                    'id',
                    'login',
                    'email',
                    'firstName',
                    'lastName',
                    'phone',
                    'text',
                    'picture',
                    'video',
                    'active',
                    'admin',
                    'validator',
                    'sell',
                    'agent',
                    'civility',
                    'birthdate',
                    'accountType',
                    'location',
                    'location.id',
                    'location.city',
                    'location.road1',
                    'location.road2',
                    'location.road3',
                    'location.roadNum',
                    'location.postalCode',
                    'location.countryCode',
                    'company',
                    'company.id',
                    'company.code'
            ]
        }
        List<String> companies = []
        Map result = super.asMap(included, excluded, entity, lang);
        entity.companies.each {
            companies << it.code
        }
        result << [companies: companies]
        translate(result, entity, lang)
        return result;
    }

    def String asString(com.mogobiz.store.domain.Seller entity) {
        return "com.mogobiz.store.domain.Seller : " + entity.id
    }

}
