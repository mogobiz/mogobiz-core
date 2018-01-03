/**
 * 
 */
package com.mogobiz.ajax


class AjaxResponse {
	boolean success = true
	def data = null
	Map errors = [:]
	
	/**
	 * Transform this to a map for json
	 * @return
	 */
	Map asMap() {
		def map = [:]
		map['success'] = success
		map['data'] = data
		map['errors'] = errors
		return map
	}
}
