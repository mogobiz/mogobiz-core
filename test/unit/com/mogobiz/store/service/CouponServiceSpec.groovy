package com.mogobiz.store.service
import bootstrap.CommonService
import com.mogobiz.store.cmd.coupon.CouponCreateUpdateCommand
import com.mogobiz.service.SanitizeUrlService
import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.store.vo.CartItemVO
import com.mogobiz.store.vo.CartVO
import com.mogobiz.store.vo.CouponVO
import com.mogobiz.service.CouponService
import com.mogobiz.store.domain.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CouponService)
@Mock([Seller, Company, Catalog, Category, Coupon, ReductionRule, Product, TicketType, Stock, VariationValue])
class CouponServiceSpec extends Specification {

    CommonService commonService

    def setup(){
        commonService = new CommonService()
        commonService.sanitizeUrlService = new SanitizeUrlService()
        service.authenticationService = new AuthenticationService()
        Seller.metaClass.getSellerValidation = {new SellerValidation()}
        Company.metaClass.getCompanyValidation = {new CompanyValidation()}
        Catalog.metaClass.getCatalogValidation = {new CatalogValidation()}
        Category.metaClass.getCategoryValidation = {new CategoryValidation()}
        Coupon.metaClass.getCouponValidation = {new CouponValidation()}
        Product.metaClass.getProductValidation = {new ProductValidation()}
        TicketType.metaClass.getTicketTypeValidation = {new TicketTypeValidation()}
        ReductionRule.metaClass.getReductionRuleValidation = {new ReductionRuleValidation()}
        VariationValue.metaClass.getVariationValueValidation = {new VariationValueValidation()}
        Category.metaClass.getCategoryRender = {new CategoryRender()}
        ReductionRule.metaClass.getReductionRuleRender = {new ReductionRuleRender()}
        Category.metaClass.toString = {""}
        ReductionRule.metaClass.toString = {""}
    }

