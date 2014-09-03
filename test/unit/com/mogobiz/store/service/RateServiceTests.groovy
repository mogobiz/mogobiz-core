package com.mogobiz.store.service

import com.mogobiz.AbstractTestService
import com.mogobiz.service.RateService

class RateServiceTests extends AbstractTestService {
	
	RateService service;

    void setUp() {
		
		service = mockRateService()
    }

    void tearDown() {
        // Tear down logic here
    }

	void testFormat() {
		String r = service.format(1, "EUR", new Locale("fr", "FR"));
		assertEquals("0,01 €", r)
		
		r = service.format(100, "EUR", Locale.forLanguageTag("fr-FR"));
		assertEquals("1,00 €", r)
		
		r = service.format(100000, "EUR", Locale.forLanguageTag("fr-FR"));
		assertEquals("1 000,00 €", r)
		
		r = service.format(1, "USD", Locale.forLanguageTag("en-US"));
		assertEquals("\$0.01", r)
		
		r = service.format(100, "USD", Locale.forLanguageTag("en-US"));
		assertEquals("\$1.37", r)
		
		r = service.format(100000, "USD", Locale.forLanguageTag("en-US"));
		assertEquals("\$1,374.85", r)
	}
	
    void testInverse() {
		Long montant = service.inverse(null, "EUR");
		assertNull(montant);
		
		montant = service.inverse(1, "EUR");
		assertNotNull(montant);
		assertEquals(100, montant)
		
		montant = service.inverse(1, "USD");
		assertNotNull(montant);
		assertEquals(72, montant)

		montant = service.inverse(100, "EUR");
		assertNotNull(montant);
		assertEquals(10000, montant)
		
		montant = service.inverse(100, "USD");
		assertNotNull(montant);
		assertEquals(7273, montant)
    }
}
