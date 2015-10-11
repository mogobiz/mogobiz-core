/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

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
	SanitizeUrlService sanitizeUrlService
	TaxRateService taxRateService

    ESRiver river = ESRivers.instance.loadRiver("product")

    def servletContext = SCH.servletContext


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
			Map map = product.asMapForJSON(null, null, locale?.language)

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
			return map
		}
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
			if (!shipping.hasErrors()) {
				shipping.save(flush:true)
				entity.shipping = shipping
			}
			else {
				shipping.errors.each {
					println(it)
				}
			}
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
		}

		if(!entity.hasErrors()){
			def productVO = entity.asMapForJSON()
			if (!stockUpdated) {
				productVO.put("stockError",IperConstant.ERREUR_INSUFFISENT_STOCK)
			}
			return entity
		}
		else {
			entity.errors.allErrors.each { println it }
			return null;
		}
	}
}