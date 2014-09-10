
/**
 * 
 */
package com.mogobiz.service

import bootstrap.CommonService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
@Mock([Company, BOCart, BOCartItem, BOProduct, Category, Product])
@TestFor(SaleService)
class SaleServiceSpec extends Specification {

    private static final String TEST_CUSTOMER_EMAIL = "test@test.com"
    private static final String TEST_CUSTOMER_NAME = "test"
    private static final String TEST_UNKNOWN_CUSTOMER_EMAIL = "unknown@test.com"
    private static final String TEST_UNKNOWN_CUSTOMER_NAME = "unknown"

    CommonService commonService;

    def setup(){
        commonService = new CommonService();
        Company.metaClass.getCompanyValidation = {new CompanyValidation()}
        BOCart.metaClass.getBOCartValidation = {new BOCartValidation()}
        BOCartItem.metaClass.getBOCartItemValidation = {new BOCartItemValidation()}
        BOProduct.metaClass.getBOProductValidation = {new BOProductValidation()}
        BOProduct.metaClass.getBOProductRender = {new BOProductRender()}
        Category.metaClass.getCategoryValidation = {new CategoryValidation()}
        Product.metaClass.getProductValidation = {new ProductValidation()}
        SaleService.metaClass.searchTransactionUuidByCustomer = { String email, String name ->
            if (TEST_CUSTOMER_EMAIL.equals(email) || TEST_CUSTOMER_NAME.equals(name)) {
                ["1", "2"]
            }
            else {
                return []
            }
        }
    }

    void "test"() {
        given:
        Company company = new Company(code:'TEST1', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        Category cat =  new Category(name:"CAT", company : company, uuid:UUID.randomUUID().toString()).save()
        Product product1 = createProduct("P1", "P1", cat, company);
        BOProduct boProduct = new BOProduct(principal : true, product : product1, price : 10000, BOProductRender: new BOProductRender()).save()
        BOCart cart = new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        new BOCartItem(code : "SALE_1", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart, bOProducts: [boProduct]).save();

        when:
        def searchList = service.searchBOCartByProduct(company, product1, 0);

        then:
        1 == searchList.size();
    }

/*
    void "searchBOCartByCustomer when exists BOCart for email with multiple companies"() {
        given:
        Company company1 = new Company(code:'TEST1', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        Company company2 = new Company(code:'TEST2', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company1).save()
        new BOCart(transactionUuid : "2", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company2).save()
        when:
        List<BOCart> searchList = service.searchBOCartByCustomer(company1, TEST_CUSTOMER_EMAIL, null, null, 0)
        then:
        1 == searchList.size();
        1 == searchList.totalCount;
    }

    void "searchBOCartByCustomer when not exist BOCart for email"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        when:
        List<BOCart> searchList = service.searchBOCartByCustomer(company, TEST_UNKNOWN_CUSTOMER_EMAIL, null, null, 0)
        then:
        0 == searchList.size();
    }

    void "searchBOCartByCustomer when exists BOCart for name"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        when:
        List<BOCart> searchList = service.searchBOCartByCustomer(company, null, TEST_CUSTOMER_NAME, null, 0)
        then:
        1 == searchList.size();
    }

    void "searchBOCartByCustomer when not exist BOCart for name"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        when:
        List<BOCart> searchList = service.searchBOCartByCustomer(company, null, TEST_UNKNOWN_CUSTOMER_NAME, null, 0)
        then:
        0 == searchList.size();
    }

    void "searchBOCartByCustomer when exists BOCart for date"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        when:
        List<BOCart> searchList = service.searchBOCartByCustomer(company, null, null, Calendar.getInstance(), 0)
        then:
        1 == searchList.size();
    }

    void "searchBOCartByCustomer when not exist BOCart for date"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        Calendar searchDate = Calendar.getInstance();
        searchDate.add(Calendar.DAY_OF_MONTH, -1);
        when:
        List<BOCart> searchList = service.searchBOCartByCustomer(company, null, null, searchDate, 0)
        then:
        0 == searchList.size();
    }

    void "searchBOCartItemByCode when exist code"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        BOCart cart = new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        new BOCartItem(code : "SALE_1", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart).save();
        new BOCartItem(code : "SALE_2", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart).save();
        when:
        List<BOCartItem> searchList = service.searchBOCartItemByCode(cart, "sale")
        then:
        2 == searchList.size();
    }

    void "searchBOCartItemByCode when exist exactly code"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        BOCart cart = new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        new BOCartItem(code : "SALE_1", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart).save();
        new BOCartItem(code : "SALE_2", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart).save();
        when:
        List<BOCartItem> searchList = service.searchBOCartItemByCode(cart, "SALE_1")
        then:
        1 == searchList.size();
    }

    void "searchBOCartItemByCode when not exist code"() {
        given:
        Company company = new Company(code:'TEST', name:'TEST', aesPassword: 'PASSWORD', onlineValidation: false).save()
        BOCart cart = new BOCart(transactionUuid : "1", date : Calendar.getInstance(), price : 10000, status : TransactionStatus.PENDING, currencyCode : "EUR", currencyRate : 0.01, company: company).save()
        new BOCartItem(code : "SALE_1", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart).save();
        new BOCartItem(code : "SALE_2", price: 10000, tax: 0, endPrice: 10000, totalPrice: 10000, totalEndPrice: 10000, quantity : 1, bOCart : cart).save();
        when:
        List<BOCartItem> searchList = service.searchBOCartItemByCode(cart, "UNKWOWN")
        then:
        0 == searchList.size();
    }
    */

    def Product createProduct(final String code, final String name, Category category, Company company){
        Product product = new Product(
                code:code,
                name:name,
                xtype:ProductType.PRODUCT,
                price:10000,
                nbSales:0,
                hide:false,
                sanitizedName:name,
                category:category,
                company:company
        )
        product.save()
        return product
    }
}
