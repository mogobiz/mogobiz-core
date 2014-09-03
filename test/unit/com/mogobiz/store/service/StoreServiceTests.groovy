package com.mogobiz.store.service

import com.mogobiz.AbstractTestService
import com.mogobiz.service.CatalogService
import com.mogobiz.service.StoreService;
import com.mogobiz.store.customer.StoreSessionData;
import com.mogobiz.store.domain.Catalog;
import com.mogobiz.store.domain.Company;

import grails.test.mixin.Mock;

@Mock([Company, Catalog])
class StoreServiceTests extends AbstractTestService {

	StoreService service;
	
	void setUp() {
		super.setUp()
		
		service = new StoreService();
		service.catalogService = new CatalogService()
	}
	
    void testLoadStoreSessionData() {
        StoreSessionData sessionData = service.loadStoreSessionData(null, "EUR", company.code);
		assertNotNull(sessionData); 
		assertEquals(company.id, sessionData.companyId)
		assertEquals(company.code, sessionData.companyCode)
		assertEquals(catalog.id, sessionData.companyId)
		assertEquals("US", sessionData.country)
		assertEquals("EUR", sessionData.currency)
    }
}
