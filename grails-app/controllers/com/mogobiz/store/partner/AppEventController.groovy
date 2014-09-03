package com.mogobiz.store.partner

import com.mogobiz.store.domain.Event
import com.mogobiz.store.domain.Product
import java.text.SimpleDateFormat

import com.mogobiz.constant.IperConstant
import grails.converters.JSON
import grails.converters.XML 
/**
 * Controller utilis� pour recup�rer la liste des evenements declench�s (creation, modification, ou suppression d'un objet twitable  )
 */
class AppEventController {
	
	def ajaxResponseService
	def authenticationService

	SimpleDateFormat sdf = new SimpleDateFormat(IperConstant.DATE_FORMAT)
	
	def index = {}
	
	def show = {
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		
		
		def id = params.id
		if(id != null){
			//TODO en cas de besoin
		}
		else{
			def productId = params['product']?.id
			if(productId) {
				def product = Product.get(productId)
				if(product && product.company==company){
					
					def events = Event.createCriteria() 
					def eventslist = events.list {
						eq('product.id', productId.toLong())
						order("date", "asc")
					}
					
					// construct the events VO list to send to client
					def evtList = new ArrayList()
					eventslist.each { evt ->
						evtList.add(evt.asMapForJSON())
					}
					withFormat {
						html evtList:evtList
						xml {  render evtList as XML }
						json { render evtList as JSON }
					}
				}else{
					response.sendError 404
				}
			}else{
				def crit = Event.createCriteria()
				// recuperer la liste des evenements ordonn�es par date
				def eventslist = crit.list { order("date", "asc") }
				
				// construct the events VO list to send to client
				def evtList = new ArrayList()
				eventslist.each { evt ->
					evtList.add(evt.asMapForJSON())
				}
				withFormat {
					html evtList:evtList
					xml {  render evtList as XML }
					json { render evtList as JSON }
				}
			}
		}
	}
}
