package com.mogobiz.service

import grails.converters.JSON
import grails.test.mixin.TestFor
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(FeatureService)
class FeatureServiceTests {

	void testSomething() {
		String[] iso = java.util.Locale.getISOLanguages()
		iso.each {println(it) }
	}
	void testRest() {
		def http = new HTTPBuilder('http://mogopay.ebiznext.com/mogopay/')
		def data = http.get( path : 'rate/list', query : [:])
		List<JSONObject> res = JSON.parse(data.toString())
		Map<String,Double> map = res.collectEntries {
			[(it.get('currencyCode')) : it.get('currencyRate')]
		}
		def x = map.get('USD')
		double y = map.get('USD')
		println(x)
		println(y)
	}
}
