/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.validator

import com.mogobiz.store.domain.BOProduct;
import com.mogobiz.store.domain.BOTicketType;
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Consumption;
import com.mogobiz.store.domain.DatePeriod;
import com.mogobiz.store.domain.IntraDayPeriod;
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.Product
import com.mogobiz.constant.IperConstant;
import com.mogobiz.json.RenderUtil;
import com.mogobiz.utils.IperUtil
import grails.transaction.Transactional;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException
import grails.converters.JSON

import java.text.SimpleDateFormat

import org.apache.shiro.authc.UsernamePasswordToken

/**
 * @author hedi.abidi@ebiznext.com
 *
 */
class ValidatorController {

	def authenticationService
	static defaultAction = "loginValidator"

	def index = { }

	@Transactional
	def loginValidator() {
		SecurityUtils.subject?.logout()
		def user = null
		def authToken = new UsernamePasswordToken(params.username, params.password)
		try {
			SecurityUtils.subject.login(authToken)
			if (authenticationService.isValidator())
			{
				user = authenticationService.retrieveAuthenticatedUser()
			}
		}
		catch (AuthenticationException ex){
			response.sendError 403
			return
		}
		if (user) {
			redirect(action: "getEventsData")
		} else {
			response.sendError 403
		}
	}

	@Transactional(readOnly = true)
	def getEventsData() {
		def user = request.user?request.user:authenticationService.retrieveAuthenticatedUser()
		if(user == null || !authenticationService.isValidator()){
			response.sendError 403
			return
		}

		def productId = params.long("event.id")
		def eventList = []
		def consumList = []
		def products = []
		def today = IperUtil.today()
		if (productId != null) {
			def product = Product.get(productId)
			if (product!=null && product?.company ==user?.company) {
				products = [product]}
		} else {
			products = getProductEventsByDateAndCompany(today,user?.company)
		}

		products.each { prod ->
			def product = formatProduct(prod)
			eventList.add(product)
		}
		def consumptions = getTicketConsumption(products as Product[])
		consumptions.eachWithIndex { consum,i ->
			def consumption = formatConsumption(consum,i)
			consumList.add(consumption)
		}

		def map = [:]
		map.put("eventList",eventList)
		map.put("consumList",consumList)

		//
		def company = user.company
		map.put("aesPassword", company.aesPassword);
		map.put("onlineValidation", company.onlineValidation);
		

		render map as JSON
	}

	@Transactional
	def synchronize() {
		def user = request.user?request.user:authenticationService.retrieveAuthenticatedUser()
		if(user == null || !authenticationService.isValidator()){
			response.sendError 403
			return
		}

		def checkedConsumptionList
		def uncheckedConsumptionList
		if (params?.checkedConsumList){
			checkedConsumptionList = JSON.parse(params.checkedConsumList)
		}
		if (params?.uncheckedConsumList){
			uncheckedConsumptionList = JSON.parse(params.uncheckedConsumList)
		}

		//adding checked consumptions
		if (checkedConsumptionList) {
			checkedConsumptionList.each {
				def boTicketType = BOTicketType.get(it.BoTicketId.toLong())
				def boProduct = BOProduct.get(it.BoProductId.toLong())
				if (boTicketType && boProduct) {
					def consumption = getConsumptionByBOTicketTypeAndBOProduct(boTicketType, boProduct)
					if (consumption == null) {
						Calendar chekinCalendar = RenderUtil.translateDateTimeToCalendar(it.date, IperConstant.DATE_FORMAT)
						consumption = new Consumption(
								date:chekinCalendar?chekinCalendar:Calendar.getInstance(),
								bOTicketType:boTicketType)
								.save()
						boProduct.addToConsumptions(consumption)
					}
				}
			}
		}
		//deleting unchecked consumptions
		if (uncheckedConsumptionList) {
			uncheckedConsumptionList.each {
				def boTicketType = BOTicketType.get(it.BoTicketId.toLong())
				def boProduct = BOProduct.get(it.BoProductId.toLong())
				if (boTicketType && boProduct) {
					def consumption = getConsumptionByBOTicketTypeAndBOProduct(boTicketType, boProduct)
					if (consumption != null) {
						boProduct.removeFromConsumptions(consumption)
						consumption.delete()
					}
				}
			}
		}
//		chain(action:'getEventsData');
		redirect(action: "getEventsData")
	}

	@Transactional(readOnly = true)
	def getEventDateTime() {
		def user = request.user?request.user:authenticationService.retrieveAuthenticatedUser()
		if(user == null || !authenticationService.isValidator()){
			response.sendError 403
			return
		}
        long eventId = params.long("event.id")
		def events = []
		def result = [:]
		def today = IperUtil.today()
		result.put("date",RenderUtil.asMapForJSON(today, new SimpleDateFormat(IperConstant.DATE_FORMAT_WITHOUT_HOUR)))
		def products = getProductEventByDateAndCompany(eventId, today,user?.company)
		products.each { prod ->
			def event = [:]
			event.put("eventId", prod.id)
			event.put("calendarType", prod.calendarType)
			def included
			if (prod.calendarType != ProductCalendar.NO_DATE) {
				def eventTimes = []
				def listIncluded = IntraDayPeriod.createCriteria().list{
					eq('product.id', prod.id)
					le('startDate', today)
					ge('endDate', today)
				}
				def listExcluded = DatePeriod.createCriteria().list{
					eq('product.id', prod.id)
					le('startDate',today)
					ge('endDate',today)
				}
				included = false
				if (!IperUtil.isDateExcluded(listExcluded, today)) {
					listIncluded.each { period ->
						if (IperUtil.isDateIncluded([period], today)){
							def eventFromTo = [:]
							eventFromTo.put("startTime", RenderUtil.asMapForJSON(period.startDate, new SimpleDateFormat(IperConstant.TIME_FORMAT)))
							eventFromTo.put("endTime", RenderUtil.asMapForJSON(period.endDate, new SimpleDateFormat(IperConstant.TIME_FORMAT)))
							eventTimes << eventFromTo
							included = true
						}
					}
					if (included && prod.calendarType == ProductCalendar.DATE_TIME){
						event.put("eventTimes",eventTimes)
					}
				}
			} else {
				included = true
			}
			if (included){
				events << event
			}

		}
		result.put("events", events)
		render result as JSON
	}


