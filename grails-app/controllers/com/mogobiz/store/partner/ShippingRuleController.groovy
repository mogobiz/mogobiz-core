/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.cmd.ShippingRuleCommand
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.ShippingRule
import grails.converters.JSON
import grails.orm.PagedResultList
import grails.transaction.Transactional
import org.springframework.validation.Errors

import javax.servlet.http.HttpServletResponse

class ShippingRuleController {
    def authenticationService
    def shippingRuleService
    def ajaxResponseService

    @Transactional(readOnly = true)
    def list(PagedListCommand cmd) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            PagedResultList list = shippingRuleService.list(seller, cmd)
            render ajaxResponseService.preparePage(list, cmd) { ShippingRule shippingRule -> shippingRule.asMapForJSON() } as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }

    @Transactional
    def save(ShippingRuleCommand cmd) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            ShippingRule shippingRule = shippingRuleService.save(seller, cmd)
            Errors errs =shippingRule.errors
            AjaxResponse r = ajaxResponseService.prepareResponse(shippingRule, shippingRule?.asMapForJSON())
            render r.asMap() as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }

    @Transactional
    def delete(Long id) {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            ShippingRule shippingRule = shippingRuleService.delete(seller, id)
            AjaxResponse r = ajaxResponseService.prepareResponse(shippingRule, shippingRule?.asMapForJSON())
            render r.asMap() as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }
}
