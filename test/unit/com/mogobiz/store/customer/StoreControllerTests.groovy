package com.mogobiz.store.customer

import static org.junit.Assert.*
import grails.test.mixin.TestFor
import com.mogobiz.AbstractTestController
import com.mogobiz.store.domain.Seller
import com.mogobiz.service.SkuService

/**
 * Test du controller StoreController
 */
@TestFor(StoreController)
class StoreControllerTests extends AbstractTestController {
		
	@Override
	public void setUp() {
		super.setUp();
		authenticate(null);
	}
	
	private void authenticate(Seller seller)
	{
		mockForAuthenticationService.demand.retrieveAuthenticatedSeller(1..Integer.MAX_VALUE) { -> return seller }
		controller.authenticationService = mockForAuthenticationService.createMock();
		
		def mockForSKUService = mockFor(SkuService, true);
		mockForSKUService.demand.listSalableSKUByProduct(1..Integer.MAX_VALUE) { 
			return [sku] 
		}
		controller.skuService = mockForSKUService.createMock();		
	}

	/**
	 * Cas d'un appel sans vendeur authentifié
	 */
	void testShowCatalog401() 
	{		
		authenticate(null);
		params["format"] = "json";
			
		controller.showCatalog();
				
		assertEquals(401, response.status);
	}

	/**
	 * Cas d'un appel sans le paramètre id
	 */
    void testShowCatalogAll() 
	{
		authenticate(seller);
		params["format"] = "json";
				
        controller.showCatalog();
				
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(1, json.size());
		assertEquals(catalog.name, json[0].name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id existant en base
	 */
    void testShowCatalogById()
	{
		authenticate(seller);
		params["format"] = "json";
		params["id"] = company.id;
		
        controller.showCatalog();
		
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(catalog.name, json.name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id inexistant en base
	 */
    void testShowCatalogById404() 
	{
		authenticate(seller);
		
		params["format"] = "json";
		params["id"] = 0;
		
        controller.showCatalog();
		
		assertEquals(404, response.status);
    }

	/**
	 * Cas d'un appel sans vendeur authentifié
	 */
	void testShowBrand401() 
	{		
		authenticate(null);
		params["format"] = "json";
			
		controller.showBrand()
				
		assertEquals(401, response.status);
	}

	/**
	 * Cas d'un appel sans le paramètre id
	 */
    void testShowBrandAll() 
	{
		authenticate(seller);
		params["format"] = "json";
				
        controller.showBrand();
				
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(2, json.size());
		assertEquals(nike.name, json[0].name);
		assertEquals(puma.name, json[1].name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id existant en base
	 */
    void testShowBrandById()
	{
		authenticate(seller);
		params["format"] = "json";
		params["id"] = nike.id;
		
        controller.showBrand();
		
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(nike.name, json.name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id inexistant en base
	 */
    void testShowBrandById404() 
	{
		authenticate(seller);
		
		params["format"] = "json";
		params["id"] = 0;
		
        controller.showBrand();
		
		assertEquals(404, response.status);
    }

	/**
	 * Cas d'un appel sans vendeur authentifié
	 */
	void testShowCategory401() 
	{		
		authenticate(null);
		params["format"] = "json";
			
		controller.showCategory()
				
		assertEquals(401, response.status);
	}
	
	/**
	 * Cas d'un appel avec un paramètre id existant en base
	 */
    void testShowCategoryById()
	{
		authenticate(seller);
		params["format"] = "json";
		params["id"] = category.id;
		
        controller.showCategory();
		
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(category.name, json.name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id inexistant en base
	 */
    void testShowCategoryById404() 
	{
		authenticate(seller);
		
		params["format"] = "json";
		params["id"] = 0;
		
        controller.showCategory();
		
		assertEquals(404, response.status);
    }

	/**
	 * Cas d'un appel sans le paramètre id
	 */
    void testShowCategoryByParent() 
	{
		authenticate(seller);
		params["parentId"] = categorieParent.id;
		params["format"] = "json";
				
        controller.showCategory();
				
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(1, json.size());
		assertEquals(category.name, json[0].name);
    }

	/**
	 * Cas d'un appel sans le paramètre id
	 */
    void testShowCategoryByCatalogue() 
	{
		authenticate(seller);
		params["catalogId"] = catalog.id;
		params["format"] = "json";
				
        controller.showCategory();
				
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(1, json.size());
		assertEquals(categorieParent.name, json[0].name);
    }

	/**
	 * Cas d'un appel sans vendeur authentifié
	 */
	void testShowProduct401() 
	{		
		authenticate(null);
		params["format"] = "json";
			
		controller.showProduct()
				
		assertEquals(401, response.status);
	}
	
	/**
	 * Cas d'un appel avec un paramètre id existant en base
	 */
    void testShowProductById()
	{
		authenticate(seller);
		params["format"] = "json";
		params["id"] = produitPull.id;
		
        controller.showProduct();
		
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(produitPull.name, json.name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id inexistant en base
	 */
    void testShowProductById404() 
	{
		authenticate(seller);
		
		params["format"] = "json";
		params["id"] = 0;
		
        controller.showProduct();
		
		assertEquals(404, response.status);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id inexistant en base
	 */
    void testShowProductByCategory() 
	{
		authenticate(seller);
		
		params["format"] = "json";
		params["categoryId"] = category.id;
		
        controller.showProduct();
		
		def json = response.json;
		assertNotNull(json);
		assertEquals(1, json.size());
		assertEquals(produitPull.name, json[0].name);
    }

	/**
	 * Cas d'un appel sans vendeur authentifié
	 */
	void testShowSKU401() 
	{		
		authenticate(null);
		params["format"] = "json";
			
		controller.showSKU()
				
		assertEquals(401, response.status);
	}
	
	/**
	 * Cas d'un appel avec un paramètre id existant en base
	 */
    void testShowSKUById()
	{
		authenticate(seller);
		params["format"] = "json";
		params["id"] = sku.id;
		
        controller.showSKU();
		
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(sku.name, json.name);
    }
	
	/**
	 * Cas d'un appel avec un paramètre id inexistant en base
	 */
    void testShowSKUById404() 
	{
		authenticate(seller);
		
		params["format"] = "json";
		params["id"] = -1;
		
        controller.showSKU();
		
		assertEquals(404, response.status);
    }

	/**
	 * Cas d'un appel sans le paramètre id
	 */
    void testShowSKUByProduct() 
	{
		authenticate(seller);
		params["format"] = "json";
		params["productId"] = produitPull.id;
				
        controller.showSKU();
				
		assertEquals(200, response.status);
		def json = response.json;
		assertNotNull(json);
		assertEquals(1, json.size());
		assertEquals(sku.name, json[0].name);
    }
}
