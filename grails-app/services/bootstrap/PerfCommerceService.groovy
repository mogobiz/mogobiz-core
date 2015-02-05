package bootstrap

import com.mogobiz.geolocation.domain.Location
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.BOCart
import com.mogobiz.store.domain.Brand
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.EsEnv
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.GoogleEnv
import com.mogobiz.store.domain.Ibeacon
import com.mogobiz.store.domain.LocalTaxRate
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.ProductType
import com.mogobiz.store.domain.ReductionRule
import com.mogobiz.store.domain.ReductionRuleType
import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.Shipping
import com.mogobiz.store.domain.Tag
import com.mogobiz.store.domain.TaxRate
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.UserPermission
import com.mogobiz.store.vo.RegisteredCartItemVO
import grails.util.Holders
import org.apache.shiro.crypto.hash.Sha256Hash

class PerfCommerceService {

    CommonService commonService
    def destroy() {}

    void init() {

        // création de l'adresse de la compagnie
        Location adresseMogobiz = Location.findByPostalCodeAndCity("92800", "Puteaux")
        if (adresseMogobiz == null)
        {
            adresseMogobiz = new Location(road1: "4 Place de la Défense", postalCode: "92800", city: "Puteaux", countryCode: 'FR');
            commonService.saveEntity(adresseMogobiz)
        }

        // création des compagnies
        Company mogobiz = Company.findByCode("mogobiz")
        if (mogobiz == null) {
            mogobiz = new Company(code: "mogobiz", name: "Mogobiz", location: adresseMogobiz, website: "http://www.ebiznext.com", aesPassword:"5c3f3da15cae1bf2bc736b95bda10c78", email:"contact@mogobiz.com")
            GoogleEnv googleEnv = new GoogleEnv(
                    merchant_id: '100653663',
                    merchant_url: Holders.config.grails.serverURL,
                    client_id: 'mogobiz@gmail.com',
                    client_secret: 'e-z12B24',
                    cronExpr: '0 * * * * ?',
                    running: false,
                    dry_run: true,
                    version: 2,
                    active:false
            )
            commonService.saveEntity(googleEnv)
            mogobiz.googleEnv = googleEnv
            commonService.saveEntity(mogobiz)

            Catalog catalog = new Catalog(name:"Default Catalog", uuid:UUID.randomUUID().toString(), social:false, activationDate:new Date(), company:mogobiz)
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
                    name:'dev',
                    url:Holders.config.elasticsearch.serverURL as String,
                    cronExpr: Holders.config.elasticsearch.export.cron as String,
                    company: mogobiz,
                    active: true
            )
            commonService.saveEntity(env)
        }
        Catalog mogobizCatalog = Catalog.findByNameAndCompany("Default Catalog", mogobiz);

        // création des sellers
        Seller seller = Seller.findByLogin("partner@mogobiz.com")
        if(seller == null) {
            seller = new Seller(login:"partner@mogobiz.com", email:"partner@mogobiz.com", password:new Sha256Hash('changeit').toHex(),
                    firstName: 'rector', lastName: 'Dir', active: true, company: mogobiz, location: adresseMogobiz, admin:true)
            seller.addToRoles(commonService.createRole(RoleName.PARTNER))
            seller.addToCompanies(mogobiz)
            commonService.saveEntity(seller)
        }

        Permission permission = Permission.findByTypeAndPossibleActions('org.apache.shiro.authz.permission.WildcardPermission', '*');
        UserPermission userPermission = UserPermission.createCriteria().get{
            eq('permission.id', permission?.id)
            eq('user.id', seller.id)
            eq('target', 'company:'+seller.company.id+':admin')
            eq('actions', '*')
        }
        if (seller.admin) {
            if(!userPermission) {
                userPermission = new UserPermission(permission:permission, user:seller, target:'company:'+seller.company.id+':admin',actions:'*')
                commonService.saveEntity(userPermission)
            }
        }
        else if(userPermission) { userPermission.delete() }

        // création du valideur
        Seller userValidator = Seller.findByLogin("validator@mogobiz.com")
        if (userValidator == null)
        {
            userValidator = new Seller(login:"validator@mogobiz.com", email:"validator@mogobiz.com", password:new Sha256Hash('changeit').toHex(),
                    firstName:'Valid', lastName:'ator', active:true, company: mogobiz, location: adresseMogobiz, admin:true)
            userValidator.addToRoles(commonService.createRole(RoleName.VALIDATOR))
            userValidator.addToCompanies(mogobiz)
            commonService.saveEntity(userValidator)
        }

