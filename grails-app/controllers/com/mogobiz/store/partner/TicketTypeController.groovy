package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponseService;
import grails.converters.JSON
import grails.converters.XML

import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.Stock;
import com.mogobiz.store.domain.StockCalendar;
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.VariationValue;
import com.mogobiz.constant.IperConstant;
import com.mogobiz.json.RenderUtil;
import com.mogobiz.utils.IperUtil
import grails.transaction.Transactional;

/**
 * @version $Id $
 *
 */
public class TicketTypeController {
    AjaxResponseService ajaxResponseService

    def authenticationService

    @Transactional(readOnly = true)
    def show() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def sellerCompany = seller.company
        def ticketType = params.id ? TicketType.get(params.id) : null
        def paramProduct = ticketType ? ticketType.product : params['product']?.id ? Product.get(params['product']?.id) : null
        if (paramProduct && paramProduct.company == sellerCompany) {
            if (ticketType) {
                withFormat {
                    html ticketType: ticketType
                    xml { render ticketType as XML }
                    json {
                        render ajaxResponseService.prepareResponse(ticketType, ticketType.asMapForJSON()).asMap() as JSON
                    }
                }
            } else {
                def ticketTypes = TicketType.findAllByProduct(paramProduct)
                withFormat {
                    html ticketTypes: ticketTypes
                    xml { render ticketTypes as XML }
                    json {
                        def ret = []
                        ticketTypes.each { ticket ->
                            ret.add(ticket.asMapForJSON())
                        }
                        render ret as JSON
                    }
                }
            }
        } else if (params['name']) {
            Long catalogId = params['catalog']?.id?.toLong()

            List<TicketType> ticketTypes = TicketType.withCriteria {
                "product" {
                    "company" {
                        eq('id', seller.company.id)
                    }
                }
                if (params['name']) {
                    ilike('sku', '%' + params['name'] + '%')
                }
            }
            if (catalogId) {
                ticketTypes = ticketTypes.findAll { it.product.category.catalog.id == catalogId }
            }
            render ticketTypes as JSON
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def save() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (!product) {
            response.sendError 401
            return
        }
        def company = seller?.company
        if (product.company == company) {
            TicketType ticketType = new TicketType()
            String name = params['ticketType']?.name;
            def tickets = TicketType.findAllByNameAndProduct(name, product)
            if (tickets.isEmpty()) {
                ticketType.product = product
                ticketType.sku = params['ticketType']?.sku;
                ticketType.name = params['ticketType']?.name;
                ticketType.price = Float.parseFloat(params['ticketType']?.price);
                ticketType.minOrder = Integer.parseInt(params['ticketType']?.minOrder);
                ticketType.maxOrder = Integer.parseInt(params['ticketType']?.maxOrder);
                ticketType.xprivate = Boolean.parseBoolean(params['ticketType']?.xprivate);
                ticketType.description = params['ticketType']?.description;
                ticketType.startDate = RenderUtil.translateDateTimeToCalendar(params['ticketType']?.startDate, IperConstant.DATE_FORMAT)
                ticketType.stopDate = RenderUtil.translateDateTimeToCalendar(params['ticketType']?.stopDate, IperConstant.DATE_FORMAT)
                ticketType.availabilityDate = RenderUtil.translateDateTimeToCalendar(params['ticketType']?.availabilityDate, IperConstant.DATE_FORMAT)
                Long quantity = params['ticketType']?.stock ? new Long(params['ticketType']?.stock) : 0
                def stock = new Stock()
                stock.stock = quantity && quantity > 0 ? quantity : 0
                stock.stockUnlimited = params['ticketType']?.stockUnlimited == "true"
                stock.stockOutSelling = params['ticketType']?.stockOutSelling == "true"
                ticketType.stock = stock
                ticketType.variation1 = params['variation1']?.id ? VariationValue.get(params['variation1']?.id) : null
                ticketType.variation2 = params['variation2']?.id ? VariationValue.get(params['variation2']?.id) : null
                ticketType.variation3 = params['variation3']?.id ? VariationValue.get(params['variation3']?.id) : null

                if (ticketType.validate()) {
                    ticketType.save()
                }
                withFormat {
                    html ticketType: ticketType
                    xml { render ticketType as XML }
                    json {
                        render ajaxResponseService.prepareResponse(ticketType, ticketType.asMapForJSON()).asMap() as JSON
                    }
                }
            } else {
                response.sendError 404
            }
        } else {
            response.sendError 401
        }
    }

