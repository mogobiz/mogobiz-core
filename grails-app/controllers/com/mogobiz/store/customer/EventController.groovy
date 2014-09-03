package com.mogobiz.store.customer

import com.mogobiz.service.RateService
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.mail.MailService

import java.text.SimpleDateFormat

import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

import com.mogobiz.store.domain.BOCart
import com.mogobiz.store.domain.BOCartItem
import com.mogobiz.store.domain.BOProduct
import com.mogobiz.store.domain.BOTicketType
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.ProductType
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.Stock
import com.mogobiz.store.domain.StockCalendar
import com.mogobiz.store.domain.TaxRate
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.TransactionStatus
import com.mogobiz.constant.IperConstant
import com.mogobiz.geolocation.domain.Poi
import com.mogobiz.json.RenderUtil
import com.mogobiz.service.CompanyService
import com.mogobiz.service.ProductService
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.Page
import com.mogobiz.utils.QRCodeUtils
import com.mogobiz.utils.SecureCodec
import com.sun.org.apache.xml.internal.security.utils.Base64

class EventController {

	def grailsApplication
	CompanyService companyService
	MailService mailService
	RateService rateService
	ProductService productService
	
	def g = new ApplicationTagLib()
	//def PageRenderer groovyPageRenderer

	def index() {
		chain(action:'getEvents',params:params);
	}

	/**
	 * get list of Events
	 * @param params the request params
	 */
	def getEvents() {
		def ipAddress = request.getHeader("Client-IP")
		log.info ipAddress
		if (!ipAddress) ipAddress = request.getHeader("X-Forwarded-For")
		if (!ipAddress) ipAddress = request.remoteAddr

		def idCategory = null
		def fullSearch = null
		if (params['event']?.idCategory) {
			idCategory = params['event']?.idCategory.toLong()
		}
		if (params['event']?.fullSearch){
			fullSearch = params['event']?.fullSearch
		}
		int offset;
		if (params?.pageOffset) {
			offset = params.pageOffset.toInteger() * IperConstant.NUMBER_EVENTS_PER_PAGE
		}
		else {
			offset = 0;
		}
		def companyVo = [:]
		String companyCode = params.store
		if (companyCode) {
			companyVo = this.loadCompanyVO(companyCode)
			//save the company
			if (companyVo) {
				session.companyVo = companyVo
			}
		}

		def today = IperUtil.resetCalendarTime(Calendar.getInstance())

		// filter les produits suivants les params
		def products = Product.createCriteria()
		def productsList = products.list (max: IperConstant.NUMBER_EVENTS_PER_PAGE,
				offset: offset) {
					eq('state', ProductState.ACTIVE)
					le('startDate',today)
					ge('stopDate',today)
					eq('xtype', ProductType.SERVICE)
					if(idCategory) {
						category { eq('id',idCategory) }
					}
					if (session.companyVo) {
						company{ eq('id',session.companyVo["id"]) }
					}
					if(fullSearch) {
						or {
							ilike("name","%" + fullSearch + "%")
							company{
								ilike("name","%" + fullSearch + "%")
							}
						}
					}
					order("startDate", "desc")
				}

		def resultliste = new ArrayList()
		def eventsList = new Page()
		productsList.each { prod ->
			def eventVO = toEventVO(prod)
			resultliste.add(eventVO)
		}

		eventsList = IperUtil.createListePagine(resultliste,productsList.totalCount,IperConstant.NUMBER_EVENTS_PER_PAGE, params?.pageOffset ? params.pageOffset.toInteger() : 0)

		//categorie
		def categories = Category.findAll()
		def categoriesList = []
		categories.each { categ ->
			def categoryVO = toCategoryVO(categ)
			categoriesList.add(categoryVO)
		}
		def result = [:]
		result.put('eventsList', eventsList)
		result.put('categories', categoriesList)
		String targetView='getEvents';
		boolean exists = false
		if (session.companyVo) {
			String path = "/site/"+session.companyVo.code+"/"+targetView + ".gsp";
			String realPath = servletContext.getRealPath("/");
			File realFile = new File(realPath, path);
			exists = realFile.exists()
			if (exists) {
				targetView = path
			}
		}
		return withFormat {
			html {
				render(view:targetView, model:[result:result])
			}
			xml  { render result as XML }
			json { render result as JSON }
		}
	}