        // création des categories
        commonService.createCategory("Habillement", null, mogobiz, mogobizCatalog, 1, "vetements homme femme enfant");
        Category parent = commonService.createCategory("Hightech", null, mogobiz, mogobizCatalog, 2);
        commonService.createCategory("Télévisions", parent, mogobiz, mogobizCatalog, 1, "TV télé télévision HD");
        commonService.createCategory("Cinéma", null, mogobiz, mogobizCatalog, 3);

        // Récupération des éléménts nécessaires pour créer les autres entitées
        Company company = Company.findByCode("mogobiz");
        TaxRate taxRate = TaxRate.findByNameAndCompany("TaxRate", company);
        Category categoryHabillage = Category.findByName("Habillement");
        Category categoryTV = Category.findByName("Télévisions");
        Category categoryCinema = Category.findByName("Cinéma");
        commonService.createTranslation(company, categoryTV.id, "en", '{"name": "Televisions"}', "CATEGORY");

        // Création des marques
        Brand samsung = commonService.createBrand("Samsung", "http://www.samsung.com/fr", company);
        commonService.createTranslation(company, 'de', samsung.id, [website: 'http://www.samsung.com/de'])
        commonService.createTranslation(company, 'en', samsung.id, [website: 'http://www.samsung.com'])
        commonService.createTranslation(company, 'es', samsung.id, [website: 'http://www.samsung.com/es'])
        commonService.createTranslation(company, 'fr', samsung.id, [website: 'http://www.samsung.com/fr'])

        Brand philips = commonService.createBrand("Philips", "http://www.philips.com", company);

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

        // création des variations
        commonService.createVariation("Couleur", 1, categoryHabillage, ["Blanc", "Rouge", "Noir"]);
        commonService.createVariation("Taille", 2, categoryHabillage, ["S", "M", "L"]);

        // Télévision
        Shipping shippingTVSamsungFullHD = commonService.createShipping(25, 120, 110, 15)
        Product tvSamsungFullHD = commonService.createProduct("TV_SS_1", "TV 100\" Full HD", 30000, ProductType.PRODUCT, ProductCalendar.NO_DATE, company, samsung, categoryTV, taxRate, "Full HD 100\" Television", null, shippingTVSamsungFullHD)
        commonService.createSKU("Standard", 30000, tvSamsungFullHD, null, null, null, 100)
        Feature tvSamsungFullHDMadeIn = commonService.createFeature("Fabriqué en", "Chine", tvSamsungFullHD, 0)
        commonService.createTranslation(company, 'en', tvSamsungFullHDMadeIn.id, [name: "Made in", value: "China"])
        commonService.createTranslation(company, 'es', tvSamsungFullHDMadeIn.id, [name: "Made in", value: "China"])
        commonService.createFeature("Size", "100\"", tvSamsungFullHD, 0)
        commonService.createFeature("Resulution", "Full HD", tvSamsungFullHD, 1)

        Shipping shippingTVSamsungHDReady = commonService.createShipping(25, 120, 110, 15)
        Product tvSamsungHDReady = commonService.createProduct("TV_SS_2", "TV 100\" HD", 35000, ProductType.PRODUCT, ProductCalendar.NO_DATE, company, samsung, categoryTV, taxRate, "HD Ready 100\" Television", null, shippingTVSamsungHDReady)
        commonService.createSKU("Standard", 35000, tvSamsungHDReady, null, null, null, 100)
        Feature tvSamsungHDReadyMadeIn = commonService.createFeature("Fabriqué en", "Chine", tvSamsungHDReady, 0)
        commonService.createTranslation(company, 'en', tvSamsungHDReadyMadeIn.id, [name: "Made in", value: "China"])
        commonService.createTranslation(company, 'es', tvSamsungHDReadyMadeIn.id, [name: "Made in", value: "China"])
        commonService.createFeature("Size", "100\"", tvSamsungHDReady, 1)
        commonService.createFeature("Resulution", "HD Ready", tvSamsungHDReady, 2)

