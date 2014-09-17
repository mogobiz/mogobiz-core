// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service

import com.mogobiz.common.client.BulkResponse
import com.mogobiz.common.client.Client
import com.mogobiz.common.client.ClientConfig
import com.mogobiz.common.rivers.spi.AbstractRiver
import com.mogobiz.common.rivers.spi.RiverConfig
import com.mogobiz.constant.IperConstant
import com.mogobiz.elasticsearch.rivers.ESRivers
import com.mogobiz.elasticsearch.rivers.spi.ESRiver
import com.mogobiz.json.RenderUtil
import com.mogobiz.store.domain.*
import com.mogobiz.store.exception.CurrencyRateException
import com.mogobiz.store.exception.InsufficientStockException
import com.mogobiz.store.exception.ProductNotFoundException
import com.mogobiz.utils.Html2Text
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.Page
import grails.orm.PagedResultList
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import rx.observables.BlockingObservable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import java.util.concurrent.TimeUnit

/**
 *
 */
class ProductService
{
	UuidDataService uuidDataService;	
	SanitizeUrlService sanitizeUrlService
	RateService rateService;
	TaxRateService taxRateService

    ESRiver river = ESRivers.instance.loadRiver("product")

    def servletContext = SCH.servletContext

	/**
	 * Extracts the "maxItemsPerPage" of the map or returns
	 * {@link IperConstant#NUMBER_PRODUCT_PER_PAGE} if not set
	 * @param criteria
	 * @return
	 */
	private Integer extractMaxItemsPerPage(Map criteria) {
		Integer maxItemsPerPage = criteria["maxItemsPerPage"];
		if (!maxItemsPerPage) {
			maxItemsPerPage = IperConstant.NUMBER_PRODUCT_PER_PAGE;
		}
		return maxItemsPerPage;
	}
	
	/**
	 * Extracts the "pageOffset" of the map or returns
	 * 0 if not set
	 * @param criteria
	 * @return
	 */
	private Integer extractPageOffset(Map criteria) {
		Integer pageOffset = criteria["pageOffset"]
		if (!pageOffset) {
			pageOffset = 0
		}
		return pageOffset;
	}

	/**
	 * Transforms the product into a map (with formated prices)
	 * @param locale
	 * @param currencyCode
	 * @param product
	 * @return
	 * @throws CurrencyRateException
	 */
	protected Map renderProduct(Locale locale, String currencyCode, Product product) throws CurrencyRateException {
		Map mapProduct = product.asMapForJSON(null, null, locale?.language)
		// formatting price
		mapProduct["price"] = renderProductPrice(locale, currencyCode, product)
		return mapProduct
	}
	
	/**
	 * Transforms the price of product into a map
	 * @param locale
	 * @param locale
	 * @param currencyCode
	 * @param product
	 * @return
	 * @throws CurrencyRateException
	 */
	protected Map renderProductPrice(Locale locale, String currencyCode, Product product) throws CurrencyRateException {
		// formatting price
		Float taxRate = taxRateService.findTaxRateByProduct(product, locale?.country)
		Long endPrice = taxRateService.calculateEndPrix(product.price, taxRate)
		Map price = [:]
		price["price"] = rateService.format(product.price, currencyCode, locale);
		price["taxRate"] = taxRate
		price["endPrice"] = rateService.format(endPrice, currencyCode, locale);
		return price
	}

	/**
	 * Transforms the PagedResultList of Product into a Page of map (with formated prices)
	 * @param locale
	 * @param currencyCode
	 * @param maxItemsPerPage
	 * @param pageOffset
	 * @param pagedListe
	 * @return
	 * @throws CurrencyRateException
	 */
	private Page renderPagedResultList(Locale locale, String currencyCode, Integer maxItemsPerPage, Integer pageOffset, PagedResultList pagedListe) throws CurrencyRateException {
		List<Map> result = [];
		pagedListe.list?.each { Product p ->
			result << renderProduct(locale, currencyCode, p);
		}

		return IperUtil.createListePagine(result, pagedListe.totalCount, maxItemsPerPage, pageOffset)
	}
	