	protected Map loadCompanyVO(String companyCode) {
		def companyVo = [:]
		def company = companyService.getCompany(companyCode)
		if (company) {
			companyVo = company.asMapForJSON()
			def liste = TaxRate.findAllByCompany(company)
			def taxRates = []
			liste.each {
				taxRates << RenderUtil.asMapForJSON([
					'id',
					'rate',
					'active',
					'state',
					'country',
					'country.name'
				], it) }
			companyVo.put("taxRates",taxRates)
		}
		// Security
		companyVo.paymentParam = null
		if (companyVo.buyster?.size() > 0)
			companyVo.buyster = "ok";
		if (companyVo.paypal?.size() > 0)
			companyVo.paypal = "ok";
		if (companyVo.kwixo?.size() > 0)
			companyVo.kwixo = "ok";
		// end of security
		return companyVo
	}


	/**
	 * return EventVO
	 * @param params the request params
	 */
	def getEvent() {
		def idEvent = params['event']?.idEvent?.toLong()
		if (idEvent) {
			def product = Product.get(idEvent)

			if (product) {
				def eventVO = toEventDetailVO(product);
				def ticketTypeList = TicketType.findAllByProduct(product)
				def ticketTypes = []
				ticketTypeList.each { ticketType ->
					def ticketTypeVO = toTicketTypeVO(ticketType)
					ticketTypes.add(ticketTypeVO)
				}
				eventVO.put('ticketTypes', ticketTypes)
				String targetView='getEvent';
				boolean exists = false
				if (session.companyVo) {
					String path = "/site/"+session.companyVo.code+"/"+targetView + ".gsp";
					String realPath = servletContext.getRealPath("/");
					File realFile = new File(realPath, path);
					exists = realFile.exists()
					if (exists) {
						targetView = path
					}
				}
				return withFormat {
					html {
						render(view:targetView, model:[eventVO:eventVO])
					}
					xml {  render eventVO as XML }
					json { render eventVO as JSON }
				}

			} else {
				response.sendError 403
				return
			}
		}
		else {
			response.sendError 403
			return
		}

	}

	/**
	 * save/update ticket information in the HTTP Session
	 * @param params the request params
	 */
	def register() {
		def tickets = [:]
		//check out tickets from session scope
		if (session.tickets){
			tickets = session.tickets
		}
		else {
			session.tickets = tickets
		}
		def newTickets
		if (params?.ticketInformation){
			String tick =params.ticketInformation
			newTickets = JSON.parse(tick)
		}

		def renderedTickets = [:]
		if (newTickets) {
			def mapEvent = [:]
			def liste = []
			newTickets.liste.each {
				def mapQuantity = [:]
				mapQuantity.put("quantity",it.quantity)
				mapEvent.put(it.idTicketType,mapQuantity)
				// to render

				def renderedTicket = [:]
				renderedTicket.put("idTicketType", it.idTicketType)
				renderedTicket.put("quantity", it.quantity)
				renderedTicket.put("ticketType", it.ticketType)
				renderedTicket.put("price", it.price)
				liste << renderedTicket
			}
			tickets.put(newTickets.idEvent, mapEvent)
			tickets.put("startDate",newTickets.startDate)
			//update tickets informations (idEvent,idTicketType,quantity) in session.tickets
			//to render
			renderedTickets.put("idEvent", newTickets.idEvent)
			renderedTickets.put("liste",liste)
		}
		session.renderedTickets = renderedTickets
		String targetView='register';
		boolean exists = false
		if (session.companyVo) {
			String path = "/site/"+session.companyVo.code+"/"+targetView + ".gsp";
			String realPath = servletContext.getRealPath("/");
			File realFile = new File(realPath, path);
			exists = realFile.exists()
			if (exists) {
				targetView = path
			}
		}
		return withFormat {
			html {
				render(view:targetView, model:[renderedTickets:renderedTickets])
			}
			xml {  render renderedTickets as XML }
			json { render renderedTickets as JSON }
		}
	}

