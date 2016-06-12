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
     */
    protected void updateExportCache(long target, String type) {
        List<Translation> lt = Translation.findAllByTargetAndType(target, type)
        String transToken = lt.collect {
            JSONObject data = JSON.parse(it.value) as JSONObject
            data.keys().inject("") { res, key ->
                String value = data.get(key)
                it.lang + "__" + key + "__" + value
            }
        }.inject("") { res, current ->
            res + "::" + current
        }

        switch (type) {
            case "CATALOG":
                Catalog cat = Catalog.findById(target)
                cat?.i18n = transToken
                cat.save(flush: true)
                break;
            case "CATEGORY":
                Category ct = Category.findById(target)
                ct?.i18n = transToken
                ct.save(flush: true)
                break;
            case "PRODUCT":
                Product p = Product.findById(target)
                p?.i18n = transToken
                p.save(flush: true)
                break;
            case "TICKET_TYPE":
                TicketType tt = TicketType.findById(target)
                tt?.i18n = transToken
                tt.save(flush: true)
                break;
            case "VARIATION":
                Variation v = Variation.findById(target)
                v?.i18n = transToken
                v.save(flush: true)
                break;
            case "BRAND":
                Brand b = Brand.findById(target)
                b?.i18n = transToken
                b.save(flush: true)
                break;
            case "VARIATION_VALUE":
                VariationValue vv = VariationValue.findById(target)
                vv?.i18n = transToken
                vv.save(flush: true)
                break;
            case "COUPON":
                Coupon c = Coupon.findById(target)
                c?.i18n = transToken
                c.save(flush: true)
                break;
            case "FEATURE":
                Feature f = Feature.findById(target)
                f?.i18n = transToken
                f.save(flush: true)
                break;
            case "PRODUCT_PROPERTY":
                ProductProperty pp = ProductProperty.findById(target)
                pp?.i18n = transToken
                pp.save(flush: true)
                break;
            case "POI":
                Poi poi = Poi.findById(target)
                poi?.i18n = transToken
                poi.save(flush: true)
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
            t = new Translation(companyId: user.company.id, target: target, lang: lang, type: type, catalog: catalog, company: user.company)
        }
        switch (type) {
            case "CATALOG":
                t.targetUuid = Catalog.findById(target)?.uuid
                break;
            case "CATEGORY":
                t.targetUuid = Category.findById(target)?.uuid
                break;
            case "PRODUCT":
                t.targetUuid = Product.findById(target)?.uuid
                break;
            case "TICKET_TYPE":
                t.targetUuid = TicketType.findById(target)?.uuid
                break;
            case "VARIATION":
                t.targetUuid = Variation.findById(target)?.uuid
                break;
            case "BRAND":
                t.targetUuid = Brand.findById(target)?.uuid
                break;
            case "VARIATION_VALUE":
                t.targetUuid = VariationValue.findById(target)?.uuid
                break;
            case "COUPON":
                t.targetUuid = Coupon.findById(target)?.uuid
                break;
            case "FEATURE":
                t.targetUuid = Feature.findById(target)?.uuid
                break;
            case "PRODUCT_PROPERTY":
                t.targetUuid == ProductProperty.findById(target).uuid
                break;
            case "POI":
                t.targetUuid = Poi.findById(target)?.uuid
                break;
            default:
                break;
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
        List<Translation> trans = Translation.findAllByCompanyIsNull()
        if (trans.size() > 0) {
            log.info("Starting to Update Translation Schema")
        }
        int count = 0
        trans.each {
            log.info("Updating Translation ${count++}/${trans.size()}")
            it.company = Company.findById(it.companyId)
            long target = it.target
            String xtype = it.type
            if (xtype == null || xtype.length() == 0)
                xtype = "OTHER"
            switch (xtype) {
                case "CATALOG":
                    Catalog catalog = Catalog.findById(target)
                    it.catalog = catalog
                    it.targetUuid = catalog.uuid
                    break;
                case "CATEGORY":
                    Category category = Category.findById(target)
                    it.catalog = category?.catalog
                    it.targetUuid = category?.uuid
                    break;
                case "PRODUCT":
                    Product product = Product.findById(target)
                    it.catalog = product?.category?.catalog
                    it.targetUuid = product?.uuid
                    break;
                case "TICKET_TYPE":
                    TicketType tt = TicketType.findById(target)
                    it.catalog = tt?.product?.category?.catalog
                    it.targetUuid = tt?.uuid
                    break;
                case "VARIATION":
                    Variation variation = Variation.findById(target)
                    it.catalog = variation?.category?.catalog
                    it.targetUuid = variation?.uuid
                    break;
                case "BRAND":
                    Brand brand = Brand.findById(target)
                    it.targetUuid = brand?.uuid
                    break;
                case "VARIATION_VALUE":
                    VariationValue vv = VariationValue.findById(target)
                    it.catalog = vv?.variation?.category?.catalog
                    it.targetUuid = vv?.uuid
                    break;
                case "COUPON":
                    it.targetUuid = Coupon.findById(target)?.uuid
                    break;
                case "FEATURE":
                    Feature feature = Feature.findById(target)
                    Catalog cat = feature?.product?.category?.catalog
                    if (cat == null) {
                        cat = feature?.category?.catalog
                    }
                    it.catalog = cat
                    it.targetUuid = feature?.uuid
                    break;
                case "PRODUCT_PROPERTY":
                    ProductProperty pp = ProductProperty.findById(target)
                    it.catalog = pp?.product?.category?.catalog
                    it.targetUuid == pp.uuid
                    break;
                case "POI":
                    Poi poi = Poi.findById(target)
                    it.targetUuid = poi?.uuid
                    break;
                case "OTHER":
                    Feature feature = Feature.findById(target)
                    Catalog cat = feature?.product?.category?.catalog
                    String newType = null
                    if (cat == null) {
                        cat = feature?.category?.catalog
                    }
                    if (cat) {
                        newType = "FEATURE"
                    }
                    it.targetUuid = feature?.uuid

                    ProductProperty pp = ProductProperty.findById(target)
                    if (cat == null) {
                        cat = pp?.product?.category?.catalog
                        if (cat) {
                            newType = "PRODUCT_PROPERTY"
                        }
                        it.targetUuid = pp?.uuid
                    }

                    if (cat == null) {
                        Brand brand = Brand.findById(target)
                        if (brand)
                            newType = "BRAND"
                        it.targetUuid = brand?.uuid
                    }

                    if (newType != null)
                        it.type = newType
                    it.catalog = cat
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
