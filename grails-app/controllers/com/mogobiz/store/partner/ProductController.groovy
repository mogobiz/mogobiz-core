package com.mogobiz.store.partner

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.constant.IperConstant
import com.mogobiz.json.RenderUtil
import com.mogobiz.service.ProductService
import com.mogobiz.store.domain.*
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.Page
import com.mogobiz.utils.PermissionType
import static com.mogobiz.utils.ProfileUtils.*
import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

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
    AuthenticationService authenticationService
    ProductService productService

    SimpleDateFormat sdf = new SimpleDateFormat(IperConstant.DATE_FORMAT)
    SimpleDateFormat sdfWithoutHour = new SimpleDateFormat(IperConstant.DATE_FORMAT_WITHOUT_HOUR)

    @Transactional(readOnly = true)
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


    @Transactional(readOnly = true)
    def list() {
        List<Product> ps = productService.list()
        ps.each { Product p ->
            System.out.println(p.name + "=" + p.company.name)
        }
    }

    @Transactional(readOnly = true)
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
            Long catalogId = params['catalog']?.id?.toLong()
            Catalog catalogue = catalogId == null ? null : Catalog.get(catalogId)
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
                eq('deleted', false)
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
                if (catalogue != null) {
                    category { eq("catalog", catalogue)}
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

    @Transactional
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

    @Transactional
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

    @Transactional(readOnly = true)
    def getTagsByCompany() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        def company = seller.company
        def tags = Tag.findAllByCompany(company)

        String tagsString = '';
        if (tags) {
            def names = []
            tags.each { names << it.name }
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

    @Transactional(readOnly = true)
    def getTagsByProduct() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        Product product = params['product']?.id ? Product.get(params['product']?.id) : null
        if (product) {
            def res = new ArrayList()
            product.tags.each { res.add(it.asMapForJSON()) }
            withFormat {
                html tags: res
                xml { render res as XML }
                json { render res as JSON }
            }
        } else {
            response.sendError 404
        }
    }

    /**add product tags
     *
     */
    @Transactional
    def addTag() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        String tagName = params['tag']?.name

        if (tagName != null && tagName.length() > 0) {
            Product product = params['product']?.id ? Product.findById(params['product']?.id, [fetch: [tags: 'join']]) : null
            if (product) {
                def tag = product.tags.find { tag ->
                    tag.name.toLowerCase() == tagName.toLowerCase()
                }

                if (!tag) {
                    tag = Tag.findByCompanyAndNameIlike(product.company, tagName)
                    if (!tag)
                    tag= new Tag(name: tagName, company: seller.company)
                    tag.save()
                    product.addToTags(tag)
                    product.save(flush:true)
                    withFormat {
                        html tags: product.tags
                        xml { render product.tags as XML }
                        json { render product.tags as JSON }
                    }
                }
            } else {
                response.sendError 404
            }
        } else {
            response.sendError 404
        }
    }

    /**add product tags
     *
     */
    @Transactional
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
                product.save(flush: true)
                List<Product> productsWithTag = Product.executeQuery("select p from Product p left join p.tags as t where t.id=:idTag and t.company.id = :idCompany", [idTag: tag.id, idCompany: tag.company.id])
                if (productsWithTag == null || productsWithTag.size() == 0) {
                    tag.delete();
                }
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
        if (product && product.company == seller.company) {
            ProductProperty property = ProductProperty.findByProductAndName(product, name)
            if (!property) {
                property = new ProductProperty(product: product, name: name, value: value)
            } else {
                property.value = value
            }
            property.validate()
            if (!property.hasErrors()) {
                property = property.save(flush: true)
            }
            render ajaxResponseService.prepareResponse(property, property?.asMapForJSON()).asMap() as JSON
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def deleteProperty(Long id) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        ProductProperty productProperty = ProductProperty.get(id)
        if (productProperty && productProperty.product.company == seller.company) {
            productProperty.delete(flush: true)
            render([success: true] as Map) as JSON
            return
        } else {
            response.sendError 404
        }
    }

    @Transactional
    def markDeleted(long id) {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }
        Product product = Product.get(id)
        if (product && product.company == seller.company) {
            product.setDeleted(true)
            product.save(flush: true)
            render([success: true] as Map) as JSON
            return
        } else {
            response.sendError 404
        }
    }

    def search(Long idCatalog, String query, boolean activeOnly){
        if(!idCatalog || !query){
            response.sendError(400)
            return
        }
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (seller == null) {
            response.sendError 401
            return
        }

        def permissions = UserPermission.createCriteria().list {
            'in'('user', seller)
        }.collect {computeShiroPermission(it.target)}

        def params = [idCatalog:idCatalog, query: "%$query%"]
        def sql = "SELECT p FROM Product p left join p.ticketTypes as sku left join p.tags as tag " +
                "WHERE p.category.catalog.id=:idCatalog " +
                "AND (lower(tag.name) like lower(:query) OR lower(p.name) like lower(:query) " +
                "OR lower(p.description) like lower(:query) OR lower(sku.name) like lower(:query) " +
                "OR lower(sku.description) like lower(:query))"

        if(activeOnly){
            params << [productState: ProductState.ACTIVE]
            sql += " AND p.state = :productState AND p.deleted = false"
        }

        def products = Product.executeQuery(
                sql,
                params
        ).unique(false).collect {product ->
            final requiredPermission = computeShiroPermission(
                    PermissionType.UPDATE_STORE_CATEGORY_WITHIN_CATALOG,
                    "${product.company.id}",
                    "$idCatalog",
                    "${product.category.id}"
            )
            def authorized = permissions.find {it.implies(requiredPermission)}
            if(authorized){
                product.asMapForJSON([
                        'id',
                        'name',
                        'description',
                        'category',
                        'category.id',
                        'category.name',
                        'lastUpdated',
                        'state',
                        'picture',
                        'brand',
                        'code',
                        'xtype',
                        'price',
                        'dateCreated',
                        'descriptionAsText'
                ] as List<String>)
            }
            else{
                log.warn("user not granted ${requiredPermission.toString()}")
                []
            }
        }.flatten()
        withFormat {
            html products: products
            xml { render products as XML }
            json { render products as JSON }
        }
    }
}