    /*

    def "CouponService should generate different codes" (){
        given:
        when:
            String code1 = service.generateCode();
            String code2 = service.generateCode();
        then:
            code1 != null
            code2 != null
            !code1.equals(code2)
    }
    def "CouponService should reject creation for invalid parameters"() {
        given:
            Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
            Seller seller = new Seller(login:"partner@iper2010.com", email:"partner@iper2010.com", password:"changeit", firstName: 'rector', lastName: 'Dir', active: true, company: company, admin:true).save()
            AuthenticationService.metaClass.retrieveAuthenticatedSeller = {seller}
            CouponCreateUpdateCommand cmd = new CouponCreateUpdateCommand()
        when:
            Coupon coupon = service.create(cmd);
        then:
            coupon != null
            coupon.hasErrors() == true
            1 == coupon.errors.allErrors.size()
            1 == coupon.errors.fieldErrors.size()
            "name" == coupon.errors.fieldErrors.get(0).field
    }

    def "CouponService should reject creation for invalid discount rule"() {
        given:
            Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
            Seller seller = new Seller(login:"partner@iper2010.com", email:"partner@iper2010.com", password:"changeit", firstName: 'rector', lastName: 'Dir', active: true, company: company, admin:true).save()
            AuthenticationService.metaClass.retrieveAuthenticatedSeller = {seller}
            Catalog catalog = new Catalog(
                    name:'CATALOGUE',
                    description: 'DESCRIPTION',
                    uuid: UUID.randomUUID().toString(),
                    activationDate:new Date(),
                    company:company,
                    social:false).save()
            Category category = new Category(
                    name : 'category',
                    description : 'description',
                    company : company,
                    catalog : catalog,
                    uuid : UUID.randomUUID().toString(),
                    sanitizedName : "category",
                    keywords : 'keywords',
                    hide : false,
                    position : 1).save()
            ReductionRule rule = new ReductionRule(xtype: ReductionRuleType.DISCOUNT)
            CouponCreateUpdateCommand cmd = new CouponCreateUpdateCommand(name: "coupon", code: "ABCDEF", categories: [category], rules: [rule])
        when:
            Coupon coupon = service.create(cmd);
        then:
            coupon != null
            coupon.hasErrors() == true
            1 == coupon.errors.allErrors.size()
            1 == coupon.errors.fieldErrors.size()
            "discount" == coupon.errors.fieldErrors.get(0).field
    }

    def "CouponService should reject creation for invalid y purshached rule"() {
        given:
            Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
            Seller seller = new Seller(login:"partner@iper2010.com", email:"partner@iper2010.com", password:"changeit", firstName: 'rector', lastName: 'Dir', active: true, company: company, admin:true).save()
            AuthenticationService.metaClass.retrieveAuthenticatedSeller = {seller}
            Catalog catalog = new Catalog(
                    name:'CATALOGUE',
                    description: 'DESCRIPTION',
                    uuid: UUID.randomUUID().toString(),
                    activationDate:new Date(),
                    company:company,
                    social:false).save()
            Category category = new Category(
                    name : 'category',
                    description : 'description',
                    company : company,
                    catalog : catalog,
                    uuid : UUID.randomUUID().toString(),
                    sanitizedName : "category",
                    keywords : 'keywords',
                    hide : false,
                    position : 1).save()
            ReductionRule rule = new ReductionRule(xtype: ReductionRuleType.X_PURCHASED_Y_OFFERED)
            CouponCreateUpdateCommand cmd = new CouponCreateUpdateCommand(name: "coupon", code: "ABCDEF", categories: [category], rules: [rule])
        when:
            Coupon coupon = service.create(cmd);
        then:
            coupon != null
            coupon.hasErrors() == true
            2 == coupon.errors.allErrors.size()
            2 == coupon.errors.fieldErrors.size()
            "xPurchased" == coupon.errors.fieldErrors.get(0).field
            "yOffered" == coupon.errors.fieldErrors.get(1).field
    }

    def "CouponService should create coupon for Category"() {
        given:
            Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
            Seller seller = new Seller(login:"partner@iper2010.com", email:"partner@iper2010.com", password:"changeit", firstName: 'rector', lastName: 'Dir', active: true, company: company, admin:true).save()
            AuthenticationService.metaClass.retrieveAuthenticatedSeller = {seller}
            Catalog catalog = new Catalog(
                    name:'CATALOGUE',
                    description: 'DESCRIPTION',
                    uuid: UUID.randomUUID().toString(),
                    activationDate:new Date(),
                    company:company,
                    social:false).save()
            Category category = new Category(
                    name : 'category',
                    description : 'description',
                    company : company,
                    catalog : catalog,
                    uuid : UUID.randomUUID().toString(),
                    sanitizedName : "category",
                    keywords : 'keywords',
                    hide : false,
                    position : 1).save()
            ReductionRule rule = new ReductionRule(xtype: ReductionRuleType.DISCOUNT, discount: "-1000")
            CouponCreateUpdateCommand cmd = new CouponCreateUpdateCommand(name: "coupon", code: "ABCDEF", categories: [category], rules: [rule])
        when:
            Coupon coupon = service.create(cmd);
        then:
            coupon != null
            coupon.hasErrors() == false
            coupon.id != null
            coupon.id > 0
            coupon.rules != null
            coupon.rules.size() == 1
    }

    def "CouponService should reject updating coupon"() {
        given:
            Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
            Seller seller = new Seller(login:"partner@iper2010.com", email:"partner@iper2010.com", password:"changeit", firstName: 'rector', lastName: 'Dir', active: true, company: company, admin:true).save()
            AuthenticationService.metaClass.retrieveAuthenticatedSeller = {seller}
            Catalog catalog = new Catalog(
                    name:'CATALOGUE',
                    description: 'DESCRIPTION',
                    uuid: UUID.randomUUID().toString(),
                    activationDate:new Date(),
                    company:company,
                    social:false).save()
            Category category = new Category(
                    name : 'category',
                    description : 'description',
                    company : company,
                    catalog : catalog,
                    uuid : UUID.randomUUID().toString(),
                    sanitizedName : "category",
                    keywords : 'keywords',
                    hide : false,
                    position : 1).save()
            ReductionRule rule = new ReductionRule(xtype: ReductionRuleType.DISCOUNT, discount: "-1000")
            CouponCreateUpdateCommand cmd = new CouponCreateUpdateCommand(name: "coupon", code: "ABCDEF", categories: [category], rules: [rule])
            Coupon coupon = service.create(cmd);
            rule = new ReductionRule(xtype: ReductionRuleType.DISCOUNT, discount: "-1000")
            cmd = new CouponCreateUpdateCommand(id: coupon.id, name: "coupon2", code: "ABCDEF", categories: [category], rules: [rule])
        when:
            Coupon updatedCoupon = service.update(cmd)
        then:
            updatedCoupon != null
            updatedCoupon.hasErrors() == false
            updatedCoupon.id == coupon.id
            updatedCoupon.name == "coupon2"
            updatedCoupon.rules != null
            updatedCoupon.rules.size() == 1
    }
    */

