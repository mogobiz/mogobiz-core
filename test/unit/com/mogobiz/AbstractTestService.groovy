package com.mogobiz

import com.mogobiz.service.ProductService
import com.mogobiz.service.TaxRateService
import com.mogobiz.store.domain.*
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class AbstractTestService {
	
	Company company
	Catalog catalog
	
	void setUp() {
		company = new Company(code: "Company", name: "Company", aesPassword: "aesPassword");
		saveEntity(company)

		catalog = new Catalog(name:"Default Catalog", uuid:"uuid", social:false,activationDate:new Date(), company:company)
		saveEntity(catalog)		
	}
	
	protected TaxRateService mockTaxRateService() {
		TaxRateService service = new TaxRateService();
		service.metaClass.findTaxRate = { TaxRate taxRate, String country -> 
			return 19.6
		}
		return service
	}
	
	protected void saveEntity(def entity)
	{
		String msg = "";
		if (entity.validate())
		{
			entity.save(flush: true)
			if (entity instanceof Category) {
				entity.categoryRender = new CategoryRender()
				entity.categoryValidation = new CategoryValidation()
			}
			if (entity instanceof Brand) {
				entity.brandRender = new BrandRender()
				entity.brandValidation = new BrandValidation()
			}
			if (entity instanceof LocalTaxRate) {
				entity.localTaxRateRender = new LocalTaxRateRender()
				entity.localTaxRateValidation = new LocalTaxRateValidation()
			}
			if (entity instanceof TaxRate) {
				entity.taxRateRender = new TaxRateRender()
				entity.taxRateValidation = new TaxRateValidation()
			}
			if (entity instanceof Product) {
				ProductRender productRender = new ProductRender();
				productRender.productService = new ProductService();
				productRender.productService.metaClass.retrievePicture = { Product product -> return null }
				productRender.productService.metaClass.getPictures = { return null }
				entity.productRender = productRender
				entity.productValidation = new ProductValidation()
			}
			if (entity instanceof Tag) {
				entity.tagRender = new TagRender()
				entity.tagValidation = new TagValidation()
			}
		}
		else
		{
			entity.errors?.allErrors?.each{error ->
				msg += (error.toString() + "\n")
			}
			throw new Exception(msg);
		}
	}

	protected Calendar getDateDebutAnnee()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c;
	}

	protected Calendar getDateFinAnnee()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, 11);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		return c;
	}

	protected Calendar getDateDebutMois()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c;
	}
	
	protected Calendar getDateDebutMois10Heure()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 10);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c;
	}

	protected Calendar getDateFinMois()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		return c;
	}
	
	protected Calendar getDateFinMoisMidi()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c;
	}

	protected TaxRate createTaxRate()
	{
		LocalTaxRate frTaxRate = new LocalTaxRate(rate: 1, active: true, countryCode: "FR");
		saveEntity(frTaxRate)
		
		TaxRate taxRate = new TaxRate(name: "taxRate", company: company);
		taxRate.addToLocalTaxRates(frTaxRate);
		saveEntity(taxRate)
		return taxRate;
	}
	
	protected Product createProduct(String code, String name, Company company, Brand brand, Category category, TaxRate taxRate, ProductState state = ProductState.ACTIVE, List<Tag> listTag = [], long montant = 1000)
	{
		Product product = new Product(code: code,
			name: name,
			price: montant,
			description: "description",
			descriptionAsText: "descriptionAsText",
			xtype: ProductType.PRODUCT,
			state: state,
			startDate: getDateDebutAnnee(),
			stopDate: getDateFinAnnee(),
			startFeatureDate: getDateDebutMois(),
			stopFeatureDate: getDateFinMois(),
			calendarType: ProductCalendar.NO_DATE,
			company: company,
			brand: brand,
			taxRate: taxRate
		);
		product.category = category
		listTag.each {Tag t -> 
			product.addToTags(t)
		}
		saveEntity(product);
		return product;
	}

}
