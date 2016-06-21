/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.cmd.coupon.CouponCreateUpdateCommand
import com.mogobiz.store.cmd.coupon.CouponListCommand
import com.mogobiz.store.domain.*
import grails.orm.PagedResultList

class CouponService {

    private static final String DICTIONARY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    static transactional = true
    def authenticationService

    Coupon findByCode(long companyId, String couponCode) {
        return Coupon.createCriteria().get {
            eq("code", couponCode)
            company {
                eq("id", companyId)
            }
        }

    }

    /**
     * Generate a new code using system time
     * @return
     */
    String generateCode() {
        long v = System.currentTimeMillis();
        String result = "";
        while (v > 0) {
            int index = (int) (v % DICTIONARY.length())
            result = DICTIONARY.substring(index, index + 1) + result
            v = v / DICTIONARY.length()
        }
        return result
    }

    PagedResultList list(CouponListCommand params) throws IllegalArgumentException {
        Seller seller = authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            throw new IllegalArgumentException("Unknown seller")
        }
        return Coupon.createCriteria().list(params.getPagination()) {
            company { eq("id", seller.company.id) }
            order("startDate", "desc")
        }
    }

    /**
     * Create the new Coupon using parameters
     * @param params
     * @return
     * @throws IllegalArgumentException
     */
    Coupon create(CouponCreateUpdateCommand params) throws IllegalArgumentException {
        Seller seller = authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            throw new IllegalArgumentException("Unknown seller")
        }
        Coupon coupon = new Coupon()
        coupon.company = seller.company
        return createOrUpdate(coupon, params)
    }

    /**
     * Update the given coupon using parameters
     * @param params
     * @return
     * @throws IllegalArgumentException
     */
    Coupon update(CouponCreateUpdateCommand params) throws IllegalArgumentException {
        Seller seller = authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            throw new IllegalArgumentException("Unknown seller")
        }

        Coupon coupon = (params.id) ? Coupon.get(params.id) : null
        if (coupon == null || seller.company.id != coupon.company.id) {
            throw new IllegalArgumentException("Unknown coupon")
        }
//        coupon.categories?.clear()
//        coupon.products?.clear()
//        coupon.ticketTypes?.clear()
        return createOrUpdate(coupon, params)
    }

    /**
     * Create or update the given coupon using parameters
     * @param coupon
     * @param params
     * @return
     * @throws IllegalArgumentException
     */
    private Coupon createOrUpdate(Coupon coupon, CouponCreateUpdateCommand params) throws IllegalArgumentException {
        coupon.name = params.name
        coupon.pastille = params.pastille
        coupon.description = params.description
        coupon.code = params.code ? params.code : generateCode()
        coupon.active = params.active
        coupon.anonymous = params.anonymous ? params.anonymous : false
        coupon.catalogWise = params.catalogWise ? params.catalogWise : false
        coupon.numberOfUses = params.numberOfUses
        coupon.startDate = params.startDate
        coupon.endDate = params.endDate

        if (coupon.rules?.size() > 0) {
            List<Long> ids = coupon.rules.collect { it.id }
            ids.each {
                coupon.removeFromRules(ReductionRule.load(it))
            }
        }

        if (coupon.catalogs?.size() > 0) {
            List<Long> ids = coupon.catalogs.collect { it.id }
            ids.each {
                coupon.removeFromCatalogs(Catalog.load(it))
            }
        }

        if (coupon.categories?.size() > 0) {
            List<Long> ids = coupon.categories.collect { it.id }
            ids.each {
                coupon.removeFromCategories(Category.load(it))
            }
        }

        if (coupon.products?.size() > 0) {
            List<Long> ids = coupon.products.collect { it.id }
            ids.each {
                coupon.removeFromProducts(Product.load(it))
            }
        }

        if (coupon.ticketTypes?.size() > 0) {
            List<Long> ids = coupon.ticketTypes.collect { it.id }
            ids.each {
                coupon.removeFromTicketTypes(TicketType.load(it))
            }
        }

        // link with catalogs
        params.catalogs?.each { Catalog c ->
            coupon.addToCatalogs(c)
        }

        // link with categories
        params.categories?.each { Category c ->
            coupon.addToCategories(c)
        }

        // link with products
        params.products?.each { Product p ->
            coupon.addToProducts(p)
        }

        // link with skus
        params.skus?.each { TicketType tt ->
            coupon.addToTicketTypes(tt)
        }

        // Create ReductionRules
        params.rules?.each { ReductionRule rule ->
            if (ReductionRuleType.DISCOUNT.equals(rule.xtype) && rule.discount == null) {
                rule.errors.rejectValue("discount", "null")
            }
            if (ReductionRuleType.X_PURCHASED_Y_OFFERED.equals(rule.xtype)) {
                if (rule.xPurchased == null) {
                    rule.errors.rejectValue("xPurchased", "null")
                }
                if (rule.yOffered == null) {
                    rule.errors.rejectValue("yOffered", "null")
                }
            }

            if (!rule.hasErrors() && rule.validate()) {
                rule.save()
                coupon.addToRules(rule);
            } else {
                coupon.errors = rule.errors
            }
        }

        if (!coupon.hasErrors() && coupon.validate()) {
            coupon.save()
        } else {
            coupon.errors.each { println(it) }
        }
        return coupon
    }
}
