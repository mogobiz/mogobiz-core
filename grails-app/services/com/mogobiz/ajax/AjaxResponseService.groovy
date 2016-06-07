/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 * 
 */
package com.mogobiz.ajax

import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.utils.Page
import grails.orm.PagedResultList

/**
 * @author smanciot
 *
 */
class AjaxResponseService {
	
	def messageSource

    Page preparePage(PagedResultList pagedList, PagedListCommand cmd, Closure transform) {
        return preparePage(pagedList, pagedList.totalCount, cmd, transform)
    }

    Page preparePage(List pagedList, int totalCount, PagedListCommand cmd, Closure transform) {
        long pageOffset = (cmd.pageOffset ? Math.max(0, cmd.pageOffset) : 0)
        long pageSize = cmd.pageSize ? Math.max(0, cmd.pageSize) : totalCount

        Page page = new Page()
        page.list = pagedList.collect() { transform(it) }
        page.pageSize = pagedList.size()
        page.totalCount = totalCount
        page.maxItemsPerPage = pageSize
        page.pageOffset = pageOffset
        page.pageCount = (pageSize == 0 ? 1 : (int)(page.totalCount / pageSize)+((page.totalCount % pageSize) > 0 ?1:0))
        page.hasPrevious = (pageOffset > 0)
        page.hasNext = (pageOffset != (page.pageCount -1))
        return page
    }

    AjaxResponse prepareResponse(domainInstance, data, Locale locale = null){
		AjaxResponse ajaxResponse = new AjaxResponse()
		ajaxResponse.success = !domainInstance.hasErrors()
		if(!ajaxResponse.success){
			domainInstance.errors?.allErrors?.each{error ->
				ajaxResponse.errors."${error.field}" = messageSource.getMessage(error, locale)
			}
		}
		if(ajaxResponse.success){
			ajaxResponse.data = data
		}
		return ajaxResponse
	}

	AjaxResponse addError(AjaxResponse ajaxResponse, String field, String error, Object [] args = null, Locale locale = null) {
		ajaxResponse.success = false
		ajaxResponse.errors."${field}" = messageSource.getMessage(field + "." + error, args, locale)
		return ajaxResponse
	}
}
