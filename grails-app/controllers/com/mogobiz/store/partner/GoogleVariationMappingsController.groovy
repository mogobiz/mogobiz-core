/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.GoogleVariationMapping
import com.mogobiz.store.domain.GoogleVariationType
import com.mogobiz.store.domain.GoogleVariationValue
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.Variation
import com.mogobiz.store.domain.VariationValue
import grails.converters.JSON
import grails.converters.XML
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional

class GoogleVariationMappingsController {

    AuthenticationService authenticationService

    @Transactional(readOnly = true)
    def show(Long id, String type, String value) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def _company = seller.company
        def _value = value?.trim()
        def _type = type?.trim()
        if(_type && _type.length() > 0){
            def mapping = findByType(_company, _type)
            withFormat {
                html mapping: mapping
                xml { render (mapping ? mapping : []) as XML }
                json { render (mapping ? mapping : []) as JSON }
            }
        }
        else if(_value && _value.length() > 0){
            def mapping = findByValue(_company, _value)
            withFormat {
                html mapping: mapping
                xml { render (mapping ? mapping : []) as XML }
                json { render (mapping ? mapping : []) as JSON }
            }
        }
        else{
            def mappings = GoogleVariationMapping.findAllByCompany(_company)
            withFormat {
                html mappings: mappings
                xml { render mappings as XML }
                json { render mappings as JSON }
            }
        }
    }

    @Transactional
    def save(String type, String value, String mappings){
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        if(!mappings || mappings.trim().length() == 0){
            response.sendError 400
            return
        }

        def _company = seller.company

        GoogleVariationMapping mapping

        def _value = value?.trim()
        if(_value && _value.length() > 0){
            mapping = findByValue(_company, _value)
            if(!mapping){
                mapping = new GoogleVariationMapping(
                        company:seller.company,
                        mappings: mappings.trim(),
                        value : GoogleVariationValue.findByValue(_value)
                )
            }
        }
        else{ // mapping can not references both google variation type and google variation value
            def _type = type?.trim()
            if(_type && _type.length() > 0){
                mapping = findByType(_company, _type)
                if(!mapping){
                    mapping = new GoogleVariationMapping(
                            company:seller.company,
                            mappings: mappings.trim(),
                            type : GoogleVariationType.findByXtype(_type)
                    )
                }
            }
        }

        if(!mapping.type && !mapping.value){
            response.sendError 400
            return
        }

        mapping.mappings = mappings
        mapping.validate()
        if(mapping.hasErrors()){
            def errors = []
            mapping.errors.allErrors.each {
                log.error(it)
                errors << [error:[code:it.code, message:it.getDefaultMessage()]]
            }
            withFormat {
                xml { render ([errors:errors] as Map) as XML }
                json { render ([errors:errors] as Map) as JSON }
            }
        }
        else{
            mapping.save()
            // update variations
            updateVariations(mapping)
            withFormat {
                xml { render mapping as XML }
                json { render mapping as JSON }
            }
        }
    }

    @Transactional
    def update(Long id, String mappings){
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        if(!id || !mappings || mappings.trim().length() == 0){
            response.sendError 400
            return
        }
        GoogleVariationMapping mapping = GoogleVariationMapping.get(id)
        if(!mapping){
            response.sendError 404
            return
        }
        mapping.mappings = mappings
        mapping.validate()
        if(mapping.hasErrors()){
            def errors = []
            mapping.errors.allErrors.each {
                log.error(it)
                errors << [error:[code:it.code, message:it.getDefaultMessage()]]
            }
            withFormat {
                xml { render ([errors:errors] as Map) as XML }
                json { render ([errors:errors] as Map) as JSON }
            }
        }
        else{
            mapping.save()
            updateVariations(mapping)
            withFormat {
                xml { render mapping as XML }
                json { render mapping as JSON }
            }
        }
    }

    @Transactional
    def delete(Long id){
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        if(!id || id <= 0){
            response.sendError 400
            return
        }
        GoogleVariationMapping mapping = GoogleVariationMapping.get(id)
        if(!mapping){
            response.sendError 404
            return
        }
        mapping.delete()
        withFormat {
            xml { render ([] as List) as XML }
            json { render ([] as List) as JSON }
        }
    }

    private void updateVariations(GoogleVariationMapping mapping){
        List<String> mappings = mapping.mappings.split(',') as List
        if(mapping.type){
            Collection<Variation> variations = Variation.executeQuery('from Variation variation where variation.category.company.id=:idCompany and variation.name in (:mappings)',
                    [idCompany:mapping.company.id, mappings:mappings])
            variations.each {variation ->
                log.info('update google variation for ' + variation.name)
                variation.googleVariationType = mapping.type.xtype
                variation.save()
            }
        }
        else{
            Collection<VariationValue> variationValues = Variation.executeQuery('select distinct values from Variation variation join fetch variation.variationValues as values where variation.category.company.id=:idCompany and values.value in (:mappings)',
                    [idCompany:mapping.company.id, mappings:mappings]) as Collection<VariationValue>
            variationValues.each {variation ->
                log.info('update google variation value for ' + variation.value)
                variation.googleVariationValue = mapping.value.value
                variation.save()
            }
        }
    }

    private GoogleVariationMapping findByType(Company _company, String _type){
        DetachedCriteria<GoogleVariationMapping> query = GoogleVariationMapping.where {
            company == company && type.xtype == _type
        }
        def args = [:] as Map
        query.find(args)
    }

    private GoogleVariationMapping findByValue(Company _company, String _value){
        DetachedCriteria<GoogleVariationMapping> query = GoogleVariationMapping.where {
            company == company && value.value == _value
        }
        def args = [:] as Map
        query.find(args)
    }
}
