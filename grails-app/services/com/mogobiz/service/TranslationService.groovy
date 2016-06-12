/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.geolocation.domain.Poi
import com.mogobiz.store.domain.*
import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONObject

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

        Translation t = Translation.findByTargetAndTypeAndLang(target, type, lang)
        if (t != null) {
            t.delete()
            updateExportCache(target, type)
            result.success = true;
        }
        return result;
    }
    /**
     * Sync with i18n property
     * stored in i18n field as follows : lang1:::key1__value1::key2__value2::::lang1:::key1__value1::key2__value2::key3__value3::::...
     */
    protected void updateExportCache(long target, String type) {
        List<Translation> lt = Translation.findAllByTargetAndType(target, type)
        Map map = [:]
        lt.each { trans ->
            JSONObject data = JSON.parse(trans.value) as JSONObject
            data.keys().collect{ key ->
                String value = data.get(key)
                String kv = map.get(trans.lang)
                if (!kv)
                    map.put(trans.lang, key + "__" + value)
                else
                    map.put(trans.lang, kv + "||" + key + "__" + value)
            }
        }
        String transToken = map.inject("") { res, entry ->
                res + "||||" + entry.key + "|||" + entry.value
        }

        switch (type) {
            case "CATALOG":
                Catalog cat = Catalog.findById(target)
                cat?.i18n = transToken
                cat?.save(flush: true)
                break;
            case "CATEGORY":
                Category ct = Category.findById(target)
                ct?.i18n = transToken
                ct?.save(flush: true)
                break;
            case "PRODUCT":
                Product p = Product.findById(target)
                p?.i18n = transToken
                p?.save(flush: true)
                break;
            case "TICKET_TYPE":
                TicketType tt = TicketType.findById(target)
                tt?.i18n = transToken
                tt?.save(flush: true)
                break;
            case "VARIATION":
                Variation v = Variation.findById(target)
                v?.i18n = transToken
                v?.save(flush: true)
                break;
            case "BRAND":
                Brand b = Brand.findById(target)
                b?.i18n = transToken
                b?.save(flush: true)
                break;
            case "VARIATION_VALUE":
                VariationValue vv = VariationValue.findById(target)
                vv?.i18n = transToken
                vv?.save(flush: true)
                break;
            case "COUPON":
                Coupon c = Coupon.findById(target)
                c?.i18n = transToken
                c?.save(flush: true)
                break;
            case "FEATURE":
                Feature f = Feature.findById(target)
                f?.i18n = transToken
                f?.save(flush: true)
                break;
            case "PRODUCT_PROPERTY":
                ProductProperty pp = ProductProperty.findById(target)
                pp?.i18n = transToken
                pp?.save(flush: true)
                break;
            case "POI":
                Poi poi = Poi.findById(target)
                poi?.i18n = transToken
                poi?.save(flush: true)
                break;
            default:
                log.error("Unkown Translation Type $type")
                break;
        }
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
        Translation t = Translation.findByTargetAndTypeAndLang(target, type, lang)
        if (t == null) {
            t = new Translation(companyId: user.company.id, target: target, lang: lang, type: type, catalog: catalog)
        }
        t.value = value;
        if (t.validate()) {
            t.save();
            updateExportCache(target, type)
            result.success = true;
        } else {
            result = ajaxResponseService.prepareResponse(t, null);
        }
        return result;
    }

    void updateTranslationCatalog() {
        List<Translation> trans = Translation.findAllByCatalogIsNull()
        if (trans.size() > 0) {
            log.info("Starting to Update Translation Schema")
        }
        int count = 0
        trans.each {
            log.info("Updating Translation ${count++}/${trans.size()}")
            long target = it.target
            String xtype = it.type
            if (xtype == null || xtype.length() == 0)
                xtype = "OTHER"
            switch (xtype) {
                case "CATALOG":
                    Catalog catalog = Catalog.findById(target)
                    it.catalog = catalog
                    break;
                case "CATEGORY":
                    Category category = Category.findById(target)
                    it.catalog = category?.catalog
                    break;
                case "PRODUCT":
                    Product product = Product.findById(target)
                    it.catalog = product?.category?.catalog
                    break;
                case "TICKET_TYPE":
                    TicketType tt = TicketType.findById(target)
                    it.catalog = tt?.product?.category?.catalog
                    break;
                case "VARIATION":
                    Variation variation = Variation.findById(target)
                    it.catalog = variation?.category?.catalog
                    break;
                case "BRAND":
                    Brand brand = Brand.findById(target)
                    break;
                case "VARIATION_VALUE":
                    VariationValue vv = VariationValue.findById(target)
                    it.catalog = vv?.variation?.category?.catalog
                    break;
                case "COUPON":
                    break;
                case "FEATURE":
                    Feature feature = Feature.findById(target)
                    Catalog cat = feature?.product?.category?.catalog
                    if (cat == null) {
                        cat = feature?.category?.catalog
                    }
                    it.catalog = cat
                    break;
                case "PRODUCT_PROPERTY":
                    ProductProperty pp = ProductProperty.findById(target)
                    it.catalog = pp?.product?.category?.catalog
                    break;
                case "POI":
                    Poi poi = Poi.findById(target)
                    break;
                default:
                    break;
            }
            it.save(flush: true)
            updateExportCache(it.target, it.type)
        }
        if (trans.size() > 0)
            log.info("Finished to update Translation Schema")
    }
}
