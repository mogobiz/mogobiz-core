package com.mogobiz.service;

import static com.mogobiz.constant.IperConstant.*
import grails.events.Listener
import grails.util.Holders

import org.grails.plugin.platform.events.EventMessage

import com.mogobiz.store.domain.UuidData

class QueueUuidService {
	@Listener(topic=QUEUE_UUID, namespace=QUEUE_NS)
	def onEvent(EventMessage ev) {
		Map<String,String> map = ev.data
		String uuid = map['uuid']
		String payload = map['payload']
		String xtype = map['xtype']
		String action = map['action']
		switch (xtype) {
			case QUEUE_XTYPE_CART:
				handleCart(uuid, payload, xtype, action)
				break
			case QUEUE_XTYPE_PRODUCT:			
				handleProduct(uuid, payload, xtype, action)
				break
			case QUEUE_XTYPE_COUNTRY:
				handleCountry(uuid, payload, xtype, action)
				break
			default:
				break
		}
	}
	
	private void delEntry(String uuid, String xtype) {
		UuidData.executeUpdate("delete UuidData u where u.uuid = :uuid and u.xtype = :xtype", [uuid:uuid, xtype:xtype])
	}
	
	private void createAndSave(String uuid, String payload, String xtype, int lifetime) {
		Calendar expireDate = Calendar.getInstance();
		expireDate.add(Calendar.SECOND, lifetime);
		UuidData data = new UuidData(uuid:uuid, payload:payload, xtype:xtype, expireDate: expireDate);
		data.validate()
		data.save(flush: true)
	}
	
	private void handleCart(String uuid, String payload, String xtype, String action) {
		delEntry(uuid, xtype)
		if (action == QUEUE_ACTION_SET) {
			createAndSave(uuid, payload, xtype, Holders.config.uuidData.lifetime.cart);
		}
	}
	
	private void handleProduct(String uuid, String payload, String xtype, String action) {
		UuidData data = UuidData.findByUuidAndXtype(uuid, xtype)
		List<String> productsList =  data ? data.payload.tokenize(',') : []
		delEntry(uuid, xtype)
		if (action == QUEUE_ACTION_ADD)
			// Retrait du produit (car il sera ajouté au début de la liste)
			productsList.remove(payload);
			// Ajout du nouveau produit à la liste
			productsList.add(0, payload)
			// Récupération des products de la list dans la limite paramétrée
			String newPayload = productsList.take(Holders.config.user.product.visit.history).join(',')
			
			createAndSave(uuid, newPayload, xtype, Holders.config.uuidData.lifetime.product);
	}	
	
	private void handleCountry(String uuid, String payload, String xtype, String action) {
		UuidData data = UuidData.findByUuidAndXtype(uuid, xtype)
		data = new UuidData(uuid:uuid, payload: payload, xtype:xtype)
		delEntry(uuid, xtype)
		if (action == QUEUE_ACTION_SET)
			createAndSave(uuid, payload, xtype, Holders.config.uuidData.lifetime.country);
	}	
	
	
}
