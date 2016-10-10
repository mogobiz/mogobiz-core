package com.mogobiz.service

import com.mogobiz.store.domain.Brand
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Coupon
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.LocalTaxRate
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductProperty
import com.mogobiz.store.domain.ReductionRule
import com.mogobiz.store.domain.ShippingRule
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.Variation
import com.mogobiz.store.domain.VariationValue

import java.text.SimpleDateFormat

class BaseExportService {
    def categoryService
    def featureService
    def grailsApplication

    final List<String> brandHeaders = ["uuid", "name", "website", "facebook", "twitter", "description", "hide", "i18n"]
    final List<String> taxHeaders = ["uuid", "name", "country-code", "state-code", "rate", "active"]
    final List<String> catHeaders = ["uuid", "external-code", "path", "name", "position", "description", "keywords", "hide", "seo", "google", "deleted", "i18n"]
    final List<String> featHeaders = ["category-uuid", "category-path", "product-uuid", "product-code", "uuid", "external-code", "domain", "name", "value", "hide", "i18n"]
    final List<String> varHeaders = ["category-uuid", "category-path", "uuid", "external-code", "name", "google", "hide", "i18n"]
    final List<String> varValHeaders = ["category-uuid", "category-path", "variation-uuid", "variation-name", "uuid", "external-code", "value", "google", "i18n"]
    final List<String> prdHeaders = ["category-uuid", "category-path", "uuid", "external-code", "code", "name", "xtype", "price", "state", "description", "sales", "display-stock", "calendar", "start-date", "stop-date", "start-featured-date", "stop-featured-date", "seo", "tags", "keywords", "brand-name", "tax-rate", "date-created", "last-updated", "i18n"]
    final List<String> prdPropHeaders = ["category-uuid", "category-path", "product-uuid", "product-code", "uuid", "name", "value", "i18n"]
    final List<String> skuHeaders = ["category-uuid", "category-path", "product-uuid", "product-code", "uuid", "external-code", "sku", "name", "price", "min-order", "max-order", "sales", "start-date", "stop-date", "private", "remaining-stock", "unlimited-stock", "outsell-stock", "description", "availability-date", "google-gtin", "google-mpn", "variation-name-1", "variation-value-1", "variation-name-2", "variation-value-2", "variation-name-3", "variation-value-3", "i18n"]
    final List<String> shipHeaders = ["uuid", "country-code", "min-amount", "max-amount", "price"]
    final List<String> couponHeaders = ["uuid", "name", "code", "active", "number-of-uses", "start-date", "end-date", "catalog-wise", "for-sale", "description", "anonymous", "pastille", "consumed", "i18n"]
    final List<String> reductionRuleHeaders = ["coupon-code", "uuid", "xtype", "quantity-min", "quantity-max", "discount", "xpurchased", "yoffered"]
    final List<String> couponUseHeaders = ["uuid", "code", "category-uuid", "product-uuid", "sku-uuid", "target-name"]

