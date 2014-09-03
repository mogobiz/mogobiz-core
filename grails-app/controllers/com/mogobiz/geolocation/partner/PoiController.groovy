/**
 * 
 */
package com.mogobiz.geolocation.partner

import grails.converters.JSON
import grails.converters.XML

import org.codehaus.groovy.grails.web.context.ServletContextHolder

import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Resource
import com.mogobiz.ajax.AjaxResponseService;
import com.mogobiz.authentication.AuthenticationService;
import com.mogobiz.geolocation.domain.Poi
import com.mogobiz.geolocation.domain.VisibilityType
import com.mogobiz.utils.IperUtil

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class PoiController {

	AjaxResponseService ajaxResponseService
	AuthenticationService authenticationService

	def listPOITypes = {
		def dirList = []
		
		File poiFile = new File(ServletContextHolder.servletContext.getRealPath("/images/markers"))
		poiFile.eachFile {
			if (it.isDirectory() && it.getName() != ".svn") {
				dirList.add(it.getName())
			}
		}
		withFormat {
			json { render dirList as JSON }
		}
	}
	def listPOIPictures = {
		def pngList = []
		File poiFile = new File(ServletContextHolder.servletContext.getRealPath("/images/markers"), params.pictureType)
		poiFile.eachFile {
			if (it.isFile()) {
				pngList.add(it.getName())
			}
		}
		withFormat {
			json { render pngList as JSON }
		}
	}

	def save = {
		def poi = IperUtil.saveOrUpdatePoi (params)
		def map = poi.asMapForJSON()
		withFormat {
			html poi:poi
			xml {
				if(!poi.hasErrors()){
					render map as XML
				}
				else{
					render poi.errors as XML
				}
			}
			json {
				render ajaxResponseService.prepareResponse(poi, map).asMap() as JSON
			}
		}
	}

	def update = {
		def poi = IperUtil.saveOrUpdatePoi (params)
		def map = poi.asMapForJSON()
		withFormat {
			html poi:poi
			xml {
				if(!poi.hasErrors()){
					render map as XML
				}
				else{
					render poi.errors as XML
				}
			}
			json {
				render ajaxResponseService.prepareResponse(poi, map).asMap() as JSON
			}
		}
	}

	def delete = {
		def id = params.id
		if(id){
			def poi = Poi.get(id)
			if(poi){
				def resources = Resource.findAllByPoi(poi)
				resources?.each { resource ->
					resource.poi = null
					resource.save()
				}
				def products = Product.findAllByPoi(poi)
				products?.each { product ->
					product.poi = null
					product.save()
				}
				poi.delete(flush:true)
			}
		}
		withFormat {
			json { render [:] as JSON }
		}
	}

	def show = {
		def id = params.id
		def productId = params["product.id"]
		if(id != null){
			def poi = Poi.get(id)
			def poiVO = poi.asMapForJSON()
			if(poi ){
				def poiVOs = []
				poiVOs << poiVO
				withFormat {
					json { render poiVOs as JSON }
				}
			}
			else{
				def poiVOs = []
				withFormat {
					json { render poiVOs as JSON }
				}
			}
		}
		else if (productId) {
			Product product = Product.get(productId);
			if (product && product.poi ){
				def poiVO = product.poi.asMapForJSON()
				def poiVOs = []
				poiVOs << poiVO
				withFormat {
					json { render poiVOs as JSON }
				}
			}
			else{
				def poiVOs = []
				withFormat {
					xml {  render poiVOs as XML }
					json { render poiVOs as JSON }
				}
			}
		}
		else{
			def pois = Poi.createCriteria()
			def poislist = pois.list {
				if(params['poi']?.visibility) {
					eq('visibility', VisibilityType.valueOf(params['poi']?.visibility) )
				}
				if(params['poi']?.poiType) {
					eq('poiType.code', params['poi']?.poiType)
				}
				order("name", "asc")
			}

			// construct the pois VO list to send to client
			def poiVOs = []
			poislist.each { poi ->
				poiVOs << poi.asMapForJSON()
			}
			withFormat {
				json { render poiVOs as JSON }
			}
		}
	}

}