    def "CouponService should calculate reduction for cart"() {
        given:
            Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
            Seller seller = new Seller(login:"partner@iper2010.com", email:"partner@iper2010.com", password:"changeit", firstName: 'rector', lastName: 'Dir', active: true, company: company, admin:true).save()
            AuthenticationService.metaClass.retrieveAuthenticatedSeller = {seller}
            Catalog catalog = new Catalog(name:'CATALOGUE', description: 'DESCRIPTION', uuid: UUID.randomUUID().toString(), activationDate:new Date(), company:company, social:false).save()
            Category category = commonService.createCategory("Habillement", null, company, catalog, 1)
            category.categoryRender = new CategoryRender()
            Product produitPull = commonService.createProduct("Pull_Nike", "Pull Nike", 1000, ProductType.PRODUCT, ProductCalendar.NO_DATE, company, null, category, null, null);
            TicketType skuPull = commonService.createSKU("Blanc taille S", 1000, produitPull, "Blanc", "S", null, 10);
            ReductionRule rule = new ReductionRule(xtype: ReductionRuleType.DISCOUNT, discount: "10%")
            rule.reductionRuleRender = new ReductionRuleRender()
            CouponCreateUpdateCommand cmd = new CouponCreateUpdateCommand(name: "coupon", code: "ABCD", categories: [category], rules: [rule])
            Coupon coupon1 = service.create(cmd);
            rule = new ReductionRule(xtype: ReductionRuleType.X_PURCHASED_Y_OFFERED, xPurchased: 5, yOffered: 2)
            rule.reductionRuleRender = new ReductionRuleRender()
            cmd = new CouponCreateUpdateCommand(name: "coupon2", code: "1234", categories: [category], rules: [rule])
            Coupon coupon2 = service.create(cmd);
            CartVO cart = new CartVO(45000, 0, 45000, 0)
            def cartItems = []
            cartItems << new CartItemVO("1", produitPull.id, produitPull.name, produitPull.xtype, produitPull.calendarType, skuPull.id, skuPull.name, 10, 4500L, 4500L, 0L, 45000L, 45000L, null, null, null)
            cart.cartItemVOs = cartItems

        def myCriteria = [
                list : {Closure  cls -> [skuPull]}
        ]
        TicketType.metaClass.static.createCriteria = { myCriteria }
        when:
            CouponVO vo1 = service.calculateCoupon(coupon1.id, cart)
            CouponVO vo2 = service.calculateCoupon(coupon2.id, cart)
        then:
        vo1 != null
        vo1.price == 4500
        vo2 != null
        vo2.price == 18000
    }
}