	private List availableTickets(Product product, Map mySessionTickets, Calendar startDate){
		def available = []
		def tickets = mySessionTickets.keySet()

		if (product && (product.calendarType == ProductCalendar.NO_DATE || startDate)) {

			def globalQuantity = 0
			tickets.each { globalQuantity += mySessionTickets.get(it).get('quantity') }

			tickets.each{
				def idTicketType = (long) it
				def quantity = mySessionTickets.get(it).get('quantity')
				def result = [:]
				TicketType ticketType = TicketType.get(idTicketType)
				result.put("idTicketType", idTicketType)
				result.put("name", ticketType.name)
				if (ticketType && quantity != 0)
				{
					def stockCalendar = StockCalendar.createCriteria().get {
						eq ('product', product)
						eq ('calendarType', product.calendarType)
						if (product.calendarType != ProductCalendar.NO_DATE) { eq ('startDate', startDate) }
						eq ('ticketType', ticketType)
						eq ('stockCalendarGlobal',stockCalendarGlobal)
					}
					if (!stockCalendar) {
						def remainingStock = new Stock(stock:ticketType.stock.stock,
								stockUnlimited:ticketType.stock.stockUnlimited,
								stockOutSelling:ticketType.stock.stockOutSelling)
						stockCalendar = new StockCalendar(
								calendarType:product.calendarType,
								remainingStock:remainingStock,
								startDate:startDate,
								stockCalendarGlobal:stockCalendarGlobal,
								ticketType:ticketType,
								product:product)
						stockCalendar.save(flush:true)
					}

					if(!stockCalendar.remainingStock.stockUnlimited) {
						result.put("available",true)
					}else {
						result.put("available",true)
					}
				}
				else
				{
					result.put("available",true)
				}
				available << result
			}
		}
		return available
	}

