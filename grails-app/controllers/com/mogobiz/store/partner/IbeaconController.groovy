/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner
import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.store.cmd.IBeaconCommand
import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.domain.Ibeacon
import com.mogobiz.store.domain.Seller
import grails.converters.JSON
import grails.orm.PagedResultList
import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse

class IbeaconController {

    def authenticationService
    def ibeaconService
    AjaxResponseService ajaxResponseService

    @Transactional(readOnly = true)
    def list(PagedListCommand cmd) {
        Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(seller == null){
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            PagedResultList list = ibeaconService.list(seller, cmd)
            render ajaxResponseService.preparePage(list, cmd) { Ibeacon beacon -> beacon.asMapForJSON()} as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }

    @Transactional
    def save(IBeaconCommand cmd) {
        Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(seller == null){
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            Ibeacon beacon = ibeaconService.save(seller, cmd)
            AjaxResponse r = ajaxResponseService.prepareResponse(beacon, beacon?.asMapForJSON())
            render r.asMap() as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }

    @Transactional
    def delete(Long id) {
        Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(seller == null){
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            Ibeacon beacon = ibeaconService.delete(seller, id)
            AjaxResponse r = ajaxResponseService.prepareResponse(beacon, beacon?.asMapForJSON())
            render r.asMap() as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }

}