	/**
	 * This method searches a list of active product for the company, which is salable today and meet the criteria. Criteria are :<br/>
	 * - maxItemsPerPage : Integer (Optional) : The default value is {@link IperConstant.NUMBER_PRODUCT_PER_PAGE}<br/>
	 * - pageOffset : Integer (Optional) : The default value is 0<br/>
	 * - xtype : String (Optional) : If set, this method searches the products corresponding to the given xtype<br/>
	 * - name : String (Optional) : If set, this method searches the products containing to the given name<br/>
	 * - code : String (Optional) : If set, this method searches the products corresponding to the given code<br/>
	 * - categoryId : Long (Optional) : If set, this method searches the products corresponding to the given categoryId<br/>
	 * - brandId : Long (Optional) : If set, this method searches the products corresponding to the given brandId<br/>
	 * - tagName : String (Optional) : If set, this method searches the products corresponding to the given tagName<br/>
	 * - priceMin : Long (Optional) : If set, this method searches the products which price is greater or equal than the given priceMin<br/>
	 * - priceMax : Long (Optional) : If set, this method searches the products which price is less or equal than the given priceMax<br/>
	 * - creationDateMin : Calendar (Optional) : If set, this method searches the products which create date is greater or equal than the given creationDateMin<br/>
	 * - orderBy : String (Optional) : the default value is "startDate"<br/>
	 * - orderDirection : String (Optional) : the default value is "desc"<br/>
	 * @param locale
	 * @param currencyCode
	 * @param companyId
	 * @param criteria
	 * @return
	 * @throws CurrencyRateException
	 */
	Page search(Locale locale, String currencyCode, long companyId, Map criteria) throws CurrencyRateException {
		def today = IperUtil.today()

		Integer maxItemsPerPage = extractMaxItemsPerPage(criteria)
		Integer pageOffset = extractPageOffset(criteria)
		
		PagedResultList pagedListe = Product.createCriteria().list (max: maxItemsPerPage, offset: pageOffset * maxItemsPerPage) {
			company { eq('id', companyId) }
			eq('state', ProductState.ACTIVE)
			le('startDate',today)
			ge('stopDate',today)
			if (criteria["xtype"]) {
				eq('xtype', ProductType.valueOf(criteria["xtype"]))
			}
			if (criteria["name"]) {
				ilike('name', "%" + criteria["name"] + "%")
			}
			if (criteria["code"]) {
				eq('code', criteria["code"])
			}
			if (criteria["categoryId"]) {
				category { eq('id', Long.valueOf(criteria["categoryId"])) }
			}
			if (criteria["brandId"]) {
				brand { eq('id', Long.valueOf(criteria["brandId"])) }
			}
			if (criteria["tagName"]) {
				tags { eq('name', criteria["tagName"]) }
			}			
			if (criteria["priceMin"] && criteria["priceMax"]) {
				between('price', Long.valueOf(criteria["priceMin"]), Long.valueOf(criteria["priceMax"]))
			}
			else if (criteria["priceMin"]) {
				ge('price', Long.valueOf(criteria["priceMin"]))
			}
			else if (criteria["priceMax"]) {
				le('price', Long.valueOf(criteria["priceMax"]))
			}
			if (criteria["creationDateMin"]) {
				ge('creationDate', criteria["creationDateMin"])
			}
			order(criteria["orderBy"] ?: "startDate", criteria["orderDirection"] ?: "desc")
		}
		
		return renderPagedResultList(locale, currencyCode, maxItemsPerPage, pageOffset, pagedListe);
	}

