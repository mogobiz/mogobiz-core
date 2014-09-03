package com.mogobiz.utils

import grails.test.mixin.*
import grails.test.mixin.support.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class JahiaQueryUtilTests {

	private String [] listeRequetes;
	
    void setUp() {
		listeRequetes = [
			"(NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'fr')", 
			"(ebiznt:product.hide = CAST('false' AS BOOLEAN)) AND ((NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'en'))",
			"(ebiznt:product.startFeatureDate > CAST('2013-07-01T00:00:00.000+0200' AS DATE)) AND ((NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'en'))",
			"(ebiznt:product.startFeatureDate >= CAST('2013-07-01T00:00:00.000Z' AS DATE) AND ebiznt:product.stopFeatureDate <= CAST('2013-07-01T00:00:00.000Z' AS DATE)) AND ((NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'en'))",
			"((ebiznt:product.startFeatureDate <= CAST('2013-07-01T00:00:00.000Z' AS DATE)) AND (ebiznt:product.stopFeatureDate >= CAST('2013-07-01T00:00:00.000Z' AS DATE))) AND (NOT ((ebiznt:product.[jcr:language] IS NOT NULL) OR (ebiznt:product.[jcr:language] = 'en')))"
		]
    }

    void tearDown() {
        // Tear down logic here
    }
	
	void testRemovePrefixe() {
		String [] listeAttendues = [
			"(NOT (product.[jcr:language] IS NOT NULL)) OR (product.[jcr:language] = 'fr')", 
			"(product.hide = CAST('false' AS BOOLEAN)) AND ((NOT (product.[jcr:language] IS NOT NULL)) OR (product.[jcr:language] = 'en'))",
			"(product.startFeatureDate > CAST('2013-07-01T00:00:00.000+0200' AS DATE)) AND ((NOT (product.[jcr:language] IS NOT NULL)) OR (product.[jcr:language] = 'en'))",
			"(product.startFeatureDate >= CAST('2013-07-01T00:00:00.000Z' AS DATE) AND product.stopFeatureDate <= CAST('2013-07-01T00:00:00.000Z' AS DATE)) AND ((NOT (product.[jcr:language] IS NOT NULL)) OR (product.[jcr:language] = 'en'))",
			"((product.startFeatureDate <= CAST('2013-07-01T00:00:00.000Z' AS DATE)) AND (product.stopFeatureDate >= CAST('2013-07-01T00:00:00.000Z' AS DATE))) AND (NOT ((product.[jcr:language] IS NOT NULL) OR (product.[jcr:language] = 'en')))"
		];
		
		for (int i = 0; i < listeRequetes.size(); i++)
		{
			assertEquals(listeAttendues[i], JahiaQueryUtil.removePrefixe(listeRequetes[i]));
		}
	}
	
	void testRemoveLanguage() {
		String [] listeAttendues = [
			"(NOT ) OR ",
			"(product.hide = CAST('false' AS BOOLEAN)) AND ((NOT ) OR )",
			"(product.startFeatureDate > CAST('2013-07-01T00:00:00.000+0200' AS DATE)) AND ((NOT ) OR )",
			"(product.startFeatureDate >= CAST('2013-07-01T00:00:00.000Z' AS DATE) AND product.stopFeatureDate <= CAST('2013-07-01T00:00:00.000Z' AS DATE)) AND ((NOT ) OR )",
			"((product.startFeatureDate <= CAST('2013-07-01T00:00:00.000Z' AS DATE)) AND (product.stopFeatureDate >= CAST('2013-07-01T00:00:00.000Z' AS DATE))) AND (NOT ( OR ))"
		];
		
		for (int i = 0; i < listeRequetes.size(); i++)
		{
			assertEquals(listeAttendues[i], JahiaQueryUtil.removeLanguage(JahiaQueryUtil.removePrefixe(listeRequetes[i])));
		}
	}

	void testTransformeCast() {
		String [] listeAttendues = [
			"(NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'fr')", 
			"(ebiznt:product.hide = false) AND ((NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'en'))",
			"(ebiznt:product.startFeatureDate > '2013-07-01 00:00:00.000+0200') AND ((NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'en'))",
			"(ebiznt:product.startFeatureDate >= '2013-07-01 00:00:00.000Z' AND ebiznt:product.stopFeatureDate <= '2013-07-01 00:00:00.000Z') AND ((NOT (ebiznt:product.[jcr:language] IS NOT NULL)) OR (ebiznt:product.[jcr:language] = 'en'))",
			"((ebiznt:product.startFeatureDate <= '2013-07-01 00:00:00.000Z') AND (ebiznt:product.stopFeatureDate >= '2013-07-01 00:00:00.000Z')) AND (NOT ((ebiznt:product.[jcr:language] IS NOT NULL) OR (ebiznt:product.[jcr:language] = 'en')))"
		];
		
		for (int i = 0; i < listeRequetes.size(); i++)
		{
			assertEquals(listeAttendues[i], JahiaQueryUtil.transformeCast(listeRequetes[i]));
		}
	}

    void testTransformeJahiaQuery() {
		String [] listeAttendues = [
			"", 
			"(product.hide = false)",
			"(product.startFeatureDate > '2013-07-01 00:00:00.000+0200')",
			"(product.startFeatureDate >= '2013-07-01 00:00:00.000Z' AND product.stopFeatureDate <= '2013-07-01 00:00:00.000Z')",
			"((product.startFeatureDate <= '2013-07-01 00:00:00.000Z') AND (product.stopFeatureDate >= '2013-07-01 00:00:00.000Z'))"
		];
		
		for (int i = 0; i < listeRequetes.size(); i++)
		{
			assertEquals(listeAttendues[i], JahiaQueryUtil.transformeJahiaQuery(listeRequetes[i]));			
		}
    }
}
