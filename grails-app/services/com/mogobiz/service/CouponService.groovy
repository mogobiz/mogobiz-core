package com.mogobiz.service

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.cmd.coupon.CouponCreateUpdateCommand
import com.mogobiz.store.cmd.coupon.CouponListCommand
import com.mogobiz.store.domain.*
import com.mogobiz.store.vo.CartItemVO
import com.mogobiz.store.vo.CartVO
import com.mogobiz.store.vo.CouponVO
import com.mogobiz.utils.DateUtilitaire
import com.mogobiz.utils.IperUtil
import grails.orm.PagedResultList

class CouponService {

    private static final String DICTIONARY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

	static transactional = true
    AuthenticationService authenticationService

    Coupon findByCode(long companyId, String couponCode) {
        return Coupon.createCriteria().get {
            eq ("code", couponCode)
            company {
                eq("id", companyId)
            }
        }

    }

    /**
     * update the active attribute and calculate the reduction price
     * using the content of the cartVO and the current date (to define is coupon
     * is active or not)
     * @param couponVO
     * @param cart
     */
    void updateCoupon(CouponVO couponVO, CartVO cart) {
        Coupon coupon = Coupon.get(couponVO.id)

        couponVO.name = coupon.name;
        couponVO.code = coupon.code;
        couponVO.startDate = coupon.startDate;
        couponVO.endDate = coupon.endDate;
        couponVO.active = false
        couponVO.price = 0

        if (coupon.active &&
            (coupon.startDate == null || DateUtilitaire.isBeforeOrEqual(coupon.startDate)) &&
            (coupon.endDate == null || DateUtilitaire.isAfterOrEqual(coupon.endDate))) {

            List<TicketType> listTicketType = TicketType.createCriteria().list {
                or {
                    if (coupon.products) {
                        "in"("product", coupon.products)
                    }
                    if (coupon.categories) {
                        product {
                            'in' ('category', coupon.categories)
                        }
                    }
                    if (coupon.ticketTypes) {
                        "in"("id", coupon.ticketTypes.collect {it.id})
                    }
                }
            }

            if (listTicketType.size() > 0) {
                long quantity = 0
                long xPurchasedPrice = Long.MAX_VALUE;
                cart.cartItemVOs.each { CartItemVO cartItem ->
                    if (listTicketType.find {it.id == cartItem.skuId} != null) {
                        quantity += cartItem.quantity
                        if (cartItem.endPrice > 0) {
                            xPurchasedPrice = Math.min(xPurchasedPrice, cartItem.endPrice)
                        }
                    }
                }
                if (xPurchasedPrice == Long.MAX_VALUE) {
                    xPurchasedPrice = 0
                }

                if (quantity > 0) {
                    couponVO.active = true
                    coupon.rules.each {ReductionRule rule ->
                        if (ReductionRuleType.DISCOUNT.equals(rule.xtype)) {
                            if (cart.endPrice != null) {
                                couponVO.price += IperUtil.computeDiscount(rule.discount, cart.endPrice)
                            }
                            else {
                                couponVO.price += IperUtil.computeDiscount(rule.discount, cart.price)
                            }
                        }
                        else if (ReductionRuleType.X_PURCHASED_Y_OFFERED.equals(rule.xtype)) {
                            long multiple = quantity / rule.xPurchased
                            couponVO.price += xPurchasedPrice * rule.yOffered * multiple
                        }
                    }
                }
            }
        }
    }

    /**
     * Verify if the coupon is available (from the point of view of the number of uses)
     * If the number of uses is verify, the sold of the coupon is increase
     * @param coupon
     * @return
     */
    boolean consumeCoupon(Coupon coupon) {
        if (coupon.reductionSold == null) {
            coupon.reductionSold = new ReductionSold();
            coupon.reductionSold.sold = 0
            coupon.reductionSold.save()
            coupon.save()
        }

        if (coupon.numberOfUses != null && coupon.reductionSold.sold >= coupon.numberOfUses) {
            return false;
        }
        else {
            coupon.reductionSold.sold = coupon.reductionSold.sold + 1
            coupon.reductionSold.save()
            return true;
        }
    }

    void releaseCoupon(Coupon coupon) {
        if (coupon.reductionSold) {
            coupon.reductionSold.sold = Math.max(0, coupon.reductionSold.sold - 1)
            coupon.reductionSold.save()
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
            int index = (int)(v % DICTIONARY.length())
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
            company { eq ("id", seller.company.id)}
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

        coupon.categories?.clear()
        coupon.products?.clear()
        coupon.ticketTypes?.clear()

        if (coupon.rules?.size() > 0) {
            coupon.rules.each {
                coupon.removeFromRules(it)
            }
        }
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
        coupon.code = params.code ? params.code : generateCode()
        coupon.active = params.active ? params.active : true
        coupon.catalogWise = params.catalogWise ? params.catalogWise : false
        coupon.numberOfUses = params.numberOfUses
        coupon.startDate = params.startDate
        coupon.endDate = params.endDate

        // link with categories
        params.categories?.each { groovy.lang.Category c ->
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
            }
            else {
                coupon.errors = rule.errors
            }
        }

        if (!coupon.hasErrors() && coupon.validate()) {
            coupon.save()
        }
        return coupon
    }
}
