/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.geolocation.partner

import com.mogobiz.geolocation.domain.PoiType
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

import java.util.HashMap

/**
 * Controller utilis� pour g�rer les types des pois
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class PoiTypeController {

	@Transactional(readOnly = true)
	def show(Long id) {
		if(id != null){
			def poiType = PoiType.get(id)
			if(poiType ){
				withFormat {
					json {
						render poiType as JSON
					}
				}
			}
			else{
				response.sendError 404
			}
		}
		else{
			def crit = PoiType.createCriteria()
			def poiTypesList = crit.list {
				order("xtype", "asc")
			}

			withFormat {
				json {
					render poiTypesList as JSON
				}
			}
		}
	}
}
