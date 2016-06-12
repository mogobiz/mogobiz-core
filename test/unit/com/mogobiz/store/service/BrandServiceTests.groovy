package com.mogobiz.store.service

import com.mogobiz.AbstractTestService
import com.mogobiz.service.BrandService
import com.mogobiz.store.domain.*
import grails.test.mixin.Mock

@Mock([Company, Catalog, Category, Brand, LocalTaxRate, TaxRate, Translation, Product])
class BrandServiceTests extends AbstractTestService {
	
	BrandService service;

	Category adult
	Brand nike
	Brand addidas
	Brand hideBrand
	
    void setUp() {
		super.setUp()
		
		adult = new Category(name: "Adult", catalog: catalog, company: company, uuid: "uuid", position: 10);
		saveEntity(adult)

		nike = new Brand(name: "Nike", website: "www.nike.com", company: company);
		saveEntity(nike)
		saveEntity(new Translation(target: nike.id, lang: "fr", value: '{"website": "www.nike.fr"}'));
		
		addidas = new Brand(name: "Addidas", website: "www.addidas.com", company: company);
		saveEntity(addidas)
		
		hideBrand = new Brand(name: "hideBrand", website: "www.hideBrand.com", company: company, hide: true);
		saveEntity(hideBrand)

		TaxRate taxRate = createTaxRate();
		createProduct("p1", "produitNike", company, nike, adult, taxRate);
		createProduct("p12", "produitNike2", company, nike, adult, taxRate);
		createProduct("p2", "produitInactif", company, addidas, adult, taxRate, ProductState.INACTIVE);
		createProduct("p3", "produitActif", company, hideBrand, adult, taxRate);	
		
		service = new BrandService();
    }

    void tearDown() {
        // Tear down logic here
    }

    void testListVisibleByCompany() {
		List<Map> list = service.listVisibleByCompany(null, -1);
		assertNotNull(list);
		assertEquals(0, list.size());
		
		list = service.listVisibleByCompany(null, company.id);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(nike.id, list[0].id);
		assertEquals(addidas.id, list[1].id);

		list = service.listVisibleByCompany(Locale.FRANCE, company.id);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(nike.id, list[0].id);
		assertEquals("www.nike.fr", list[0].website);
		assertEquals(addidas.id, list[1].id);
    }
	
	void testListVisibleByCategory() {
		List<Map> list = service.listActiveByCategory(null, -1, -1);
		assertNotNull(list);
		assertEquals(0, list.size());
		
		list = service.listActiveByCategory(null, company.id, -1);
		assertNotNull(list);
		assertEquals(0, list.size());
		
		list = service.listActiveByCategory(null, -1, adult.id);
		assertNotNull(list);
		assertEquals(0, list.size());

		list = service.listActiveByCategory(null, company.id, adult.id);
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals(nike.id, list[0].id);
	}
}
