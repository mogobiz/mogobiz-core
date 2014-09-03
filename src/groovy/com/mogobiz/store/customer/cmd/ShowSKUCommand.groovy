package com.mogobiz.store.customer.cmd

/**
 * Command Object pour la recherche de SKUs
 */
@grails.validation.Validateable
class ShowSKUCommand {

	Long id;
	Long productId;
	
	static constraints = {
		id(nullable: true, validator: {Long val, ShowSKUCommand cmd ->
			return (val != null || cmd.productId != null)
		})
		productId(nullable: true, validator: {Long val, ShowSKUCommand cmd ->
			return (val != null || cmd.id != null)
		})
	}
}
