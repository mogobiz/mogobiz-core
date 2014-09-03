package com.mogobiz.store.customer.cmd
/**
 * Command Object pour la recherche de marques
 */
@grails.validation.Validateable
class ShowTranslationCommand {

	String type;
	Long target;
	String id;

}