	/**
	 * create entities for the current registration
	 * @param params the request params
	 */
	def completeRegistration() {
		session.idCompany = null;
		session.idTransaction = null;
		session.emailForPayment = null;
		session.emailingData = null;

		def regInformation = [:]
		regInformation.liste = []
		regInformation.idEvent = params.int('idEvent')
		int nbTickets = params.int('nbTickets')
		for (int i = 0; i < nbTickets; i++) {
			def ticket = [:]
			ticket.idTicketType = params.int("idTicketType_"+i)
			ticket.firstName = params["f_name_"+i]
			ticket.lastName = params["l_name_"+i]
			ticket.email = params["email_"+i]
			regInformation.liste << ticket
		}

		def mySessionTickets = session.tickets
		def mySessionticket = mySessionTickets?.get(regInformation.idEvent)
		if (mySessionticket){
			def product = Product.get(regInformation.idEvent)

			if (product) {
				def startDate = IperUtil.getStartPeriodeDate(product, IperUtil.parseCalendar(mySessionTickets.startDate, IperConstant.DATE_FORMAT))
				//used to calculate total price and total sales
				def totalPrice = 0
				def totalQuantity = 0
				//used to send emails
				def emailingData = []


				//test available tickets
				def listeAvailability = availableTickets(product, mySessionticket, startDate)
				def available = true
				listeAvailability.each {available &= it.available}
				if (!available) {
					def map = [:]
					map.put("result", IperConstant.ERREUR_INSUFFISENT_STOCK)
					map.put("listeAvailability", listeAvailability)
					String targetView='register';
					boolean exists = false
					if (session.companyVo) {
						String path = "/site/"+session.companyVo.code+"/"+targetView + ".gsp";
						String realPath = servletContext.getRealPath("/");
						File realFile = new File(realPath, path);
						exists = realFile.exists()
						if (exists) {
							targetView = path;
						}
					}
					return withFormat {
						html {
							render(view:targetView, model:[result:map])
						}
						xml {  render map as XML }
						json { render map as JSON }
					}
					return
				} else {
					def fromToEvent = IperUtil.getFromToEvent(product,IperUtil.parseCalendar(mySessionTickets.startDate,IperConstant.DATE_FORMAT))
					//create BOProduct
					BOProduct boProduct = new BOProduct(
							principal : true,
							product : product)
					boProduct.save(flush:true)

					//create BOTicketType
					regInformation.liste.each{
						TicketType ticketType = TicketType.get(it.idTicketType)
						if (ticketType){
							def emailContent = [:]
							BOTicketType boTicket = new BOTicketType(
									quantity : 1,
									price : ticketType.price,
									ticketType : ticketType.name,
									firstname : it.firstName,
									lastname : it.lastName,
									email : it.email,
									phone : "",
									bOProduct : boProduct,
									startDate : fromToEvent.startDate,
									endDate : fromToEvent.stopDate)
							boTicket.save(flush:true)

							def company = product.company
							boTicket.shortCode = "P" + boProduct.id + "T" + boTicket.id

							//gererate qrcode
							if (product.xtype != ProductType.TRANSPORT){
								String qrCodeContent = "EventId:"+product.id+";BoProductId:"+boProduct.id+";BoTicketId:"+boTicket.id
								qrCodeContent += ";EventName:" + product.name + ";EventDate:" + mySessionTickets.startDate+ ";FirstName:" + boTicket.firstname
								qrCodeContent += ";LastName:" +boTicket.lastname + ";Phone:" + boTicket.phone
								qrCodeContent += ";TicketType:" +boTicket.ticketType + ";shortCode:" + boTicket.shortCode
								qrCodeContent = SecureCodec.encrypt(qrCodeContent,product.company.aesPassword);
								ByteArrayOutputStream output = new ByteArrayOutputStream()
								QRCodeUtils.createQrCode(output, qrCodeContent, 256,"png")
								String qrCodeBase64 = Base64.encode(output.toByteArray())
								boTicket.qrcode = qrCodeBase64
								boTicket.qrcodeContent = qrCodeContent
							}

							boTicket.save()

							//decrement product stock
							productService.decrement(ticketType, boTicket.quantity, startDate)

							//increment total price and total sales
							totalPrice += boTicket.price * boTicket.quantity
							totalQuantity += boTicket.quantity
							// fill emailing data
							emailContent.put("email", boTicket.email)
							emailContent.put("eventName", product.name)
							emailContent.put("startDate", fromToEvent.startDate==null?null:RenderUtil.asMapForJSON(fromToEvent.startDate))
							emailContent.put("stopDate", fromToEvent.stopDate==null?null:RenderUtil.asMapForJSON(fromToEvent.stopDate))
							Poi poi = Poi.get(product?.poi?.id)
							emailContent.put("location", toEventLocationVO(poi))
							emailContent.put("type", boTicket.ticketType)
							emailContent.put("price",boTicket.price / 100)
							emailContent.put("qrcode", boTicket.qrcodeContent)
							emailContent.put("shortCode", boTicket.shortCode)
							emailingData << emailContent
						}
					}
					//update boProduct
					boProduct.price = totalPrice
					boProduct.nombreConsommationMax = totalQuantity!=0?totalQuantity:1
					boProduct.save(flush:true)

					//create Transaction
					BOCart transaction = new BOCart(
							code : 'TRANS' + Calendar.getInstance().getTimeInMillis(),
							date : Calendar.getInstance(),
							price : boProduct.price,
							status : TransactionStatus.PAYMENT_NOT_INITIATED)
					transaction.save(flush:true)

					//create Sale
					BOCartItem sale = new BOCartItem(
							code : "SALE_" + transaction.code,
							price: boProduct.price,
							hidden : false,
							nombreConsommationMax : totalQuantity!=0?totalQuantity:1,
							debut : product.startDate,
							fin : product.stopDate,
							transaction : transaction,
							bOProducts : [boProduct])
					sale.save(flush:true)

					//remove new tickets from session
					session.tickets = null

					// sauvegarde des infos en session pour le paiement
					session.idCompany = product.company.id
					session.idTransaction = transaction.id
					session.emailForPayment = emailingData[0].email
					session.emailingData = emailingData
					session.idSale = sale.id
					session.totalPrice = totalPrice
					session.bookTime = new Date().getTime()
					chain(action:'displayPayment');

				}
			} else {
				response.sendError 403
			}
		} else {
			response.sendError 403
		}
	}