        Shipping shippingTVPhilipsHDReady = commonService.createShipping(25, 105, 100, 15)
        Product tvPhilipsHDReady = commonService.createProduct("TV_PL", "TV 90\"", 25000, ProductType.PRODUCT, ProductCalendar.NO_DATE, company, philips, categoryTV, taxRate, "90\" Television", null, shippingTVPhilipsHDReady)
        commonService.createSKU("Standard", 25000, tvPhilipsHDReady, null, null, null, 100)
        Feature tvPhilipsHDReadyMadeIn = commonService.createFeature("Fabriqué en", "Chine", tvPhilipsHDReady, 0)
        commonService.createTranslation(company, 'en', tvPhilipsHDReadyMadeIn.id, [name: "Made in", value: "China"])
        commonService.createTranslation(company, 'es', tvPhilipsHDReadyMadeIn.id, [name: "Made in", value: "China"])
        commonService.createFeature("Size", "90\"", tvPhilipsHDReady, 1)
        commonService.createFeature("Resulution", "Standard", tvPhilipsHDReady, 2)

        // tags
        Tag pull = commonService.createTag('PULL', mogobiz)
        Tag tshirt = commonService.createTag('TSHIRT', mogobiz)
        Tag theater = commonService.createTag('THEATER', mogobiz)

        // Habillage
        Shipping shippingProduitPull = commonService.createShipping(2, 30, 2, 20)
        Product produitPull = commonService.createProduct("Pull_Nike", "Pull Nike", 1000, ProductType.PRODUCT, ProductCalendar.NO_DATE, company, nike, categoryHabillage, taxRate, "Pull Nike de très bonne qualité. Pull tricotté main dans des usines respectant l'environnement et le droit de travail. Ce pull est issu du commerce équitable et n'utilise aucun produit toxique. Il est fabriqué en France. Le reste de la description est juste là pour avoir une longue description pour vérifier que l'affichage s'effectue correctement.", [pull], shippingProduitPull);
        TicketType produitPullBancTailleS = commonService.createSKU("Blanc taille S", 1000, produitPull, "Blanc", "S", null, 10);
        commonService.createSKU("Blanc taille M", 1500, produitPull, "Blanc", "M", null, 10);
        commonService.createSKU("Blanc taille L", 2000, produitPull, "Blanc", "L", null, 10);
        commonService.createSKU("Rouge taille S", 1200, produitPull, "Rouge", "S", null, 10);
        commonService.createSKU("Rouge taille M", 1700, produitPull, "Rouge", "M", null, 10);
        commonService.createSKU("Rouge taille L", 2200, produitPull, "Rouge", "L", null, 10);
        commonService.createSKU("Noir taille S", 1500, produitPull, "Noir", "S", null, 10);
        commonService.createSKU("Noir taille M (épuisé)", 2000, produitPull, "Noir", "M", null, 0);
        commonService.createSKU("Noir taille L", 2500, produitPull, "Noir", "L", null, 10);
        commonService.createFeature("Made in", "China", produitPull, 0)
        commonService.createFeature("Respet Environnement", "No", produitPull, 1)
        commonService.createTranslation(company, produitPull.id, "en", '{"name": "Pull Nike Anglais"}', "PRODUCT");

        Shipping shippingProduitTShirt = commonService.createShipping(2, 30, 2, 20)
        Product produitTShirt = commonService.createProduct("TShirt_Puma", "TShirt Puma", 1000, ProductType.PRODUCT, ProductCalendar.NO_DATE, company, puma, categoryHabillage, taxRate, "TShirt de très bonne qualité. Pull tricotté main dans des usines respectant l'environnement et le droit de travail. Ce pull est issu du commerce équitable et n'utilise aucun produit toxique. Il est fabriqué en France. Le reste de la description est juste là pour avoir une longue description pour vérifier que l'affichage s'effectue correctement.", [tshirt], shippingProduitTShirt);
        commonService.createSKU("Blanc taille M", 1500, produitTShirt, "Blanc", "M", null, 10);
        commonService.createSKU("Blanc taille L (épuisé)", 2000, produitTShirt, "Blanc", "L", null, 0);
        commonService.createSKU("Rouge taille S", 1200, produitTShirt, "Rouge", "S", null, 10);
        commonService.createSKU("Rouge taille M", 1700, produitTShirt, "Rouge", "M", null, 10);
        commonService.createSKU("Rouge taille L", 2200, produitTShirt, "Rouge", "L", null, 10);
        commonService.createSKU("Noir taille S", 1500, produitTShirt, "Noir", "S", null, 10);
        commonService.createSKU("Noir taille M", 2000, produitTShirt, "Noir", "M", null, 10);
        commonService.createSKU("Noir taille L", 2500, produitTShirt, "Noir", "L", null, 10);

