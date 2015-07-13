/**
 *
 */
package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.tools.ImageTools
import com.mogobiz.tools.MimeTypeTools
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

import java.text.SimpleDateFormat

import com.mogobiz.store.domain.EventType
import com.mogobiz.store.domain.Product2Resource
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.constant.IperConstant
import com.mogobiz.tools.ImageSize
import com.mogobiz.utils.IperUtil

/**
 * Controller utilis� pour g�rer les resources
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class ResourceController {

	AjaxResponseService ajaxResponseService

	def authenticationService

	SimpleDateFormat sdf = new SimpleDateFormat(IperConstant.DATE_FORMAT)

	/**
	 *
	 */
	static final int BUFFER_SIZE = 2048

	@Transactional(readOnly = true)
	def display() {
		def id = params.id
		def size = params.size
		if(id != null){
			Resource resource = Resource.get(id)
			if(!resource){
				response.sendError 404
				return
			}
			if(resource.uploaded) {
                String resourcesPath = grailsApplication.config.resources.path
				File file
				if (size) {
					file = ImageTools.getFile(new File(resourcesPath + (resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator) - resourcesPath)), ImageSize.valueOf(size), true);
				}
				else {
					file = new File(resourcesPath + (resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator) - resourcesPath))
				}
				if(!file.exists()){
					log.warn("${file.absolutePath} not found")
					response.sendError(404)
				}
				else{
					response.contentType = resource.contentType ?: MimeTypeTools.detectMimeType(file)
					// response.outputStream << file.path
					def out = response.outputStream
					def bytes = new byte[BUFFER_SIZE]
					file.withInputStream { inp ->
						while( inp.read(bytes) != -1) {
							out.write(bytes)
							out.flush()
						}
					}
				}
			}
			else{
				switch (resource.xtype) {
					case ResourceType.TEXT:
						def content = resource.content
						if(content){
							def out = response.outputStream
							out.write(resource.content)
							out.flush()
						}
						break;
					default:
						redirect (url:resource.url)
						break;
				}
			}
		}
	}

	@Transactional(readOnly = true)
	def show() {

		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def id = params.id
		if(id != null){
			Resource resource = Resource.get(id)
			if(resource && resource.company==company){
				def resourceVO = resource.asMapForJSON()
				withFormat {
					html resourceVO:resourceVO
					xml {  render resourceVO as XML }
					json { render resourceVO as JSON }
				}
			}
			else{
				response.sendError 404
			}
		}
		else{
			// filter les ressources suivants les params
			def resourceslist
			def c = Resource.createCriteria()
			def productId = params['product']?.id

			if (productId!=null){

				def critProduct2Resource = Product2Resource.createCriteria()
				def product2ResourceList = critProduct2Resource.list {
					eq('product.id',  productId.toLong())
					order("position", "asc")
				}

				def attachedResourcesIdlist=product2ResourceList['resource']['id']

				resourceslist = c.list {
					eq('company', company)
					eq('active',true)
					eq('deleted',false)
					if(attachedResourcesIdlist) {
						not {'in' ('id',attachedResourcesIdlist)}
					}
					order("name", "asc")
				}
			}
			else{
				resourceslist = c.list {
					eq('company', company)
					if( params['resource']?.active) {
						eq('active', params['resource']?.active.toBoolean())
					}
					if(params['resource']?.deleted) {
						eq('deleted', params['resource']?.deleted.toBoolean())
					}
					if(params['resource']?.xtype) {
						eq('xtype', ResourceType.valueOf(params['resource']?.xtype) )
					}
					order("name", "asc")
				}
			}
			// construct the resources VO list to send to client
			def resList = new ArrayList()
			resourceslist.each { res ->
				resList.add( res.asMapForJSON())
			}

			withFormat {
				html resList:resList
				xml {  render resList as XML }
				json { render resList as JSON }
			}
		}
	}

	/**
	 * mise a jour d'une ressource
	 */
	@Transactional
	def update() {
		def resourceMap = new HashMap()
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def id = params['resource']?.id
		def resource = params['resource']?.id?Resource.get (params['resource']?.id):null
		if(resource && resource.company==company){

			saveResource(params, resource, EventType.MODIFY)
		}
		else{
			response.sendError 404
		}
	}


	/**
	 * creation d'une nouvelle ressource
	 */
	@Transactional
	def save() {

		def resourceMap = new HashMap()
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()

		if(seller == null){
			response.sendError 401
			return
		}

		def resource = new Resource()
		resource.active = true
		resource.deleted = false
		resource.company = seller.company
		saveResource(params, resource, EventType.CREATE)
	}

	@Transactional
	def delete() {
		def resourceMap = new HashMap()
		def seller = request.seller?request.seller:authenticationService.retrieveAuthenticatedSeller()
		if(seller == null){
			response.sendError 401
			return
		}
		def company = seller.company
		def resource = params['resource']?.id?Resource.get (params['resource']?.id):null
		if(resource && resource.company==company){
			if(resource.deleted){
				resource.active = false
			}
			resource.deleted=!resource.deleted

			resourceMap = resource.asMapForJSON()
			resource.save()

			withFormat {
				xml{
					if(!resource.hasErrors()){
						render resource as XML
					}
					else{
						render resource.errors as XML
					}
				}
				json{
					render ajaxResponseService.prepareResponse(resource,resourceMap).asMap() as JSON
				}
			}
		}
		else{
			response.sendError 404
		}
	}

	/**
	 * create or update resource
	 * @param params the request params
	 * @param resource the resource to save
	 * @param eventType the event type (creation or modification)
	 */
	private saveResource(params, resource, eventType) {

		def poi
		def resourceVO = new HashMap()

		resource.properties = params['resource']
		if(resource.validate()){
			if(params['poi']){
				poi= IperUtil.saveOrUpdatePoi(params)
				if(!poi.hasErrors()) {
					resource.poi=poi
					resource.save()
				}else{
					resource.errors = poi.errors
				}
			}else{
				resource.save()
			}

			resourceVO =  resource.asMapForJSON()
		}
		withFormat {
			html {
				redirect(action:'show', params:[format:'html'])
			}
			xml{
				if(!resource.hasErrors()){
					render resource as XML
				}
				else{
					render resource.errors as XML
				}
			}
			json{
				render ajaxResponseService.prepareResponse(resource,resourceVO).asMap() as JSON
			}
		}
	}
}