	/**
	 * This method searches a list of active product for the company, which is featured today and meet the criteria. Criteria are :<br/>
	 * - maxItemsPerPage : Integer (Optional) : The default value is {@link IperConstant.NUMBER_PRODUCT_PER_PAGE}<br/>
	 * - pageOffset : Integer (Optional) : The default value is 0<br/>
	 * - xtype : String (Optional) : If set, this method searches the products corresponding to the given xtype<br/>
	 * - name : String (Optional) : If set, this method searches the products containing to the given name<br/>
	 * - code : String (Optional) : If set, this method searches the products corresponding to the given code<br/>
	 * - categoryId : Long (Optional) : If set, this method searches the products corresponding to the given categoryId<br/>
	 * - brandId : Long (Optional) : If set, this method searches the products corresponding to the given brandId<br/>
	 * - tagName : String (Optional) : If set, this method searches the products corresponding to the given tagName<br/>
	 * - priceMin : Long (Optional) : If set, this method searches the products which price is greater or equal than the given priceMin<br/>
	 * - priceMax : Long (Optional) : If set, this method searches the products which price is less or equal than the given priceMax<br/>
	 * - creationDateMin : Calendar (Optional) : If set, this method searches the products which create date is greater or equal than the given creationDateMin<br/>
	 * - orderBy : String (Optional) : the default value is "startDate"<br/>
	 * - orderDirection : String (Optional) : the default value is "desc"<br/>
	 * @param locale
	 * @param currencyCode
	 * @param companyId
	 * @param criteria
	 * @return
	 * @throws CurrencyRateException
	 */
	Page searchFeatured(Locale locale, String currencyCode, long companyId, Map criteria) throws CurrencyRateException {
		def today = IperUtil.today()
		
		Integer maxItemsPerPage = extractMaxItemsPerPage(criteria)
		Integer pageOffset = extractPageOffset(criteria)
		
		PagedResultList pagedListe = Product.createCriteria().list (max: maxItemsPerPage, offset: pageOffset * maxItemsPerPage) {
			company { eq('id', companyId) }
			eq('state', ProductState.ACTIVE)
			le('startFeatureDate',today)
			ge('stopFeatureDate',today)
			if (criteria["xtype"]) {
				eq('xtype', ProductType.valueOf(criteria["xtype"]))
			}
			if (criteria["name"]) {
				ilike('name', "%" + criteria["name"] + "%")
			}
			if (criteria["code"]) {
				eq('code', criteria["code"])
			}
			if (criteria["categoryId"]) {
				category { eq('id', criteria["categoryId"]) }
			}
			if (criteria["brandId"]) {
				brand { eq('id', criteria["brandId"]) }
			}
			if (criteria["tagName"]) {
				tags { eq('name', criteria["tagName"]) }
			}
			if (criteria["priceMin"] && criteria["priceMax"]) {
				between('price', criteria["priceMin"], criteria["priceMax"])
			}
			else if (criteria["priceMin"]) {
				ge('price', criteria["priceMin"])
			}
			else if (criteria["priceMax"]) {
				le('price', criteria["priceMax"])
			}
			if (criteria["creationDateMin"]) {
				ge('creationDate', criteria["creationDateMin"])
			}
			order(criteria["orderBy"] ?: "startDate", criteria["orderDirection"] ?: "desc")
		}
		
		return renderPagedResultList(locale, currencyCode, maxItemsPerPage, pageOffset, pagedListe);
	}

