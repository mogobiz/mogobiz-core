package com.mogobiz.store.customer.cmd

/**
 * Command Object pour la recherche de produits
 */
@grails.validation.Validateable
class ShowIntraDayPeriodCommand {

	Long id;
	Long productId;

	
	static constraints = {
		id(nullable: true, validator: {Long val, ShowIntraDayPeriodCommand cmd ->
			return (val != null || cmd.productId != null)
		})
		productId(nullable: true, validator: {Long val, ShowIntraDayPeriodCommand cmd ->
			return (val != null || cmd.id != null)
		})
	}
}
