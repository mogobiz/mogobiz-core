/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.store.domain.GoogleVariationType
import com.mogobiz.store.domain.GoogleVariationValue
import grails.converters.JSON
import grails.converters.XML
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional

class GoogleVariationController {

    def authenticationService

    @Transactional(readOnly = true)
    def show(String type) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def _type = type?.trim()
        def variations
        if(!_type || _type.length() == 0){
            variations = GoogleVariationType.findAll()
        }
        else{
            DetachedCriteria<GoogleVariationValue> query = GoogleVariationValue.where {
                type.xtype == _type
            }
            def params = [:] as Map
            variations = query.list(params)
        }
        withFormat {
            html variations: variations
            xml { render variations as XML }
            json { render variations as JSON }
        }
    }

}