    @Transactional
    def update() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (!product) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def ticketType = params['ticketType']?.id ? TicketType.get(params['ticketType']?.id) : null
        def name = params['ticketType']?.name;
        if (ticketType && product.company == company) {
            def tickets = TicketType.findAllByNameAndProduct(name, product)
            def ret = tickets.isEmpty()
            if (!ret && tickets.size() == 1) {
                ret = tickets.iterator().next() == ticketType
            }
            if (ret) {
                //ticketType.properties = params['ticketType']
                ticketType.sku = params['ticketType']?.sku;
                ticketType.name = params['ticketType']?.name;
                ticketType.price = Float.parseFloat(params['ticketType']?.price);
                ticketType.minOrder = Integer.parseInt(params['ticketType']?.minOrder);
                ticketType.maxOrder = Integer.parseInt(params['ticketType']?.maxOrder);
                ticketType.xprivate = Boolean.parseBoolean(params['ticketType']?.xprivate);
                ticketType.description = params['ticketType']?.description;
                ticketType.startDate = RenderUtil.translateDateTimeToCalendar(params['ticketType']?.startDate, IperConstant.DATE_FORMAT)
                ticketType.stopDate = RenderUtil.translateDateTimeToCalendar(params['ticketType']?.stopDate, IperConstant.DATE_FORMAT)
                ticketType.availabilityDate = RenderUtil.translateDateTimeToCalendar(params['ticketType']?.availabilityDate, IperConstant.DATE_FORMAT)
                Long quantity = params['ticketType']?.stock ? new Long(params['ticketType']?.stock) : 0
                def stock = new Stock()
                stock.stock = quantity
                stock.stockUnlimited = params['ticketType']?.stockUnlimited == "true"
                stock.stockOutSelling = params['ticketType']?.stockOutSelling == "true"
                ticketType.variation1 = params['variation1']?.id ? VariationValue.get(params['variation1']?.id) : null
                ticketType.variation2 = params['variation2']?.id ? VariationValue.get(params['variation2']?.id) : null
                ticketType.variation3 = params['variation3']?.id ? VariationValue.get(params['variation3']?.id) : null

                //update related stockCalendar and Global stock
                def sockCalendarUpdated = true
                Long stockVariation = quantity > 0 ? quantity - (ticketType.stock.stock ? ticketType.stock.stock : 0) : null
                if (stockVariation && 0 != stockVariation) {
                    sockCalendarUpdated = updateStockCalendar(ticketType, stockVariation, product.calendarType)
                }

                if (sockCalendarUpdated) {
                    ticketType.stock = stock
                } else {
                    def error = [:]
                    error.put("stockError", IperConstant.ERREUR_INSUFFISENT_STOCK)
                    withFormat {
                        html error: error
                        json { render error as JSON }
                    }
                    return
                }

                if (ticketType.validate()) {
                    ticketType.save()
                }
            }
            withFormat {
                html ticketType: ticketType
                xml { render ticketType as XML }
                json {
                    render ajaxResponseService.prepareResponse(ticketType, ticketType.asMapForJSON()).asMap() as JSON
                }
            }
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def delete() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (!product) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def ticketType = params['ticketType']?.id ? TicketType.get(params['ticketType']?.id) : null
        if (ticketType && product.company == company) {
            StockCalendar sc = StockCalendar.findByTicketType(ticketType)
            if (sc != null) {
                if (sc.sold == 0) {
                    sc.delete()
                    ticketType.delete()
                } else {
                    response.sendError 401
                    return
                }
            } else {
                ticketType.delete()
            }
        }
        withFormat {
            xml { render [:] as XML }
            json { render [:] as JSON }
        }
    }

    @Transactional(readOnly = true)
    def exists() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (!product) {
            response.sendError 401
            return
        }
        def company = seller?.company
        def name = params['ticketType']?.name
        Boolean exists;
        if (product && product.company == company) {
            def tickets = TicketType.findAllByNameAndProduct(name, product)
            exists = !tickets.isEmpty()
            withFormat {
                html exists: exists
                xml { render exists as XML }
                json { render exists as JSON }
            }
        } else {
            response.sendError 404
        }
    }


    private boolean updateStockCalendar(TicketType ticketType, long stockVariation, ProductCalendar calendarType) {
        def today = IperUtil.today()
        def stockCalendars = StockCalendar.createCriteria().list {
            eq('ticketType', ticketType)
            if (calendarType != ProductCalendar.NO_DATE) {
                ge('startDate', today)
            }
        }
        //
        //  remaining = Math.max(0, stockCalendar.stock - stockCalendar.sold)
        def canUpdateStock = true
        stockCalendars.each {
            long remaining = Math.max(0, it.stock - it.sold)
            canUpdateStock = canUpdateStock && (remaining + stockVariation >= 0)
        }

        if (canUpdateStock) {
            stockCalendars.each { StockCalendar it ->
                it.stock = it.stock + stockVariation
                it.save()
            }
            return true
        } else {
            return false
        }
    }
}