	/**
	 * Returns the map corresponding to the given product.
	 * If the parameter "addToHistory" is null or true, the given productId is add to the user's history list
	 * @param locale
	 * @param currencyCode
	 * @param companyId
	 * @param productId
	 * @param addToHistory
	 * @return
	 * @throws CurrencyRateException
	 * @throws ProductNotFoundException
	 */
	Map getProduct(Locale locale, String currencyCode, long companyId, long productId, Boolean addToHistory) throws CurrencyRateException, ProductNotFoundException {
		def today = IperUtil.today()
		Product product = Product.createCriteria().get {
			eq('id',productId)
			company{ eq('id',companyId) }
			eq('state', ProductState.ACTIVE)
			le('startDate',today)
			ge('stopDate',today)
		}
		
		if (product == null) {
			throw new ProductNotFoundException("The product " + productId + "was not found")
		}
		else
		{
			Map map = renderProduct(locale, currencyCode, product)
			//adding ticket types
			def ticketTypes = []
			TicketType.createCriteria().list {eq('product.id', product.id)}.each { TicketType ticketType ->
				Map mapTicketType = ticketType.asMapForJSON(["id", "amount", "startDate", "stopDate", "minOrder", "name", "maxOrder"], null, locale?.language)

				//adding stock		
				Stock stock = ticketType.stock;
				if (stock != null)
				{
					mapTicketType["initialStock"] = stock.stock
					mapTicketType["stockUnlimited"] = stock.stockUnlimited
					mapTicketType["stockOutSelling"] = stock.stockOutSelling
					mapTicketType["stockDisplay"] = product.stockDisplay
					
					if (!stock.stockUnlimited) {
						if (product.calendarType == ProductCalendar.NO_DATE) {
							StockCalendar stockCalendar = StockCalendar.findByTicketType(ticketType)
							if (stockCalendar) {
								mapTicketType["stock"] = Math.max(0, stockCalendar.stock - stockCalendar.sold);							
							}
							else
							{
								mapTicketType["stock"] = stock.stock								
							}							
						}
						else {
							List<Map> stockByDateTime = []
							StockCalendar.findAllByTicketType(ticketType).each { StockCalendar stockCalendar ->
								Map mapStockByDateTime = [:]
								mapStockByDateTime["dateTime"] = IperUtil.formatCalendar(stockCalendar.startDate)
								mapStockByDateTime["stock"] =  Math.max(0, stockCalendar.stock - stockCalendar.sold);
								stockByDateTime << mapStockByDateTime
							}
							mapTicketType["stockByDateTime"] = stockByDateTime
						}
					}
				}
				ticketTypes << mapTicketType
			}
			map['ticketTypes'] =  ticketTypes
			
			// adding features
			def features = []
			Feature.findAllByProduct(product).each { Feature feature ->
				features << feature.asMapForJSON(["id", "externalCode", "name", "position", "domain", "uuid"], null, locale?.language)
			}
			map["properties"] = features

			//adding pictures
			def tabPictures = [];
			getPictures(product).each{ Resource r ->
				tabPictures << IperUtil.getResourceVOSimple(r);
			}
			map.put("pictures", tabPictures)
			
			if (addToHistory == null || addToHistory == true)
			{
				uuidDataService.addProduct(productId);
			}
			return map
		}
	}
	
	/**
	 * Returns the list of map of visited products
	 * @param locale
	 * @param currencyCode
	 * @param companyId
	 * @return
	 * @throws CurrencyRateException
	 * @throws ProductNotFoundException
	 */
	List<Map> getVisited(Locale locale, String currencyCode, long companyId) throws CurrencyRateException, ProductNotFoundException {
		String [] idProducts = uuidDataService.getProducts();
		if (!idProducts) 
		{
			return []
		}
		else 
		{
			def today = IperUtil.today()
			
			List<Product> list = Product.createCriteria().list {
				company { eq('id', companyId) }
				eq('state', ProductState.ACTIVE)
				le('startDate',today)
				ge('stopDate',today)
				'in' ('id', idProducts.collect{ String item -> item.toLong() })
			}
	
			List<Map> result = [];
			list?.each { Product p ->
				result << renderProduct(locale, currencyCode, p);
			}
			return result;
		}
	}
	
	/**
	 * Return the list of salable dates for the given product and between the given dates.
	 * If startCalendar is before today, today is use instead
	 * @param productId
	 * @param startCalendar
	 * @param endCalendar
	 * @return
	 * @throws ProductNotFoundException
	 */
	List<String> getProductDatesBetween(long productId, Calendar startCalendar, Calendar endCalendar) throws ProductNotFoundException {
		def today = IperUtil.today()

		if (startCalendar.compareTo(today) < 0) {
			startCalendar = today
		}

		Product product = Product.get(productId)
		if (product == null) {
			throw new ProductNotFoundException("The product " + productId + "was not found")			
		}
		
		List<String> result = []
		if (product.calendarType != ProductCalendar.NO_DATE) {
			// On récupère les dates possibles à cheval sur la période donnée
			List<IntraDayPeriod> listIncluded = IntraDayPeriod.createCriteria().list{
				eq('product',product)
				le('startDate', endCalendar)
				ge('endDate', startCalendar)
			}
			// On récupère les dates à exclure à cheval sur la période donnée
			List<DatePeriod> listExcluded = DatePeriod.createCriteria().list{
				eq('product',product)
				le('startDate', endCalendar)
				ge('endDate', startCalendar)
			}

			Calendar currentDate = startCalendar.clone()
			while (currentDate.before(endCalendar)) {
				if (IperUtil.isDateIncluded(listIncluded, currentDate) && !IperUtil.isDateExcluded(listExcluded, currentDate)) {
					result << RenderUtil.asMapForJSON(currentDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR)
				}
				currentDate.add(Calendar.DAY_OF_YEAR,1)
			}
		}
		return result;
	}
	
