package com.mogobiz.ajax

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class AjaxResponseTest {

	@Before
	public void setUp() {
	}

	@Test
	public void testPrepareResponse(){
		AjaxResponse response = new AjaxResponse()
		response.success = true
		response.data = 'data'
		def map = response.asMap()
		assertTrue(map.success)
		assertEquals('data', map.data)
		response.success = false
		map = response.asMap()
		assertFalse(map.success)
	}
}