        Shipping shippingPackPullAndTShirt = commonService.createShipping(4, 30, 4, 20)
        Product packPullAndTShirt = commonService.createProduct("Pack_Pull_TShirt", "Pack Pull et TShirt", 1000, ProductType.PACKAGE, ProductCalendar.NO_DATE, company, puma, categoryHabillage, taxRate, "TShirt de très bonne qualité. Pull tricotté main dans des usines respectant l'environnement et le droit de travail. Ce pull est issu du commerce équitable et n'utilise aucun produit toxique. Il est fabriqué en France. Le reste de la description est juste là pour avoir une longue description pour vérifier que l'affichage s'effectue correctement.", [pull, tshirt], shippingPackPullAndTShirt);
        packPullAndTShirt.state = ProductState.INACTIVE
        packPullAndTShirt.save()
        commonService.createSuggestion(true, 1, "10%", produitPull, packPullAndTShirt)
        commonService.createSuggestion(true, 2, "-100", produitTShirt, packPullAndTShirt)

        // Cinema
        Product theatre = commonService.createProduct("LePereNoelEstUneOrdure", "Le père noël est une ordure", 7500, ProductType.SERVICE, ProductCalendar.DATE_ONLY, company, null, categoryCinema, taxRate, "Pièce de théatre", [theater], null, "theatre");
        commonService.createSKU("Child", 750, theatre, null, null, null, 50);
        TicketType theatreAdulte = commonService.createSKU("Adult", 1000, theatre, null, null, null, 150);
        commonService.createIntraDayPeriod(theatre, commonService.getDateDebutMois(), commonService.getDateFinMois(), false, false, true, false, true, true, true)

        Product cinema = commonService.createProduct("LesTontonsFlingeurs", "Les tontons flingeurs", 800, ProductType.SERVICE, ProductCalendar.DATE_TIME, company, null, categoryCinema, taxRate, "Séance de cinéma", [commonService.createTag('CINEMA', mogobiz)], null, "film action");
        commonService.createSKU("Child", 400, cinema, null, null, null, 50);
        commonService.createSKU("Adult", 800, cinema, null, null, null, 150);
        commonService.createIntraDayPeriod(cinema, commonService.getDateDebutMois(15), commonService.getDateFinMois(17), false, false, false, false, false, true, true)
        commonService.createIntraDayPeriod(cinema, commonService.getDateDebutMois(21), commonService.getDateFinMois(23), false, false, false, false, false, true, true)

        BOCart sale1 = commonService.createBOCart(company, "1");
        commonService.createBOCartItem(sale1, theatre, theatreAdulte, commonService.getDateDebutMois(), commonService.getDateFinMois(), new RegisteredCartItemVO(email: "test@test.com", lastname: "Test", firstname: "Test"));

        BOCart sale2 = commonService.createBOCart(company, "2");
        commonService.createBOCartItem(sale2, produitPull, produitPullBancTailleS, null, null, null);

        // Coupon
        ReductionRule regle3achete1Offer = commonService.createReductionRule(ReductionRuleType.X_PURCHASED_Y_OFFERED, null, 3, 1)
        ReductionRule regle10Pourcent = commonService.createReductionRule(ReductionRuleType.DISCOUNT, "10%")
        commonService.createCoupon(company, [regle3achete1Offer], "TEST1", "Pour 3 pulls ou tshirt achetés, 1 offert", null, null, [produitPull, produitTShirt])
        commonService.createCoupon(company, [regle10Pourcent], "TEST2", "Sold tout à 10%", null, [categoryTV])

        // IBeacon
        Ibeacon ibeacon = produitPull.ibeacon
        if(ibeacon == null){
            ibeacon = new Ibeacon(uuid: UUID.randomUUID(), name:"Ibeacon", startDate: commonService.getDateDebutMois(), endDate: commonService.getDateFinMois(), active: true, company: company)
            commonService.saveEntity(ibeacon)
            produitPull.ibeacon = ibeacon
            commonService.saveEntity(produitPull)
        }

        // Promotion
        commonService.createCoupon(company, [regle10Pourcent], "Promotion", "-10%", 0L, [categoryHabillage], null, null, "Promotion exceptionnelle de -10% sur tout l'habillement", true)
        commonService.createCoupon(company, [regle3achete1Offer], "Promo1pour3", "Pour 3 tshirt achetés, 1 offert", null, null, [produitTShirt],null,"Pour 3 pour 1 sur tout les tshirts",true)

        commonService.createShippingRule(company, "fr", 0L, 500L, "-10")
        commonService.createShippingRule(company, "fr", 5001L, 1000L, "-100")
        commonService.createShippingRule(company, "fr", 10001L, 10000L, "-10%")
    }
}
