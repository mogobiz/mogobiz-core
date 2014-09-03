package com.mogobiz.constant

import grails.util.Holders

interface IperConstant {
	
	/**
	 * Nom de la queue en charge de stocker les données
	 * associé au tracking
	 */
	static final String QUEUE_UUID = "queue.uuid";
	static final String QUEUE_SOCIAL = "queue.social";
	static final String QUEUE_NS = "mogobiz-admin";

	/**
	 * Xtype des objets correspond à un Cart envoyé dans la queue
	 */
	static final String QUEUE_XTYPE_CART = "Cart";
	/**
	 * Xtype des objets correspond à un Product envoyé dans la queue
	 */
	static final String QUEUE_XTYPE_PRODUCT = "Product";
	/**
	 * Xtype des objets correspond à un Country envoyé dans la queue
	 */
	static final String QUEUE_XTYPE_COUNTRY = "Country";
	
	/**
	 * Add action sending into UUID queue
	 */
	static final String QUEUE_ACTION_ADD = "add";

	/**
	 * Update action sending into UUID queue
	 */
	static final String QUEUE_ACTION_SET = "set";
	
	/**
	 * Delete action sending into UUID queue
	 */
	static final String QUEUE_ACTION_DELETE = "delete";

	
	static final String TIME_FORMAT = "HH:mm"
	
	static final String DATE_FORMAT = "dd/MM/yyyy HH:mm"
	static final String DATE_FORMAT_dMy_Hms = "dd-MM-yyyy_HH-mm-ss"
	
	static final String DATE_FORMAT_WITHOUT_HOUR = "dd/MM/yyyy"
	static final String DATE_FORMAT_FULL_STRING = "MMMMM dd, yyyy HH:mm:ss"
		
	//Store Services Errors
	static final String ERREUR_INSUFFISENT_STOCK = 'error.insufficient.stock'
	static final String ERREUR_ILLEGAL_ARGUMENT = 'error.illegal.argument'
	static final String ERREUR_CART_NOT_ORDERED = 'error.cart.not.ordered'
	static final String ERREUR_DATE_ORDER = 'error.date.order'
	
	static final Integer ALL_IN_ONE_PAGE = Integer.MAX_VALUE
	static final Integer NUMBER_PRODUCT_PER_PAGE = 50
	static final Integer NUMBER_EVENTS_PER_PAGE = 50
	static final Integer NUMBER_SALES_PER_PAGE = 50
	
	static String GET_QRCODE_URL = Holders.config.grails.serverURL.toString() + '/event/getQRCode?content='
}
