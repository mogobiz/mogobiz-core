package com.mogobiz.store.sale
import com.mogobiz.store.domain.*
import com.mogobiz.constant.IperConstant
import com.mogobiz.json.RenderUtil
import com.mogobiz.service.SaleService
import com.mogobiz.utils.IperUtil
import grails.converters.JSON

class SaleController {

	def authenticationService
    SaleService saleService

	def index = {
	}

	def initGetSalesByBuyer = {}

    /**
     * Retrieve BOCart and BOCartItem by criteria
     */
	def getSalesByCriteria = {Criteria criteria ->
        Seller user = authenticationService.retrieveAuthenticatedSeller()
		if (!user && !authenticationService.isAdministrator()){
			response.sendError 401
			return
		}
		if (criteria.validate()) {
			def saleDate = criteria.saleDate?RenderUtil.translateDateTimeToCalendar(criteria.saleDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR):null
            List<BOCart> listOfCarts = saleService.searchBOCartByCustomer(user.company, criteria.buyerLogin, saleDate, criteria.offset);
			def result = []
            listOfCarts?.each { BOCart cart ->
                def map = cart.asMapForJSON()
				def saleVOs = []
				saleService.searchBOCartItemByCode(cart, criteria.saleCode).each { BOCartItem item ->
					saleVOs << item.asMapForJSON()
				}
				map.put('items', saleVOs)
				result << map
			}
			render IperUtil.createListePagine(result, listOfCarts?.totalCount, IperConstant.NUMBER_EVENTS_PER_PAGE, criteria.pageOffset ? criteria.pageOffset : 0) as JSON
		} else {
			response.sendError 403
		}
	}

	//liste des vente par produit
	def getSalesByProduct = {
		def user = authenticationService.retrieveAuthenticatedSeller()
		if (!user && !authenticationService.isAdministrator()){
			response.sendError 401
			return
		}
		def startDate = params.startDate?RenderUtil.translateDateTimeToCalendar(params.startDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR):null
		def endDate = params.endDate?RenderUtil.translateDateTimeToCalendar(params.endDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR):null
		def product = params.productId?Product.get (params.productId):null
        int offset = params.pageOffset? params.pageOffset.toInteger() * IperConstant.NUMBER_SALES_PER_PAGE : 0
		System.out.println("---------------startDate"+startDate+"     endDate:    "+endDate+"     product     "+product);
		if (product && startDate && endDate) {
            List<BOCart> listOfCarts = saleService.searchBOCartByProduct(user.company, product, offset)
            def result = []
            listOfCarts?.each { BOCart cart ->
                def map = cart.asMapForJSON()
                def saleVOs = []
                saleService.searchBOCartItemByDate(cart, startDate, endDate).each { BOCartItem item ->
                    saleVOs << item.asMapForJSON()
                }
                map.put('items', saleVOs)
                result << map
            }
            render IperUtil.createListePagine(result, listOfCarts?.totalCount, IperConstant.NUMBER_EVENTS_PER_PAGE, params.pageOffset ? params.pageOffset.toInteger() : 0) as JSON
		} else {
			response.sendError 403
		}
	}

	//get sale detail : input = sale id, output = SaleVO
    /*
	def getSale = {
		def user = authenticationService.retrieveAuthenticatedSeller()
		if (!user && !authenticationService.isAdministrator()){
			response.sendError 401
			return
		}
		def sale = params.saleId?Sale.get(Long.parseLong(params.saleId)):null
		if (sale) {
			def products = []
			def tickets = []
			sale.bOProducts.each {
				products << it.product
				tickets << BOTicketType.findByBOProduct(it)
			}
			def saleVO = RenderUtil.asMapForJSON([
				'id',
				'code',
				'price',
				'debut',
				'fin'
			], sale)
			saleVO.put('products',products)
			saleVO.put('tickets',tickets)
			render saleVO as JSON
		} else {
			response.sendError 401
		}
	}*/

	//returns products by name and/or sku(code)
	def getProducts = {
		def user = authenticationService.retrieveAuthenticatedSeller()
		if (!user && !authenticationService.isAdministrator()){
			response.sendError 401
			return
		}
		def name = params.productName
		def code = params.productCode
		def products = Product.createCriteria().list () {
			if(name) { ilike("name","%" + name + "%") }
			if(code) { ilike("code","%" + code + "%") }
			order("startDate", "desc")
		}
		def result = []
		products.each {
			def productVO = RenderUtil.asMapForJSON(['id', 'code', 'name'], it)
			result << productVO
		}
		render result as JSON
	}
}


class Criteria {
	String buyerLogin
	String saleCode
	String saleDate
	Integer pageOffset
	int offset
	static constraints = {
		buyerLogin(blank:true, nullable:true)
		saleDate(blank:true, nullable:true)
		saleCode(blank:true, nullable:true, validator:{val, obj ->
			if (obj?.pageOffset) {
				obj.offset = obj.pageOffset.toInteger() * IperConstant.NUMBER_SALES_PER_PAGE
			}
			else {
				obj.offset = 0;
			}
			if(obj.buyerLogin || obj.saleCode || obj.saleDate){
				return true
			}
			else{
				return false
			}
		})
		
	}
}