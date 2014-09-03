package com.mogobiz.store.service

import com.mogobiz.AbstractTestService;
import com.mogobiz.store.domain.Catalog;
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Company;
import com.mogobiz.store.domain.Translation
import com.mogobiz.service.CategoryService;
import grails.test.mixin.Mock;

@Mock([Company, Catalog, Category, Translation])
class CategoryServiceTests extends AbstractTestService {
	
	CategoryService service;

	Category adult
	Category shirtAdult
	Category pantsAdult
	Category child
	Category shirtChild
	Category pantsChild
	
    void setUp() {
		super.setUp()
		
		adult = new Category(name: "Adult", catalog: catalog, company: company, uuid: "uuid", position: 10);
		saveEntity(adult)
		
		shirtAdult = new Category(name: "Shirt Adult", catalog: catalog, company: company, parent: adult, uuid: "uuid", position: 10);
		saveEntity(shirtAdult);
		saveEntity(new Translation(target: shirtAdult.id, lang: "fr", value: '{"name": "Chemise Adulte"}'));		

		pantsAdult = new Category(name: "Pants Adult", catalog: catalog, company: company, parent: adult, uuid: "uuid", position: 10);
		saveEntity(pantsAdult);
		saveEntity(new Translation(target: pantsAdult.id, lang: "fr", value: '{"name": "Pantalon Adulte"}'));
		
		child = new Category(name: "Child", catalog: catalog, company: company, uuid: "uuid", position: 10);
		saveEntity(child)
		
		shirtChild = new Category(name: "Shirt Child", catalog: catalog, company: company, parent: child, uuid: "uuid", position: 10);
		saveEntity(shirtChild);
		saveEntity(new Translation(target: shirtChild.id, lang: "fr", value: '{"name": "Chemise Enfant"}'));
		
		pantsChild = new Category(name: "Pants Child", catalog: catalog, company: company, parent: child, uuid: "uuid", position: 10);
		saveEntity(pantsChild);
		saveEntity(new Translation(target: pantsChild.id, lang: "fr", value: '{"name": "Pantalon Enfant"}'));
		
		service = new CategoryService();
    }

    void tearDown() {
        // Tear down logic here
    }

    void testListVisibleByCompany() {
		List<Map> list = service.listVisibleByCompany(Locale.US, -1, null);
		assertNotNull(list);
		assertEquals(0, list.size());
		
		list = service.listVisibleByCompany(Locale.US, company.id, null);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(adult.id, list[0].id);
		assertEquals(child.id, list[1].id);

		list = service.listVisibleByCompany(Locale.US, company.id, adult.id);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(shirtAdult.id, list[0].id);
		assertEquals(shirtAdult.name, list[0].name);
		assertEquals(pantsAdult.id, list[1].id);
		assertEquals(pantsAdult.name, list[1].name);
		
		list = service.listVisibleByCompany(Locale.FRANCE, company.id, adult.id);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(shirtAdult.id, list[0].id);
		assertEquals("Chemise Adulte", list[0].name);
		assertEquals(pantsAdult.id, list[1].id);
		assertEquals("Pantalon Adulte", list[1].name);
    }
}
