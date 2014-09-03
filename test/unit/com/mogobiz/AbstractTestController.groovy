package com.mogobiz

import com.mogobiz.store.domain.Brand;
import com.mogobiz.store.domain.Catalog;
import com.mogobiz.store.domain.Category;
import com.mogobiz.store.domain.Company;
import com.mogobiz.store.domain.Product;
import com.mogobiz.store.domain.ProductCalendar;
import com.mogobiz.store.domain.ProductState;
import com.mogobiz.store.domain.ProductType;
import com.mogobiz.store.domain.Seller;
import com.mogobiz.store.domain.Stock;
import com.mogobiz.store.domain.TicketType;
import com.mogobiz.store.domain.Translation;
import com.mogobiz.authentication.AuthenticationService;

import grails.test.mixin.Mock;
import grails.test.mixin.TestMixin;
import grails.test.mixin.support.GrailsUnitTestMixin;

@TestMixin(GrailsUnitTestMixin)
@Mock([Company, Seller, Catalog, Brand, Category, Product, TicketType, Stock, Translation])
class AbstractTestController {
	
	Company company;
	Seller seller;
	Catalog catalog;
	Category categorieParent;
	Brand nike;
	Brand puma;
	Category category;
	Product produitPull;
	TicketType sku;
	def mockForAuthenticationService;
	
	void setUp()
	{
		company = new Company(code: "Company", name: "Company", aesPassword: "aesPassword");
		saveEntity(company)
		
		seller = new Seller(login: "seller", email: "seller@ebiznext.com", password: "seller", active:true, company:company, admin:true);
		saveEntity(seller)

		catalog = new Catalog(name:"Default Catalog", uuid:"uuid", social:false,activationDate:new Date(), company:company)
		saveEntity(catalog)
		saveEntity(new Translation(target: catalog.id, lang: "en", value: '{"name": "English Catalog"}'));

		categorieParent = new Category(name: "Parent", catalog: catalog, company: company, uuid: "uuid", position: 10);
		saveEntity(categorieParent)
		
		nike = new Brand(name: "Nike", company: company);
		saveEntity(nike);
		puma = new Brand(name: "Puma", company: company);
		saveEntity(puma);

		category = new Category(name: "Habillement", catalog: catalog, company: company, parent: categorieParent, uuid: "uuid", position: 10);
		saveEntity(category);

		produitPull = new Product(code: "Pull_Nike", name: "Pull Nike", price: 1000, description: "Pull Nike de très bonne qualité",
			descriptionAsText: "Pull Nike de très bonne qualité", xtype: ProductType.PRODUCT, state: ProductState.ACTIVE,
			startDate: getDateDebutAnnee(), stopDate: getDateFinAnnee(), startFeatureDate: getDateDebutMois(), stopFeatureDate: getDateFinMois(),
			calendarType: ProductCalendar.DATE_TIME, creationDate: Calendar.getInstance(), company: company, brand: nike
		);
		produitPull.addToCategories(category)
		saveEntity(produitPull);
		
		Stock stock = new Stock(stock: 10, stockUnlimited: false);
		saveEntity(stock);
		
		sku = new TicketType(sku: "BlancS", name: "Blanc taille S", price: 1000, minOrder: 1, maxOrder: 1, product: produitPull, stock: stock, startDate: produitPull.startDate, stopDate: produitPull.stopDate);
		saveEntity(sku);

		mockForAuthenticationService = mockFor(AuthenticationService, true);
	}

	void tearDown() {
		resetGrailsApplication();
		// Tear down logic here
	}

	
	private static Calendar getDateDebutAnnee()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c;
	}
		
	private static Calendar getDateFinAnnee()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, 11);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		return c;
	}
	
	private static Calendar getDateDebutMois()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c;
	}
		
	private static Calendar getDateFinMois()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		return c;
	}

	private static void saveEntity(def entite)
	{
		String msg = "";
		if (entite.validate())
		{
			entite.save(flush: true)
		}
		else
		{
			entite.errors?.allErrors?.each{error ->
				msg += (error.toString() + "\n")
			}
			throw new Exception(msg);
		}
	}

}
