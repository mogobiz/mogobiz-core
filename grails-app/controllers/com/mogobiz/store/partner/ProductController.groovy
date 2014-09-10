package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.constant.IperConstant
import com.mogobiz.json.RenderUtil
import com.mogobiz.service.ProductService
import com.mogobiz.store.domain.*
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.Page
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.mail.MailService

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

import static com.mogobiz.constant.IperConstant.QUEUE_NS
import static com.mogobiz.constant.IperConstant.QUEUE_SOCIAL

/**
 * Controller utilisé pour gérer les produits
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class ProductController {
    AjaxResponseService ajaxResponseService
    MailService mailService
    AuthenticationService authenticationService
    ProductService productService

    SimpleDateFormat sdf = new SimpleDateFormat(IperConstant.DATE_FORMAT)
    SimpleDateFormat sdfWithoutHour = new SimpleDateFormat(IperConstant.DATE_FORMAT_WITHOUT_HOUR)

    def existCode() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def company = seller.company
        Product foundProduct = Product.findByCodeAndCompany(params['product.code'], company);
        if (foundProduct && params['product.id'] != foundProduct.id) {
            render RenderUtil.primitifAsMapForJSON("result", true) as JSON
        }
        render RenderUtil.primitifAsMapForJSON("result", false) as JSON
    }


    def list() {
        List<Product> ps = productService.list()
        ps.each { Product p ->
            System.out.println(p.name + "=" + p.company.name)
        }
    }

    def show() {

        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def company = seller.company
        def id = params.id?.toLong()
        def uuid = params.uuid
        if (id != null || uuid != null) {
            def product = id != null ? Product.get(id) : Product.findByUuid(uuid)
            if (product && product.company == company) {
                def productVO = product.asMapForJSON()
                def ticketList = TicketType.findAllByProduct(product)
                def ticketTypesVO = []
                ticketList.each { ticketTypesVO << it.asMapForJSON() }
                productVO.put("ticketTypes", ticketTypesVO)
                /*
                 def properties = Feature.findAllByProduct(product)
                 def propertiesVO = []
                 properties.each{ propertiesVO << it.asMapForJSON() }
                 productVO.put("properties", propertiesVO)
                 */
                withFormat {
                    html productVO: productVO
                    xml { render productVO as XML }
                    json { render productVO as JSON }
                }
            } else {
                response.sendError 404
            }
        } else {
            // filter les produits suivants les params
            Long idCategory = params['product']?.idCategorie?.toLong()

            def products = Product.createCriteria()
            int offset;
            int pageSize = IperConstant.NUMBER_PRODUCT_PER_PAGE;
            if (params?.pageSize) {
                pageSize = params.pageSize.toInteger();
            }
            if (params?.pageOffset) {
                offset = params.pageOffset.toInteger() * pageSize;
            } else {
                offset = 0;
            }
            def productslist = products.list(max: pageSize,
                    offset: offset) {
                eq('company', company)
                if (params['product']?.state) {
                    eq('state', ProductState.valueOf(params['product']?.state))
                }
                if (params['product']?.xtype) {
                    eq('xtype', ProductType.valueOf(params['product']?.xtype))
                }
                if (params['name']) {
                    ilike('name', '%' + params['name'] + '%')
                }
                if (idCategory != null) {
                    category { eq('id', idCategory) }
                }
                order(params.orderBy ? params.orderBy : "name", params.orderDirection ? params.orderDirection : "asc")
            }

            // construct the products VO list to send to client
            def prodList = new Page()
            def resultliste = new ArrayList()
            productslist.each { prod ->
                def productVO = prod.asMapForJSON()
                resultliste.add(productVO)
            }

            prodList = IperUtil.createListePagine(resultliste, productslist.totalCount, pageSize, params?.pageOffset ? params.pageOffset.toInteger() : 0)
            withFormat {
                html prodList: prodList
                xml { render prodList as XML }
                json { render prodList as JSON }
            }
        }
    }

    def save() {

        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }

        Product product = new Product()
        product.company = seller.company
        product.state = ProductState.ACTIVE
        saveProduct(product, params, seller, EventType.CREATE)
    }

    def update() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def company = seller.company
        def id = params['product']?.id
        def product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (product && product.company == company) {
            saveProduct(product, params, seller, EventType.MODIFY)
        } else {
            response.sendError 404
        }
        if (params.social) {
            def map = [:]
            map.put('userId', seller.id)
            map.put('action', "publishProduct")
            map.put("productId", id)
            event(namespace: QUEUE_NS, topic: QUEUE_SOCIAL, data: map)
        }
    }

    /**get product tags
     *
     */

    def getTagsByCompany() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def company = seller.company
        def tags = Tag.executeQuery("select p.tags from Product p where p.company.id = " + company.id + ')')
        String tagsString = '';
        if (tags) {
            def names = []
            tags.each { names << it.name }
            names = names.unique();
            names.each { name ->
                if (tagsString.length() > 0)
                    tagsString += ',:,;';
                tagsString += name
            }
            withFormat { json { render tagsString } }
        } else {
            withFormat { json { response.sendError 200 } }
        }
    }

    def getTagsByProduct() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        Product product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (product) {
            withFormat {
                html tags: product.tags
                xml { render product.tags as XML }
                json { render product.tags as JSON }
            }
        } else {
            response.sendError 404
        }
    }

    /**add product tags
     *
     */
    def addTag() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def tagName = params['tag']?.name
        Product product = params['product']?.id ? Product.findById(params['product']?.id, [fetch: [tags: 'join']]) : null
        if (product) {
            def tag = product.tags.find { tag ->
                tag.name == tagName
            }
            if (!tag) {
                def newTag = new Tag(name: tagName)
                newTag.save()
                product.addToTags(newTag)
                withFormat {
                    html tags: product.tags
                    xml { render product.tags as XML }
                    json { render product.tags as JSON }
                }
            }
        } else {
            response.sendError 404
        }
    }

    /**add product tags
     *
     */
    def removeTag() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def tagName = params['tag']?.name
        Product product = params['product']?.id ? Product.findById(params['product']?.id, [fetch: [tags: 'join']]) : null
        if (product) {
            def tag = product.tags.find { tag ->
                tag.name == tagName
            }
            if (tag) {
                product.removeFromTags(tag)
                product.save()

            }
            withFormat {
                html tags: product.tags
                xml { render product.tags as XML }
                json { render product.tags as JSON }
            }
        } else {
            response.sendError 404
        }
    }

    /**
     * function to convert date string to calendar
     * @param str_date
     * @return
     */
    private Calendar convertStringToCalendar(String str_date) throws ParseException {
        try {
            DateFormat formatter
            Date date
            formatter = new SimpleDateFormat(IperConstant.DATE_FORMAT_WITHOUT_HOUR)
            date = (Date) formatter.parse(str_date)
            Calendar cal = Calendar.getInstance()
            cal.setTime(date)
            return cal
        } catch (Exception e) {
            log.error("error while parsing date " + str_date, e)
            throw e
        }
    }

    /**
     * create or update Product
     * @param params the request params
     * @param product the product to save
     * @param eventType the event type (creation or modification)
     */
    private saveProduct(Product product, Map params, Seller seller, EventType eventType) {
        Product result = productService.saveProduct(product, params, seller, eventType);
        render ajaxResponseService.prepareResponse(product, result?.asMapForJSON()).asMap() as JSON
    }

    def saveProperty(Long product_id, String name, String value) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        Product product = Product.get(product_id)
        if(product &&  product.company == seller.company){
            ProductProperty property = ProductProperty.findByProductAndName(product, name)
            if(!property){
                property = new ProductProperty(product: product, name: name, value: value)
            }
            else{
                property.value = value
            }
            property.validate()
            if(!property.hasErrors()){
                property = property.save(flush:true)
            }
            render ajaxResponseService.prepareResponse(property, property?.asMapForJSON()).asMap() as JSON
        } else {
            response.sendError 404
        }
    }

    def deleteProperty(Long id) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        ProductProperty productProperty = ProductProperty.get(id)
        if (productProperty && productProperty.product.company == seller.company) {
            productProperty.delete(flush: true)
            render ([success:true] as Map) as JSON
            return
        } else {
            response.sendError 404
        }
    }

}
