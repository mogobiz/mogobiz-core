/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 * 
 */
package com.mogobiz.store.admin

import com.mogobiz.store.domain.Company
import grails.transaction.Transactional

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class KeyGenController {

	@Transactional
	def generateAPIKey() {
		def company = params['company']?.id?Company.get(params['company']?.id):null
		if(company) {
			company.apiKey = company.id+"-"+UUID.randomUUID();
			if(company.validate()){
				company.save()
			}
			render company.apiKey as String
		}
		else
		{
			response.sendError 404
		}
	}

}
