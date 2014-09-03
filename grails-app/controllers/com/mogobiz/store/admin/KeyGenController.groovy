/**
 * 
 */
package com.mogobiz.store.admin

import grails.converters.JSON
import grails.converters.XML
import java.security.MessageDigest

import com.mogobiz.store.domain.Company
import java.util.UUID;

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class KeyGenController {

	def generateAPIKey = {
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
