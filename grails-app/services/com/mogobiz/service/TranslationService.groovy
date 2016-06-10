/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.geolocation.domain.Poi
import com.mogobiz.store.domain.*
import grails.util.Holders

/**
 * Translation manager service
 */
class TranslationService {
    static transactional = true

    def ajaxResponseService;

    /**
     * Returns a list of configurable languages ​​by the Partner application
     * @return
     */
    List<String> languages(Seller seller) {
        String defaultLanguage = seller?.company?.defaultLanguage
        def languages = Holders.config.application.languages;
        return languages.findAll { String l ->
            l != defaultLanguage
        }
    }

    /**
     * Returns a list of Translation corresponding to the given target
     * @param target
     * @return
     */
    List<Map> list(long target, String type) {
        List<Translation> list = Translation.findAllByTargetAndType(target, type);
        List<Map> result = [];
        list.each { Translation t ->
            result << t.asMapForJSON();
        }
        return result;
    }

    /**
     * Delete the Translation corresponding to the given target and language
     * @param target
     * @param lang
     * @return
     */
    AjaxResponse delete(long target, String lang, String type) {
        AjaxResponse result = new AjaxResponse()

        Translation t = findByTargetTypeAndLang(target, type, lang);
        if (t != null) {
            t.delete()
            result.success = true;
        }
        return result;
    }

    /**
     * Create or update the Translation corresponding to the given target and language.
     * @param target
     * @param lang
     * @param value
     * @return
     */
    AjaxResponse update(User user, Catalog catalog, long target, String lang, String value, String type) {
        AjaxResponse result = new AjaxResponse()
        Translation t = findByTargetTypeAndLang(target, type, lang);
        if (t == null) {
            t = new Translation(companyId: user.company.id, target: target, lang: lang, type: type, catalog: catalog)
        }

        t.value = value;
        if (t.validate()) {
            t.save();
            result.success = true;
        } else {
            result = ajaxResponseService.prepareResponse(t, null);
        }
        return result;
    }

    private Translation findByTargetTypeAndLang(
            final long target, final String type, final String lang) {
        def ret = Translation.executeQuery("SELECT translation FROM Translation AS translation WHERE translation.target = :target AND translation.type = :type AND translation.lang = :lang",
                [target: target, type: type, lang: lang]).iterator()
        return ret.hasNext() ? ret.next() : null
    }

    void updateTranslationCatalog() {
        List<Translation> trans = Translation.findAllByCatalogIsNull()
        if (trans.size() > 0)
            log.info("Starting to Update Translation Schema")
        trans.each {
            long target = it.target
            String xtype = it.type
            if (xtype == null || xtype.length() == 0)
                xtype = "FEATURE"
            switch (xtype) {
                case "CATALOG":
                    it.catalog = Catalog.findById(target)
                    break;
                case "CATEGORY":
                    it.catalog = Category.findById(target)?.catalog
                    break;
                case "PRODUCT":
                    it.catalog = Product.findById(target)?.category?.catalog
                    break;
                case "TICKET_TYPE":
                    it.catalog = TicketType.findById(target)?.product?.category?.catalog
                    break;
                case "VARIATION":
                    it.catalog = Variation.findById(target)?.category?.catalog
                    break;
                case "VARIATION_VALUE":
                    it.catalog = VariationValue.findById(target)?.variation?.category?.catalog
                    break;
                case "COUPON":
                    //it.catalog = Coupon.findById(target)?.ca
                    break;
                case "FEATURE":
                    Catalog cat = Feature.findById(target)?.product?.category?.catalog
                    String newType = null
                    if (cat == null) {
                        cat = Feature.findById(target)?.category?.catalog

                    } else
                        newType = "FEATURE"

                    if (cat == null) {
                        cat = FeatureValue.findById(target)?.feature?.category?.catalog
                    } else
                        newType = "FEATURE"

                    if (cat == null) {
                        cat = ProductProperty.findById(target)?.product?.category?.catalog
                    } else
                        newType = "FEATURE"

                    if (cat == null) {
                        Brand brand = Brand.findById(target)
                        if (brand != null)
                            newType = "BRAND"
                    } else
                        newType = "FEATURE"

                    if (newType != null)
                        it.type = newType
                    it.catalog = cat
                    break;
                case "POI":
                    // Poi.findById(target)
                    break;
                case "BRAND":
                    // Brand.findById(target)
                    break;
                default:
                    break;
            }
            it.save(flush: true)
        }
        if (trans.size() > 0)
            log.info("Finished to update Translation Schema")
    }
}
