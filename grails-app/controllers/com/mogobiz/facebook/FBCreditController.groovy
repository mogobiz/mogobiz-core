/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.facebook

import grails.converters.JSON;

import com.mogobiz.store.domain.Product
import grails.transaction.Transactional

class FBCreditController
{

	@Transactional(readOnly = true)
	def callback() {
		//TODO faire le controlle de la signature
		
		String method = params["method"];
		if (method == "payments_get_items")
		{
			// preparation de la fenetre de confirmation
			String idProduit = params["order_info"];
			
			Product p = Product.get(idProduit);
			def listeItem = [];
			if (p != null)
			{
				def item = [
					item_id : p.id,
					title : p.name,
					description : p.description,
				//item.image_url = "http://www.facebook.com/images/gifts/21.png";
				//item.product_url = "http://www.facebook.com/images/gifts/21.png";
					price : 1
				]
				listeItem << item;
			}
			def output = [
				content: listeItem,
				method: "payments_get_items"
			]
			render output as JSON
		}
		else if (method == "payments_status_update")
		{
			// changement de status
			String status = params["status"];
			def obj = params["order_details"];
			println status;
			def output = [
				content: [status : "settled"],
				method: "payments_status_update"
			]
			render output as JSON
		}
	}
}