	def traiterRetourPaiement() {
		BOCart transaction = BOCart.get(session.idTransaction);
		String targetView;
		if (TransactionStatus.COMPLETE == transaction.status)
		{
			//sending emails
			sendEmails(session.emailingData)

			session.idCompany = null;
			session.idTransaction = null;
			session.emailForPayment = null;
			session.emailingData = null;
			session.idSale = null;
			session.renderedTickets = null
			session.totalPrice = null
			session.bookTime = null
			targetView='paymentComplete';
		}
		else if (TransactionStatus.FAILED == transaction.status && !params.erreur)
		{
			BOCartItem sale = BOCartItem.get(session.idSale)
			sale.delete()
			session.idCompany = null;
			session.idTransaction = null;
			session.emailForPayment = null;
			session.emailingData = null;
			session.idSale = null;
			session.renderedTickets = null
			session.totalPrice = null
			session.bookTime = null
			targetView='paymentFailed';
		}
		else
		{
			BOCartItem sale = BOCartItem.get(session.idSale)
			sale.delete()
			targetView='displayPayment';
		}
		boolean exists = false
		if (session.companyVo) {
			String path = "/site/"+session.companyVo.code+"/"+targetView + ".gsp";
			String realPath = servletContext.getRealPath("/");
			File realFile = new File(realPath, path);
			exists = realFile.exists()
			if (exists) {
				targetView = path
			}
		}
		render(view: targetView, model: [erreur: params.erreur]);
	}

	/**
	 * render qrcode image from qrcode text
	 */
	def getQRCode() {
		def content = params.content
		if (content) {
			QRCodeUtils.createQrCode(response.outputStream,content.toString(), 256,"png")
			response.sendError HttpServletResponse.SC_OK
		}
		else
		{
			response.sendError HttpServletResponse.SC_BAD_REQUEST			
		}
	}

	/**
	 * render payment type for the given company
	 */
	def getPaymentTypes() {
		def companyVo = session.companyVo
		String companyCode = companyVo?.code;
		if (!companyVo) {
			companyCode = grailsApplication.config.defaultCompany
		}
		Company company = Company.findByCode(companyCode)
		def paymentTypes = RenderUtil.asMapForJSON([
			'paymentProvider',
			'paypal',
			'kwixo',
			'buyster',
			'cashOk',
			'checkOk',
			'cashText',
			'checkText'
		], company)
		// Security
		if (paymentTypes.buyster?.size() > 0)
			paymentTypes.buyster = "ok";
		if (paymentTypes.paypal?.size() > 0)
			paymentTypes.paypal = "ok";
		if (paymentTypes.kwixo?.size() > 0)
			paymentTypes.kwixo = "ok";
		// end of security

		withFormat {
			html paymentTypes:paymentTypes
			xml {  render paymentTypes as XML }
			json { render paymentTypes as JSON }
		}
	}

	/**
	 * @param product
	 * @return Map
	 */
	private Map toEventVO(Product product) {
		def map = [:]
		map.put("idEvent", product.id);
		map.put("eventName", product.name);
		Company company = Company.get(product.company.id)
		map.put("host", company?.name)
		map.put("startDate", RenderUtil.asMapForJSON(product.startDate, new SimpleDateFormat(IperConstant.DATE_FORMAT)))
		map.put("stopDate", RenderUtil.asMapForJSON(product.stopDate, new SimpleDateFormat(IperConstant.DATE_FORMAT)))
		map.put("price", product.price)
		// todo replace with rateService.format
		map.put("description", product.description)
		map.put("descriptionAsText", product.descriptionAsText)
		def g = new ApplicationTagLib()
		def url = g.createLink(controller: 'event', action: 'getEvent', params: ["event.idEvent" : product.id], absolute:true)
		map.put("eventURL", url)
		return map;

	}

	/**
	 * @param category
	 * @return Map
	 */
	private Map toCategoryVO(Category category) {
		def map = RenderUtil.asMapForJSON(['id', 'name'], category)
		def url = g.createLink(controller: 'event', action: 'getEvents', params: ["event.idCategory" : category.id], absolute:true)
		map.put("categoryURL",url)
		return map;
	}

	/**
	 * @param product
	 * @return Map
	 */
	private Map toEventDetailVO(Product product) {
		def map = new HashMap();
		map = toEventVO(product)
		Poi poi = Poi.get(product?.poi?.id)
		map.put("location", toEventLocationVO(poi));
		map.put("description", product.description);

		def tabPictures = [];
		product.getPictures().each{ Resource r ->
			tabPictures << IperUtil.getResourceVOSimple(r);
		}
		map.put("pictures", tabPictures)
		map.put("calendarType", product.calendarType)
		map.put("mapProvider", product?.company?.mapProvider)
		return map;
	}

