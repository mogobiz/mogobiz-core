package bootstrap

import com.mogobiz.authentication.ProfileService
import com.mogobiz.geolocation.domain.Location
import com.mogobiz.store.domain.*
import com.mogobiz.utils.PermissionType
import grails.util.Holders
import org.apache.shiro.crypto.hash.Sha256Hash
import org.hibernate.SessionFactory

class PerfCommerceService {

    CommonService commonService
    SessionFactory sessionFactory
    ThreadLocal<Map> propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    ProfileService profileService

    def destroy() {}

    void init(int level1, int level2, int maxProducts) {
        log.info("Begin Performance catalog creation")
        // création de l'adresse de la compagnie
        Location adresseMogobiz = Location.findByPostalCodeAndCity("92800", "Puteaux")
        if (adresseMogobiz == null) {
            adresseMogobiz = new Location(road1: "4 Place de la Défense", postalCode: "92800", city: "Puteaux", countryCode: 'FR');
            commonService.saveEntity(adresseMogobiz)
        }

        // création des compagnies
        Company mogobiz = Company.findByCode("mogobiz")
        if (mogobiz == null) {
            mogobiz = new Company(code: "mogobiz", name: "Mogobiz", location: adresseMogobiz, website: "http://www.ebiznext.com", aesPassword: "5c3f3da15cae1bf2bc736b95bda10c78", email: "contact@mogobiz.com")
            GoogleEnv googleEnv = new GoogleEnv(
                    merchant_id: '100653663',
                    merchant_url: Holders.config.grails.serverURL,
                    client_id: 'mogobiz@gmail.com',
                    client_secret: 'e-z12B24',
                    cronExpr: '0 * * * * ?',
                    running: false,
                    dry_run: true,
                    version: 2,
                    active: false
            )
            commonService.saveEntity(googleEnv)
            mogobiz.googleEnv = googleEnv
            commonService.saveEntity(mogobiz)

            Catalog catalog = new Catalog(name: "Performance Catalog", uuid: UUID.randomUUID().toString(), social: false, activationDate: new Date(), company: mogobiz)
            commonService.saveEntity(catalog)

            // création des TaxRate
            LocalTaxRate frTaxRate = new LocalTaxRate(rate: 19.6, active: true, countryCode: "FR");
            commonService.saveEntity(frTaxRate)
            LocalTaxRate usaAlTaxRate = new LocalTaxRate(rate: 9.0, active: true, countryCode: "USA", stateCode: "USA.AL");
            commonService.saveEntity(usaAlTaxRate)

            TaxRate taxRate = new TaxRate(name: "TaxRate", company: mogobiz);
            taxRate.addToLocalTaxRates(frTaxRate);
            taxRate.addToLocalTaxRates(usaAlTaxRate);
            commonService.saveEntity(taxRate)

            EsEnv env = new EsEnv(
                    name: 'dev',
                    url: Holders.config.elasticsearch.serverURL as String,
                    cronExpr: Holders.config.elasticsearch.export.cron as String,
                    company: mogobiz,
                    active: true
            )
            commonService.saveEntity(env)
        }
        Catalog mogobizCatalog = Catalog.findByNameAndCompany("Performance Catalog", mogobiz);

        // création des sellers
        Seller seller = Seller.findByLogin("partner@mogobiz.com")
        if (seller == null) {
            seller = new Seller(login: "partner@mogobiz.com", email: "partner@mogobiz.com", password: new Sha256Hash('changeit').toHex(),
                    firstName: 'rector', lastName: 'Dir', active: true, company: mogobiz, location: adresseMogobiz, admin: true)
            seller.addToRoles(commonService.createRole(RoleName.PARTNER))
            seller.addToCompanies(mogobiz)
            commonService.saveEntity(seller)
        }

        profileService.saveUserPermission(seller, seller.admin, PermissionType.ADMIN_COMPANY, seller.company.id as String)

        // création du valideur
        Seller userValidator = Seller.findByLogin("validator@mogobiz.com")
        if (userValidator == null) {
            userValidator = new Seller(login: "validator@mogobiz.com", email: "validator@mogobiz.com", password: new Sha256Hash('changeit').toHex(),
                    firstName: 'Valid', lastName: 'ator', active: true, company: mogobiz, location: adresseMogobiz, admin: true)
            userValidator.addToRoles(commonService.createRole(RoleName.VALIDATOR))
            userValidator.addToCompanies(mogobiz)
            commonService.saveEntity(userValidator)
        }

        Company company = Company.findByCode("mogobiz");
        TaxRate taxRate = TaxRate.findByNameAndCompany("TaxRate", company);
        // Création des marques
        Brand samsung = commonService.createBrand("Samsung", "http://www.samsung.com/fr", company);
        commonService.createTranslation(company, 'de', samsung.id, [website: 'http://www.samsung.com/de'])
        commonService.createTranslation(company, 'en', samsung.id, [website: 'http://www.samsung.com'])
        commonService.createTranslation(company, 'es', samsung.id, [website: 'http://www.samsung.com/es'])
        commonService.createTranslation(company, 'fr', samsung.id, [website: 'http://www.samsung.com/fr'])

        commonService.createBrand("Philips", "http://www.philips.com", company);

        Brand nike = commonService.createBrand("Nike", "http://www.nike.com/fr/fr_fr/", company)
        commonService.createTranslation(company, 'de', nike.id, [website: 'http://www.nike.com/de/de_de/'])
        commonService.createTranslation(company, 'en', nike.id, [website: 'http://www.nike.com'])
        commonService.createTranslation(company, 'es', nike.id, [website: 'http://www.nike.com/es/es_es/'])
        commonService.createTranslation(company, 'fr', nike.id, [website: 'http://www.nike.com/fr/fr_fr/'])

        Brand puma = commonService.createBrand("Puma", "http://www.shop.puma.fr", company);
        commonService.createTranslation(company, 'de', puma.id, [website: 'http://www.shop.puma.de'])
        commonService.createTranslation(company, 'en', puma.id, [website: 'http://www.puma.com'])
        commonService.createTranslation(company, 'es', puma.id, [website: 'http://www.puma.com'])
        commonService.createTranslation(company, 'fr', puma.id, [website: 'http://www.shop.puma.fr'])

        commonService.createBrand("Hide brand", "http://www.google.fr", company, true);

        int countInserts = 0;
        // création des categories
        for (int i = 1; i <= level1; i++) {
            Category cat = commonService.createCategory("Main $i", null, mogobiz, mogobizCatalog, 1, "hello, I am category Main $i from catalog ${mogobizCatalog.name}");
            for (int j = 1; j <= level2; j++) {
                log.info("Begin Performance Sub category creation $i$j")
                Category subcat = commonService.createCategory("Sub $i$j", cat, mogobiz, mogobizCatalog, 1, "hello, I am category Sub $i$j from catalog ${mogobizCatalog.name}");

                // création des variations
                commonService.createVariation("Couleur", 1, cat, ["Blanc", "Rouge", "Noir"]);
                commonService.createVariation("Taille", 2, cat, ["S", "M", "L"]);
                commonService.createTranslation(company, subcat.id, "en", '{"name": "SubName for en $i$j"}', "CATEGORY");
                commonService.createTranslation(company, subcat.id, "en", '{"name": "SubName for es $i$j"}', "CATEGORY");
                commonService.createTranslation(company, subcat.id, "en", '{"name": "SubName for fr $i$j"}', "CATEGORY");
                commonService.createTranslation(company, subcat.id, "en", '{"name": "SubName for de $i$j"}', "CATEGORY");

                // Création des produits
                for (int k = 1; k < maxProducts; k++) {
                    Shipping shipping = commonService.createShipping(25 + k, 120, 110, 15)
                    Product product = commonService.createProduct("PRODUCT_$i$j$k", "Product of sub cat $i$j with id $k", 28000 + (k * 100), ProductType.PRODUCT, ProductCalendar.NO_DATE, company, samsung, subcat, taxRate, "Product $i$j$k", null, shipping, null, false)
                    commonService.createSKU("Standard$i$j$k", 30000 + (k * 100), product, k % 2 == 0 ? "Blanc" : "Rouge", k % 2 == 0 ? "M" : "L", null, 10000 + (k * 100), false)
                    Feature feature = commonService.createFeature("Frabriqué en ", "Country $k", product, 0, false)
                    commonService.createTranslation(company, 'en', feature.id, [name: "Made in", value: "China"], false)
                    commonService.createTranslation(company, 'es', feature.id, [name: "Fabricado :)", value: "China"], false)
                    commonService.createFeature("Size", "100\"", product, 0, false)
                    commonService.createFeature("Resulution", "Full HD", product, 1, false)
                    countInserts++;
                    if (countInserts % 100 == 0) {
                        log.info(countInserts)
                        this.cleanUpGorm()
                    }
                }
                log.info("End Performance Sub category creation $i$j")
            }
        }
        commonService.createShippingRule(company, "fr", 0L, 500L, "-10")
        commonService.createShippingRule(company, "fr", 5001L, 1000L, "-100")
        commonService.createShippingRule(company, "fr", 10001L, 10000L, "-10%")
        log.info("End Performance catalog creation")
    }
    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

}