	/**
	 * Return the list of salable times for the given product and the given date.
	 * @param productId
	 * @param date
	 * @return
	 * @throws ProductNotFoundException
	 */
	List<String> getProductTimesForDate(long productId, Calendar date) throws ProductNotFoundException {
		def today = IperUtil.today()
		
		Product product = Product.get(productId)
		if (product == null) {
			throw new ProductNotFoundException("The product " + productId + "was not found")
		}
		
		List<String> result = []
		if (date.compareTo(IperUtil.today()) >= 0 && product.calendarType == ProductCalendar.DATE_TIME) {
			List<IntraDayPeriod> listIncluded = IntraDayPeriod.createCriteria().list{
				eq('product', product)
				le('startDate', date)
				ge('endDate', date)
			}
			listIncluded.each {
				if (IperUtil.isDateIncluded([it], date)) 
				{
					result << RenderUtil.asMapForJSON(it.startDate, IperConstant.TIME_FORMAT)
				}
			}
		}
		return result;		
	}

	
	com.mogobiz.store.domain.Resource retrievePicture(Product entity) {
		def pictures = getPictures(entity)
		return (pictures && pictures.size() > 0)?pictures[0]:null
	}

	java.util.Collection getPictures(Product entity) {
		def pictures = []
		if(entity.id > 0){
			Map params = [:]
			params['product'] = entity
			params['xtype'] = ResourceType.PICTURE
			List<Product2Resource> bindedResources = Product2Resource.executeQuery("\
			select distinct pr from Product2Resource pr join pr.resource as r\
			where pr.product=:product "
					+ "and r.xtype=:xtype "
					+ " order by pr.position asc\
			", params)
			bindedResources?.each { bindedResource ->
				pictures << bindedResource.resource
			}
		}
		return pictures
	}
	public com.mogobiz.store.domain.Resource retrieveVideo(Product entity) {
		def params = [:]
		params['product'] = entity
		params['xtype'] = ResourceType.VIDEO
		def bindedResource = Product2Resource.executeQuery("\
			select distinct pr from Product2Resource pr join pr.resource as r\
			where pr.product=:product "
				+ "and r.xtype=:xtype "
				+ " order by pr.position asc\
			", params, [max:1, offset:0])
		return bindedResource?bindedResource.resource:null
	}

	/**
	 * Increment the stock of the ticketType of the ticket for the given date and decrement the number of sale
	 * @param ticketType
	 * @param quantity
	 * @param date
	 */
	void increment(TicketType ticketType, long quantity, Calendar date) {
		Product product = ticketType.product
		Stock stock = ticketType.stock
		if (stock != null)
		{
			// Search the corresponding StockCalendar
			StockCalendar stockCalendar = retrieveStockCalendar(product, ticketType, date, stock)
			
			// sale decrement			
			product.nbSales -= quantity
			ticketType.nbSales -= quantity
			stockCalendar.sold -= quantity
			stockCalendar.save(flush:true)

            upsertProduct(product)
		}
	}

    /**
	 * Decrement the stock of the ticketType of the ticket for the given date and increment the number of sale
	 * If stock is insufficient, Exception is thrown
	 * @param ticketType
	 * @param quantity
	 * @param date
	 * @throws InsufficientStockException
	 */
	void decrement(TicketType ticketType, long quantity, Calendar date) throws InsufficientStockException {
		Product product = ticketType.product
		Stock stock = ticketType.stock
		if (stock != null)
		{
			// Search the corresponding StockCalendar
			StockCalendar stockCalendar = retrieveStockCalendar(product, ticketType, date, stock)

			// stock vérification
			if (!stock.stockUnlimited&& !stock.stockOutSelling && stockCalendar.stock < (quantity + stockCalendar.sold))
			{
				throw new InsufficientStockException('The available stock is insufficient for the quantity required')
			}
			
			// sale increment
			product.nbSales += quantity
			product.save()
			
			ticketType.nbSales += quantity
			ticketType.save()

			stockCalendar.sold += quantity
			stockCalendar.save(flush:true)

            upsertProduct(product)
		}
	}
	