	/**
	 * @param poi
	 * @return formated location
	 */
	private String toEventLocationVO(Poi poi){
		def strLocation
		if (poi){
			def country = Country.get(poi.country?.id)
			strLocation = poi.road1?poi.road1 + ", ":""
			strLocation += poi.road2?poi.road2 + ", ":""
			strLocation += poi.city?poi.city + " ":""
			strLocation += poi.postalCode?poi.postalCode + " ":""
			strLocation += poi.state?poi.state + ". " :""
			strLocation += country.name?country.name+".":""
		}
		return strLocation
	}


	/**
	 * @param boTicketType
	 * @return Map
	 */
	private Map toTicketTypeVO(TicketType ticketType) {
		def map = [:]
		map.put("idTicketType", ticketType.id)
		map.put("ticketType", ticketType.name)
		if (ticketType.variation1) {
			map.put("variation1Id", ticketType.variation1.id)
			map.put("variation1Name", ticketType.variation1.name)

		}
		if (ticketType.variation2) {
			map.put("variation2Id", ticketType.variation2.id)
			map.put("variation2Name", ticketType.variation2.name)

		}
		if (ticketType.variation3) {
			map.put("variation3Id", ticketType.variation3.id)
			map.put("variation3Name", ticketType.variation3.name)

		}
		map.put("salesEnd",  RenderUtil.asMapForJSON(ticketType.stopDate,new SimpleDateFormat(IperConstant.DATE_FORMAT)))
		map.put("price", ticketType.amount)
		map.put("minOrder", ticketType.minOrder)
		map.put("maxOrder", ticketType.maxOrder)
		return map
	}

	def getStockInfo() {
		Product product = params.productId?Product.get(params.productId):null
		if (product) {
			params.companyCode = product.company.code
			chain(controller:'store',action:'getStockInfo',params:params);
		}

	}

	/**
	 * @param liste
	 */
	private void sendEmails (liste) {
		liste.each {ligne->
			mailService.sendMail{
				to ligne.email
				subject 'Your Ticket : ' + ligne.eventName
				body (view:"/email/ticket",
						model:[eventName:ligne.eventName,
							startDate:ligne.startDate,
							stopDate:ligne.stopDate,
							startTime:ligne.startTime,
							stopTime:ligne.stopTime,
							location:ligne.location,
							price:ligne.price,
							type:ligne.type,
							shortCode:ligne.shortCode,
							qrcodeUrl:IperConstant.GET_QRCODE_URL + ligne.qrcode])
			}
		}
	}

	//get product calendar
	def getProductDates() {
		Product product = params.productId?Product.get(params.productId):null
		if (product) {
			params.companyCode = product.company.code
			chain(controller:'store',action:'getProductDates',params:params);
		}


	}
	def getProductTimes() {
		Product product = params.productId?Product.get(params.productId):null
		if (product) {
			params.companyCode = product.company.code
			chain(controller:'store',action:'getProductTimes',params:params);
		}
	}

	def listCarouselImages() {
		chain(controller:'store',action:'listCarouselImages',params:params);
	}

	def displayLogo() {
		chain(controller:'store',action:'displayLogo',params:params);
	}

	def displayCarousel() {
		chain(controller:'store',action:'displayCarousel',params:params);
	}
	
	def displayPayment() {
		String targetView='displayPayment';
		boolean exists = false
		long partieEntiere = session.totalPrice / 100
		def totalAsString = partieEntiere + "." + session.totalPrice % 100;
		if (session.companyVo) {
			String path = "/site/"+session.companyVo.code+"/"+targetView + ".gsp"
			String realPath = servletContext.getRealPath("/")
			File realFile = new File(realPath, path);
			exists = realFile.exists()
			if (exists) {
				targetView = path
			}     
		}
		return withFormat {
			html { 
				render(view:targetView, model:[renderedTickets: session.renderedTickets, totalPrice:totalAsString, bookTime :session.bookTime])
			}
			xml {  render session.renderedTickets as XML }
			json { render session.renderedTickets as JSON }
		}
	}
	
	def about() {}
	
	def contact() {}	
}
