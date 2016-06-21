/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.service.PagedList
import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.Tag
import grails.converters.JSON
import grails.transaction.Transactional

import javax.servlet.http.HttpServletResponse

class TagController {

    def authenticationService
    def tagService
    def ajaxResponseService

    @Transactional(readOnly = true)
    def list(PagedListCommand cmd) {
        Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(seller == null){
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            PagedList pagedList = tagService.list(seller.company, cmd)
            render ajaxResponseService.preparePage(pagedList.list, pagedList.totalCount, cmd) { Tag tag -> tag.asMapForJSON()} as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }

    @Transactional
    def save(Long tagId, Long ibeaconId) {
        Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(seller == null){
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            Tag tag = tagService.setIbeacon(seller, tagId, ibeaconId)
            AjaxResponse r = ajaxResponseService.prepareResponse(tag, tag?.asMapForJSON())
            render r.asMap() as JSON
        }
        catch (IllegalArgumentException ex) {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
            return
        }
    }
}