	/**
	 * retrieve the StockCalendar of the TicketType for the given date.
	 * If the StockCalendar does not exist, it is be created
	 * @param product
	 * @param ticketType
	 * @param date
	 * @return
	 */
	private StockCalendar retrieveStockCalendar(Product product, TicketType ticketType, Calendar date, Stock stock) {
		StockCalendar stockCalendar = StockCalendar.createCriteria().get {
			eq ('ticketType', ticketType)
			if (product.calendarType != ProductCalendar.NO_DATE) {eq ('startDate', date)}
		}
		
		if (!stockCalendar)
		{
			// creation of StockCalendar
			stockCalendar = new StockCalendar(sold: 0, stock: stock.stock, startDate: date, ticketType: ticketType, product: product)
			stockCalendar.save()
		}
		return stockCalendar
	}
	
	/**
	 * <p>
	 * Permet de cr�er ou modifier le produit. La m�thode renvoie la
	 * Map repr�sentant le produit si la modification c'est bien pass�
	 * ou null sinon (dans ce cas, le produit contient la liste des
	 * erreurs)
	 * </p>
	 */
	List<Product> list() {
		Product.findAll()
	}
	Product saveProduct(Product entity, Map params, Seller seller, com.mogobiz.store.domain.EventType eventType) {
		if(params['product.state.name']){
			if (params['product.state.name'] == 'ACTIVE') {
				params['product']?.startDate = IperUtil.parseDateFromParam('01/01/2011')
				params['product']?.stopDate = IperUtil.parseDateFromParam('31/12/2049')
			}
			else {
				params['product']?.startDate = null
				params['product']?.stopDate = null

			}
		}
		if(params['product']?.startFeatureDate){
			try {
				params['product']?.startFeatureDate = IperUtil.parseDateFromParam(params['product']?.startFeatureDate)
			}
			catch (Exception e) {
				println ("error while parsing startFeatureDate "+params['product']?.startFeatureDate)
			}
		}


		if(params['product']?.stopFeatureDate){
			try {
				params['product']?.stopFeatureDate = IperUtil.parseDateFromParam(params['product']?.stopFeatureDate)
			}catch (Exception e) {
				println ("error while parsing stopFeatureDate "+params['product']?.stopFeatureDate)
			}
		}

        // Shipping
        if (params["product"]?.shipping) {
            Shipping shipping = new Shipping()
            shipping.properties = params["product"].shipping
            shipping.save()
            entity.shipping = shipping
        }

        // Ibeacon
        if (params["product"]?.ibeaconId) {
            if (params["product"]?.ibeaconId == -1) {
                entity.ibeacon = null
            }
            else {
                entity.ibeacon = Ibeacon.get(params["product"]?.ibeaconId)
            }
        }

		def stock
		// r�cup�ration de l'ancien stock global avant d'ex�cuter l'instruction "entity.properties = params['product']"
		if (false) {
			if (entity.stock) {
				stock = new Stock( stock : entity.stock.stock,
				stockUnlimited:entity.stock.stockUnlimited,
				stockOutSelling:entity.stock.stockOutSelling)
			}
			else {
				entity.stock = new Stock() // unlimited by default
			}
		}
		entity.sanitizedName = null
		entity.properties = params['product']
		if (!entity.sanitizedName)
			entity.sanitizedName = 	sanitizeUrlService.sanitizeWithDashes(entity.name)
		entity.modificationDate = Calendar.getInstance();

		def stockUpdated = true
		if (false) {
			if (params.product.stock) {
				Long quantity = params['product']?.stock?.stock?new Long(params['product'].stock.stock):null
				//calcul de la varation du stock
				def stockVariation = quantity > 0?quantity - (stock?.stock?stock?.stock:0) : null
				//v�rifier si la mise � jour du stock actuel est permise

				if (stockVariation!=0) { //la quantit� est chang�e
					stock = new Stock( stock : quantity,
					stockUnlimited:quantity?false:true,
					stockOutSelling:params['product']?.stock?.stockOutSelling)
				}
				// dans tous les cas (modification permise ou pas) on met � jour stockOutSelling
				def stockOutSelling = params.product.stock.stockOutSelling
				stock.stockOutSelling = new Boolean(params['product']?.stock?.stockOutSelling)
				//mise � jour du stock
				entity.stock = stock
			}
		}
		if (params.containsKey('product.description')) {
			entity.descriptionAsText = new Html2Text(entity.description).getText()
		}
		if (params['product.state.name']) {
			entity.state = ProductState.valueOf(params['product.state.name'])
		}
		entity.company = seller.company
		// TaxRate
		if (params["taxRateId"]) {
            entity.taxRate = taxRateService.findTaxRateById(Long.valueOf(params["taxRateId"] as String))
		}
		// price
		def price = params['product']?.price
		if(price){
			entity.price = Long.parseLong(price)
		}
		if (!entity.price) {
			entity.price = 0;
		}
		if (!entity.code && !params['product']?.code) {
			entity.code = UUID.randomUUID();
		}
		if (!entity.uuid && !params['product']?.uuid) {
			entity.uuid = UUID.randomUUID();
		}
		if(entity.validate()){
			// gestion des ressources
			def resources = params['product']?.resourcesList
			if(resources) {
				// TODO	pour ordonner les ressources
				//				def resourcesList = JSON.parse(resources)
				//				def product2Resources = product?.product2Resources?.toArray()
				//
				//				for(int i=0; i<product2Resources?.size(); i++) {
				//					product.removeFromProduct2Resources(product2Resources[i])
				//					product2Resources[i].delete()
				//				}
				//				resourcesList?.each { resource->
				//					def res =  Resource.get(resource.id)
				//					product.addToProduct2Resources(resource:res,product:product,montant:resource.montant,position:resource.position)
				//				}
			}

			//gestion de la categorie
			def categoryId = params['product.category.id']
			if (categoryId) {
                entity.category = Category.get(Long.parseLong(categoryId));
            }
			// gestion du brand
			def brandId = params['product.brand.id']
			if (brandId) {
				entity.brand = Brand.get(Long.parseLong(brandId));
			}

			if(!entity.hasErrors()){
				entity.save(flush: true)
			}

			def event = IperUtil.saveEvent(seller, entity, eventType)
			if(!event.hasErrors() && EventType.CREATE.equals(eventType)) {
				entity.creation = event
				entity.save(flush: true)
			}
		}

		if(!entity.hasErrors()){
			def productVO = entity.asMapForJSON()
			if (!stockUpdated) {
				productVO.put("stockError",IperConstant.ERREUR_INSUFFISENT_STOCK)
			}
			return entity
		}
		else {
			return null;
		}
	}

