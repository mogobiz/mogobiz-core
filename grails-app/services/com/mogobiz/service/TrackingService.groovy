package com.mogobiz.service

import org.codehaus.groovy.grails.web.util.WebUtils;

import grails.util.Holders;

import com.dalew.CookieService;

/**
 * Service en charge de manipuler les cookies iper notamment le 
 * cookie de tracking.
 * 
 */
class TrackingService {

	static transactional = true
	
	/**
	 * Service d'accès au cookie
	 */
	CookieService cookieService;
	
	/**
	 * Crée le cookie Tracking.
	 * Cette fonction ne doit être utilisé que par le filtre
	 * en charge de créer le cookie s'il n'existe pas
	 */
	void createTrackingUuid() {
		String trackingName = Holders.config.cookie.tracking.name;
		String uuid = UUID.randomUUID().toString();
		cookieService.setCookie(trackingName, uuid, Holders.config.cookie.tracking.lifetime)
		WebUtils.retrieveGrailsWebRequest().currentRequest.setAttribute(trackingName, uuid);
	}
	
	/**
	 * Renvoie le UUID de tracking correspondant à la requête en cours de traitement
	 * @return
	 */
    String getTrackingUuid() {
		String trackingName = Holders.config.cookie.tracking.name;
		String uuid = cookieService.getCookie(Holders.config.cookie.tracking.name);
		if (uuid == null) {
			// cas où le tracnking a été créé par le filtre pour le requête en cours.
			// Dans ce cas,le uuid est en attribut de la requête
			uuid = WebUtils.retrieveGrailsWebRequest().currentRequest.getAttribute(trackingName);
		}
		return uuid;
    }
}
