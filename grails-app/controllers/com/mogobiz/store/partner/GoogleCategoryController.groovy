package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.domain.GoogleCategory
import grails.converters.JSON
import grails.converters.XML
import grails.gorm.DetachedCriteria
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class GoogleCategoryController {

    AuthenticationService authenticationService

    def show(String parentPath) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def company = seller.company
        def _lang = company.defaultLanguage ? company.defaultLanguage : RCU.getLocale(request)?.getLanguage()
        def _parentPath = parentPath?.trim()
        Collection<GoogleCategory> categories
        if(!_parentPath || _parentPath.length() == 0){
            categories = GoogleCategory.findAllByLangAndParentPathIsNull(_lang)
        }
        else{
            DetachedCriteria<GoogleCategory> query = GoogleCategory.where {
                lang == _lang && (parentPath == _parentPath || path == _parentPath)
            }
            def params = [:] as Map
            categories = query.list(params)
        }
        withFormat {
            html categories: categories
            xml { render categories as XML }
            json { render categories as JSON }
        }
    }

}
