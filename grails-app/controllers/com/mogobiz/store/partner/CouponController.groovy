/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 * 
 */
package com.mogobiz.store.partner

import com.mogobiz.store.cmd.coupon.CouponCreateUpdateCommand
import com.mogobiz.store.cmd.coupon.CouponListCommand
import com.mogobiz.store.domain.Coupon
import com.mogobiz.store.domain.Seller
import grails.converters.JSON
import grails.orm.PagedResultList
import grails.transaction.Transactional
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse

class CouponController {

    def couponService;
    def ajaxResponseService
    def authenticationService

    @Transactional(readOnly = true)
    def list(CouponListCommand cmd) {
        Seller seller = authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        }
        else {
            PagedResultList list = couponService.list(cmd)
            render ajaxResponseService.preparePage(list, cmd) { Coupon coupon -> coupon.asMapForJSON()} as JSON
        }
    }

    @Transactional
    def create(CouponCreateUpdateCommand cmd) {
        Seller seller = authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        }
        else {
            try {
                Coupon coupon = couponService.create(cmd);
                render ajaxResponseService.prepareResponse(coupon, coupon?.asMapForJSON(), RequestContextUtils.getLocale(request)).asMap() as JSON
            }
            catch (IllegalArgumentException ex) {
                log.error(ex.message);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST)
            }
        }
    }

    @Transactional
    def update(CouponCreateUpdateCommand cmd) {
        Seller seller = authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        }
        else {
            try {
                Coupon coupon = couponService.update(cmd);
                render ajaxResponseService.prepareResponse(coupon, coupon?.asMapForJSON(), RequestContextUtils.getLocale(request)).asMap() as JSON
            }
            catch (Exception ex) {
                log.error(ex.message);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST)
            }
        }
    }
}
