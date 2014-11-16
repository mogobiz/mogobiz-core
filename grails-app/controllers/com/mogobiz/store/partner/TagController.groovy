package com.mogobiz.store.partner
import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.ajax.AjaxResponseService
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
    AjaxResponseService ajaxResponseService

    @Transactional(readOnly = true)
    def list(PagedListCommand cmd) {
        Seller seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
        if(seller == null){
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        try {
            PagedList pagedList = tagService.list(seller, cmd)
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
