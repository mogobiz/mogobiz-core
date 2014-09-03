package com.mogobiz.store.service

import com.mogobiz.AbstractTestService
import com.mogobiz.store.domain.Brand
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.LocalTaxRate;
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Product2Resource;
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.TaxRate;
import com.mogobiz.store.domain.Translation
import com.mogobiz.utils.Page
import com.mogobiz.service.ProductService;
import grails.test.mixin.Mock;

@Mock([Company, Catalog, Category, Brand, LocalTaxRate, TaxRate, Translation, Product, Product2Resource])
class ProductServiceTests extends AbstractTestService {
	
	ProductService service;

	Category adult
	Brand nike
	Brand addidas
	Brand hideBrand
	Product p1
	Product p2
	Product p3
	Product p4
	
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
		p1 = createProduct("p1", "produitNike", company, nike, adult, taxRate, ProductState.ACTIVE, null, 1000);
		p2 = createProduct("p2", "produitNike2", company, nike, adult, taxRate, ProductState.ACTIVE, null, 2000);
		p3 = createProduct("p3", "produitInactif", company, addidas, adult, taxRate, ProductState.INACTIVE, null, 2500);
		p4 = createProduct("p4", "produitActif", company, hideBrand, adult, taxRate, ProductState.ACTIVE, null, 3000);	
		
		service = new ProductService();
		service.rateService = mockRateService();
		service.taxRateService = mockTaxRateService()
    }

    void tearDown() {
        // Tear down logic here
    }

    void testSearch() {
		Map criteria = [:]
		criteria["code"] = "p1"		
		Page page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(1, page.list.size());
		
		criteria = [:]
		criteria["name"] = "nike"
		page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(2, page.list.size());
		
		criteria = [:]
		criteria["categoryId"] = adult.id
		page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(3, page.list.size());
		
		criteria = [:]
		criteria["brandId"] = hideBrand.id
		page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(1, page.list.size());
		
		criteria = [:]
		criteria["priceMin"] = 2200
		page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(1, page.list.size());

		criteria = [:]
		criteria["priceMax"] = 2200
		page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(2, page.list.size());

		criteria = [:]
		criteria["priceMin"] = 1500
		criteria["priceMax"] = 2700
		page = service.search(new Locale("fr", "FR"), "EUR", company.id, criteria)
		assertNotNull(page);
		assertNotNull(page.list);
		assertEquals(1, page.list.size());
    }
}
