package com.mogobiz.store.customer.cmd
/**
 * Command Objet pour la fonctionnalité de suppression d'un élément du panier
 */
@grails.validation.Validateable
public class RemoveCartItemCommand {

	String cartItemId;
	
	static constraints = {
		cartItemId(nullable: false)
	}
}