    Map<String, String> toMap(Coupon it) {
        [
                "type"        : "Coupon",
                "uuid"        : it.uuid,
                "name"        : it.name,
                "code"        : it.code,
                "active"      : "" + it.active,
                "numberOfUses": it.numberOfUses?.toString(),
                "startDate"   : it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "",
                "endDate"     : it.endDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.endDate.getTime()) : "",
                "catalogWise" : "" + it.catalogWise,
                "forSale"     : "" + it.forSale,
                "description" : it.description,
                "anonymous"   : it.anonymous?.toString(),
                "pastille"    : it.pastille,
                "consumed"    : "" + it.consumed,
                "i18n"        : it.i18n
        ]
    }

    List<String> toArray(Coupon it) {
        [it.uuid, it.name, it.code, "" + it.active, it.numberOfUses?.toString(), it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.endDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.endDate.getTime()) : "", "" + it.catalogWise, "" + it.forSale, it.description, it.anonymous?.toString(), it.pastille, "" + it.consumed, it.i18n]
    }

    Map<String, String> toMap(ReductionRule it, String couponCode) {
        [
                "type"        : "ReductionRule",
                "couponCode" : couponCode,
                "uuid"       : it.uuid,
                "xtype"      : it.xtype.name(),
                "quantityMin": it.quantityMin?.toString(),
                "quantityMax": it.quantityMax?.toString(),
                "discount"   : it.discount,
                "xPurchased" : it.xPurchased?.toString(),
                "yOffered"   : it.yOffered?.toString()
        ]

    }

    List<String> toArray(ReductionRule it, String couponCode) {
        [couponCode, it.uuid, it.xtype.name(), it.quantityMin?.toString(), it.quantityMax?.toString(), it.discount, it.xPurchased?.toString(), it.yOffered?.toString()]
    }

    Map<String, String> toMap(ShippingRule it) {
        [
                "type"        : "ShippingRule",
                "uuid"       : it.uuid,
                "countryCode": it.countryCode,
                "minAmount"  : "" + it.minAmount,
                "maxAmount"  : "" + it.maxAmount,
                "price"      : it.price
        ]
    }

    List<String> toArray(ShippingRule it) {
        [it.uuid, it.countryCode, "" + it.minAmount, "" + it.maxAmount, it.price]
    }

    Map<String, String> toMap(Brand it) {
        [
                "type"        : "Brand",
                "uuid"        : it.uuid,
                "name"        : it.name,
                "website"     : it.website,
                "facebooksite": it.facebooksite,
                "twitter"     : it.twitter,
                "description" : it.description,
                "hide"        : "" + it.hide,
                "i18n"        : it.i18n
        ]
    }

    List<String> toArray(Brand it) {
        [it.uuid, it.name, it.website, it.facebooksite, it.twitter, it.description, "" + it.hide, it.i18n]
    }

    Map<String, String> toMap(Category it) {
        [
                "type"        : "Category",
                "uuid"          : it.uuid,
                "externalCode"  : it.externalCode,
                "categoryPath"  : categoryService.path(it),
                "name"          : it.name,
                "position"      : "" + it.position,
                "description"   : it.description,
                "keywords"      : it.keywords,
                "hide"          : it.hide?.toString(),
                "sanitizedName" : it.sanitizedName,
                "googleCategory": it.googleCategory,
                "deleted"       : "" + it.deleted,
                "i18n"          : it.i18n
        ]
    }

    List<String> toArray(Category it) {
        [it.uuid, it.externalCode, categoryService.path(it), it.name, "" + it.position, it.description, it.keywords, it.hide?.toString(), it.sanitizedName, it.googleCategory, "" + it.deleted, it.i18n]
    }

    Map<String, String> toMapForCat(Feature it, Category cat) {
        [
                "type"        : "CategoryFeature",
                "categoryUuid": cat.uuid,
                "categoryPath": categoryService.path(cat),
                "uuid"        : it.uuid,
                "externalCode": it.externalCode,
                "domain"      : it.domain,
                "name"        : it.name,
                "value"       : it.value,
                "hide"        : it.hide?.toString(),
                "i18n"        : it.i18n
        ]
    }

    List<String> toArrayForCat(Feature it, int catRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, null, null, it.uuid, it.externalCode, it.domain, it.name, it.value, it.hide?.toString(), it.i18n]
    }

    Map<String, String> toMapForPrd(Feature it, Category cat, Product prd) {
        [
                "type"        : "ProductFeature",
                "productCode" : prd.code,
                "externalCode": it.externalCode,
                "uuid"        : it.uuid,
                "domain"      : it.domain,
                "name"        : it.name,
                "value"       : it.value?.indexOf("||||") >= 0 ? it.value.substring(it.value.indexOf("||||") + 4) : it.value,
                "hide"        : it.hide?.toString(),
                "i18n"        : it.i18n
        ]
    }

    List<String> toArrayForPrd(Feature it, int catRowNum, int prdRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "product!C" + prdRowNum, "product!E" + prdRowNum, it.uuid, it.externalCode, it.domain, it.name, it.value?.indexOf("||||") >= 0 ? it.value.substring(it.value.indexOf("||||") + 4) : it.value, it.hide?.toString(), it.i18n]
    }

    Map<String, String> toMap(Variation it, Category cat) {
        [
                "type"        : "Variation",
                "categoryUuid"       : cat.uuid,
                "categoryPath"       : categoryService.path(cat),
                "uuid"               : it.uuid,
                "externalCode"       : it.externalCode,
                "name"               : it.name,
                "googleVariationType": it.googleVariationType,
                "hide"               : it.hide?.toString(),
                "i18n"               : it.i18n
        ]
    }

    List<String> toArray(Variation it, int catRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, it.uuid, it.externalCode, it.name, it.googleVariationType, it.hide?.toString(), it.i18n]
    }

    Map<String, String> toMap(VariationValue it, Category cat, Variation var) {
        [
                "type"        : "VariationValue",
                "categoryUuid"        : cat.uuid,
                "categoryPath"        : categoryService.path(cat),
                "variationUuid"       : var.uuid,
                "variationName"       : var.name,
                "uuid"                : it.uuid,
                "externalCode"        : it.externalCode,
                "value"               : it.value,
                "googleVariationValue": it.googleVariationValue,
                "i18n"                : it.i18n
        ]
    }

    List<String> toArray(VariationValue it, int catRowNum, int varRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "variation!C" + varRowNum, "variation!E" + varRowNum, it.uuid, it.externalCode, it.value, it.googleVariationValue, it.i18n]
    }

    Map<String, String> toMap(Product it, Category cat) {
        [
                "type"        : "Product",
                "categoryUuid"    : cat.uuid,
                "categoryPath"    : categoryService.path(cat),
                "uuid"            : it.uuid,
                "externalCode"    : it.externalCode ?: "",
                "code"            : it.code,
                "name"            : it.name,
                "xtype"           : "" + it.xtype,
                "price"           : "" + it.price,
                "state"           : "" + it.state,
                "description"     : it.description ?: "",
                "nbSales"         : "" + it.nbSales,
                "stockDisplay"    : "" + it.stockDisplay,
                "calendarType"    : "" + it.calendarType,
                "startDate"       : it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "",
                "stopDate"        : it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "",
                "startFeatureDate": it.startFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startFeatureDate.getTime()) : "",
                "stopFeatureDate" : it.stopFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopFeatureDate.getTime()) : "",
                "sanitizedName"   : it.sanitizedName,
                "tags"            : it.tags.collect { it.name }.join(","),
                "keywords"        : it.keywords ?: "",
                "brand"           : it.brand ? it.brand.name : "",
                "taxRate"         : it.taxRate ? it.taxRate.name : "",
                "dateCreated"     : new SimpleDateFormat("yyyy-MM-dd").format(it.dateCreated),
                "lastUpdated"     : new SimpleDateFormat("yyyy-MM-dd").format(it.lastUpdated),
                "i18n"            : it.i18n
        ]

    }

    List<String> toArray(Product it, int catRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, it.uuid, it.externalCode ?: "", it.code, it.name, "" + it.xtype, "" + it.price, "" + it.state, it.description ?: "", "" + it.nbSales, "" + it.stockDisplay, "" + it.calendarType, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "", it.startFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startFeatureDate.getTime()) : "", it.stopFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopFeatureDate.getTime()) : "", it.sanitizedName, it.tags.collect {
            it.name
        }.join(","), it.keywords ?: "", it.brand ? it.brand.name : "", it.taxRate ? it.taxRate.name : "", new SimpleDateFormat("yyyy-MM-dd").format(it.dateCreated), new SimpleDateFormat("yyyy-MM-dd").format(it.lastUpdated), it.i18n]
    }

    Map<String, String> toMap(TicketType it, Category cat, Product prd) {
        [
                "type"        : "Sku",
                "categoryPath"    : categoryService.path(cat),
                "productCode"     : prd.code,
                "uuid"            : it.uuid,
                "externalCode"    : it.externalCode,
                "sku"             : it.sku,
                "name"            : it.name,
                "price"           : "" + it.price,
                "minOrder"        : "" + it.minOrder,
                "maxOrder"        : "" + it.maxOrder,
                "nbSales"         : "" + it.nbSales,
                "startDate"       : it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "",
                "stopDate"        : it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "",
                "xprivate"        : it.xprivate?.toString(),
                "stock"           : it.stock?.stock?.toString(),
                "stockUnlimited"  : it.stock ? "" + it.stock.stockUnlimited : "",
                "stockOutSelling" : it.stock ? "" + it.stock.stockOutSelling : "",
                "description"     : it.description,
                "availabilityDate": it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : "",
                "gtin"            : it.gtin,
                "mpn"             : it.mpn,
                "variation1Name"  : it.variation1?.variation?.name,
                "variation1Value" : it.variation1?.value,
                "variation2Name"  : it.variation2?.variation?.name,
                "variation2Value" : it.variation2?.value,
                "variation3Name"  : it.variation3?.variation?.name,
                "variation3Value" : it.variation3?.value,
                "i18n"            : it.i18n
        ]
    }

    List<String> toArray(TicketType it, int catRowNum, int prdRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "product!C" + prdRowNum, "product!E" + prdRowNum, it.uuid, it.externalCode, it.sku, it.name, "" + it.price, "" + it.minOrder, "" + it.maxOrder, "" + it.nbSales, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "", it.xprivate?.toString(), it.stock?.stock?.toString(), it.stock ? "" + it.stock.stockUnlimited : "", it.stock ? "" + it.stock.stockOutSelling : "", it.description, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : "", it.gtin, it.mpn, it.variation1?.variation?.name, it.variation1?.value, it.variation2?.variation?.name, it.variation2?.value, it.variation3?.variation?.name, it.variation3?.value, it.i18n]
    }

    Map<String, String> toMap(ProductProperty it, Category cat, Product prd) {
        [
                "type"        : "ProductProperty",
                "categoryPath": categoryService.path(cat),
                "productCode" : prd.code,
                "uuid"        : it.uuid,
                "name"        : it.name,
                "value"       : it.value,
                "i18n"        : it.i18n
        ]
    }

    List<String> toArray(ProductProperty it, int catRowNum, int prdRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "product!C" + prdRowNum, "product!E" + prdRowNum, it.uuid, it.name, it.value, it.i18n]
    }

    Map<String, String> toMap(LocalTaxRate it, String name) {
        [
                "type"        : "LocalTaxRate",
                "uuid"       : it.uuid,
                "name"       : name,
                "countryCode": it.countryCode ?: "",
                "stateCode"  : it.stateCode ?: "",
                "rate"       : "" + it.rate,
                "active"     : "" + it.active
        ]
    }

    List<String> toArray(LocalTaxRate it, String name) {
        [it.uuid, name, it.countryCode ?: "", it.stateCode ?: "", "" + it.rate, "" + it.active]
    }

}
