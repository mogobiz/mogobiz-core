package com.mogobiz.store.customer.cmd
/**
 * Command Objet pour la fonctionnalité de modification d'un élément du panier
 */
@grails.validation.Validateable
public class UpdateCartItemCommand {

	String cartItemId;
	int quantity;
	
	static constraints = {
		cartItemId(nullable: false)
		
		quantity(nullable: false, min: 1)

	}
}
