package com.mogobiz.store.service

import com.mogobiz.AbstractTestService;
import com.mogobiz.store.domain.Catalog;
import com.mogobiz.store.domain.Company
import com.mogobiz.service.CatalogService;
import grails.test.mixin.Mock;


@Mock([Company, Catalog])
class CatalogServiceTests extends AbstractTestService {

	CatalogService catalogService;
	
	Catalog rootCatalog
	
	void setUp() {
		super.setUp()

		rootCatalog = new Catalog(name:"Root Catalog", uuid:"rootUuid", social:false, activationDate:new Date(), company:company)
		saveEntity(rootCatalog)
		
		catalogService = new CatalogService();
	}
	
    void testGetDefaultCatalogId() {
		Long r = catalogService.getDefaultCatalogId(-1);
		assertNull(r);
		
		r = catalogService.getDefaultCatalogId(company.id);
		assertNotNull(r)
		assertEquals(rootCatalog.id, r)
    }
}