    private void upsertProduct(Product product) {
        Company company = product.company
        String store = company?.code
        String url = servletContext.getAttribute(store)
        if (!url) {
            Collection envs = EsEnv.executeQuery(
                    'from EsEnv env where env.active=true and env.company.code=:code', [code: store])
            if (envs && envs.size() > 0) {
                url = envs.get(0).url
                servletContext.setAttribute(store, url)
            }
        }
        if (url) {
            ExecutionContext ec = ESRivers.dispatcher()
            Collection<Catalog> catalogs = Catalog.findAllByActivationDateLessThanEqualsAndCompany(
                    new Date(),
                    Company.get(company.id),
                    [sort:'activationDate', order:'desc'])
            Catalog catalog = catalogs.size() > 0 ? catalogs.get(0) : null
            def languages = Translation.executeQuery(
                    'SELECT DISTINCT t.lang FROM Translation t WHERE t.companyId=:idCompany',
                    [idCompany: company?.id]) as String[]
            RiverConfig config = new RiverConfig(
                    clientConfig: new ClientConfig(
                            url: url,
                            debug: true,
                            config: [index:store]
                    ),
                    idCatalog: catalog.id,
                    languages: languages,
                    defaultLang: 'fr')
            Future<BulkResponse> future = BlockingObservable.from(
                    (river as AbstractRiver<Product, ? extends Client>)?.upsertCatalogObjects(config, [product], ec)).last()
            BulkResponse response = Await.result(future, Duration.create(10, TimeUnit.SECONDS))
            if(response){
                log.info('product ' + product.id + ' updated to es')
            }
        }
    }

}