	/**
	 * @param products
	 * @return consumptions 
	 */
	private Object[] getTicketConsumption(Product[] products) {
		String requeteConsumption = "Select distinct consumption"
		requeteConsumption += " From BOProduct bOprod left join bOprod.consumptions consumption"
		requeteConsumption += " right join consumption.bOTicketType bott"
		requeteConsumption += " WHERE bott=bott1 AND bOprod=bOprod1"
		String requete = "SELECT distinct p1.id, bott1.qrcodeContent, bott1.firstname, bott1.lastname, bott1.birthdate, bott1.ticketType, (" + requeteConsumption + "), bOprod1.id, bott1.id, bott1.phone, bott1.shortCode"
		requete += " FROM BOCartItem s join s.bOProducts bOprod1 join bOprod1.product p1, BOTicketType bott1 "
		requete += " WHERE p1 in (:products) and bott1.bOProduct=bOprod1"

		def consumptions = Product.executeQuery(requete, [products:products])
		return consumptions
	}


	/**
	 * @param product
	 * @return formated map
	 */
	private Map formatProduct(Product product) {
		def map = [:]
		def timeFormat = new SimpleDateFormat(IperConstant.TIME_FORMAT)
		def dateWithoutHour = new SimpleDateFormat(IperConstant.DATE_FORMAT_WITHOUT_HOUR)
		map.put("id", product.id);
		map.put("title", product.name);
		map.put("startDate",  RenderUtil.asMapForJSON(product.startDate,dateWithoutHour));
		map.put("endDate", RenderUtil.asMapForJSON(product.stopDate,dateWithoutHour));
		map.put("startTime",  RenderUtil.asMapForJSON(product.startDate,timeFormat));
		map.put("endTime", RenderUtil.asMapForJSON(product.stopDate,timeFormat));
		map.put("sold", product.nbSales);
		return map;
	}

	/**
	 * @param consumption
	 * @return  formated map
	 */
	private Map formatConsumption(consumption, index) {
		def map = [:]
		map.put("id", index);
		map.put("EventId", consumption[0]);
		map.put("qrcode",  consumption[1]);
		map.put("FirstName", consumption[2]);
		map.put("LastName",  consumption[3]);
		map.put("birthDate", consumption[4]);
		map.put("ticketType", consumption[5]);
		map.put("checkedIn", consumption[6]==null?false:true);
		map.put("checkedInDate", consumption[6]==null?null: RenderUtil.asMapForJSON(consumption[6].date));
		map.put("BoProductId", consumption[7]);
		map.put("BoTicketId", consumption[8]);
		map.put("Phone", consumption[9]);
		map.put("shortCode", consumption[10]);		
		map.put("sync", true);
		return map;
	}

	//event_id to EventId,to add EventDate and FirstName instead of firstName and LastName instead of lastName and add field Phone


    /**
     * @param date
     * @param company
     * @return products
     */
    private Product[] getProductEventsByDateAndCompany(Calendar date, Company company) {
        String requete = " FROM Product p "
        requete += " WHERE p.company.id = " + company.id
        requete += " AND cast(p.startDate as date) <= :date"
        requete += " AND cast(p.stopDate as date) >= :date"
        def products = Product.executeQuery(requete, [date:date.getTime()])
        return products
    }

    /**
     * @param date
     * @param company
     * @param eventId
     * @return products
     */
    private Product[] getProductEventByDateAndCompany(long eventId, Calendar date, Company company) {
        String requete = " FROM Product p "
        requete += " WHERE p.company.id = " + company.id
        requete += " AND p.id = " + eventId
        requete += " AND cast(p.startDate as date) <= :date"
        requete += " AND cast(p.stopDate as date) >= :date"
        def products = Product.executeQuery(requete, [date:date.getTime()])
        return products
    }

    /**
	 * @param boTicketType
	 * @param boProduct
	 * @return
	 */
	private Consumption getConsumptionByBOTicketTypeAndBOProduct(BOTicketType boTicketType,BOProduct boProduct) {
		String requeteConsumption = "Select consumption"
		requeteConsumption += " From BOProduct bOprod left join bOprod.consumptions consumption"
		requeteConsumption += " right join consumption.bOTicketType bott"
		requeteConsumption += " WHERE bott=:boTicketType AND bOprod=:boProduct"
		def consumptions = Product.executeQuery(requeteConsumption, [boTicketType:boTicketType,boProduct:boProduct])
		if (consumptions.size() > 0)
			return consumptions.get(0);
		return null;
	}
}
