package com.mogobiz.store.customer.cmd

/**
 * Command Object pour la recherche de catalogues
 */
@grails.validation.Validateable
class ShowCatalogCommand {

	Long id;
	
	static constraints = {
		id(nullable: true)
	}
}
