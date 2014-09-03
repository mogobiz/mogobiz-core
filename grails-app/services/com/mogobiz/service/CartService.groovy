package com.mogobiz.service

import com.mogobiz.store.domain.*
import com.mogobiz.store.exception.CurrencyRateException
import com.mogobiz.store.exception.InsufficientStockException
import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.constant.IperConstant
import com.mogobiz.geolocation.domain.Poi
import com.mogobiz.json.RenderUtil
import com.mogobiz.store.vo.CartItemVO
import com.mogobiz.store.vo.CartVO
import com.mogobiz.store.vo.CouponVO
import com.mogobiz.store.vo.RegisteredCartItemVO
import com.mogobiz.store.vo.ShippingVO
import com.mogobiz.utils.*
import com.sun.org.apache.xml.internal.security.utils.Base64
import grails.plugin.mail.MailService
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Service en charge du panier. Il offre notamment les méthodes d'ajout, de modification
 * et de suppression des items du panier et la méthode de finalisation du panier.
 */
class CartService implements IperConstant {

    UuidDataService uuidDataService;
    ProductService productService
    AjaxResponseService ajaxResponseService
    TaxRateService taxRateService
    RateService rateService;
    MailService mailService
    CouponService couponService

    private void updateCoupons(CartVO cart) {
        cart.reduction = 0
        cart.coupons?.each { CouponVO c ->
            couponService.updateCoupon(c, cart)
            cart.reduction += c.price
        }
        if (cart.endPrice != null) {
            cart.finalPrice = cart.endPrice - cart.reduction
        } else {
            cart.finalPrice = cart.price - cart.reduction
        }
    }

    /**
     * Renders the cart by formating price
     * @param locale
     * @param currencyCode
     * @param cart
     * @return
     * @throws CurrencyRateException
     */
    public Map renderCart(Locale locale, String currencyCode, CartVO cart) throws CurrencyRateException {
        Map map = [:]
        map["count"] = cart.count

        List<Map> listMapItem = []
        cart.cartItemVOs?.each { CartItemVO cartItem ->
            listMapItem << renderCartItem(locale, currencyCode, cartItem)
        }
        map["cartItemVOs"] = listMapItem

        updateCoupons(cart)
        List<Map> listCoupon = []
        cart.coupons?.each { CouponVO c ->
            listCoupon << renderCoupon(locale, currencyCode, c)
        }
        map["coupons"] = listCoupon

        map << renderPriceCart(locale, currencyCode, cart)
        return map
    }

    public Map renderCoupon(Locale locale, String currencyCode, CouponVO coupon) throws CurrencyRateException {
        String[] included = [
                "id",
                "name",
                "code",
                "active",
                "startDate",
                "endDate"
        ]

        Map mapItem = RenderUtil.asMapForJSON(null, included, null, coupon)
        mapItem << renderPriceCoupon(locale, currencyCode, coupon);
        return mapItem
    }

    public Map renderPriceCoupon(Locale locale, String currencyCode, CouponVO coupon) throws CurrencyRateException {
        Map price = [:]
        price["price"] = coupon.price
        price["formatedPrice"] = rateService.format(coupon.price, currencyCode, locale);
        return price
    }

    public Map renderPriceCart(Locale locale, String currencyCode, CartVO cart) throws CurrencyRateException {
        Map price = [:]
        price["price"] = cart.price
        price["endPrice"] = cart.endPrice
        price["reduction"] = cart.reduction
        price["finalPrice"] = cart.finalPrice
        price["formatedPrice"] = rateService.format(cart.price, currencyCode, locale);
        price["formatedEndPrice"] = rateService.format(cart.endPrice, currencyCode, locale);
        price["formatedReduction"] = rateService.format(cart.reduction, currencyCode, locale);
        price["formatedFinalPrice"] = rateService.format(cart.finalPrice, currencyCode, locale);
        return price
    }

    public Map renderCartItem(Locale locale, String currencyCode, CartItemVO cartItem) throws CurrencyRateException {
        String[] included = [
                "id", "productId", "xtype", "skuId", "skuName", "quantity", "tax", "startDate", "endDate",
                "shipping",
                "shipping.weight",
                "shipping.weightUnit",
                "shipping.width",
                "shipping.height",
                "shipping.depth",
                "shipping.linearUnit",
                "shipping.amount",
                "shipping.free",
                "registeredCartItemVOs",
                "registeredCartItemVOs.cartItemId",
                "registeredCartItemVOs.id",
                "registeredCartItemVOs.email",
                "registeredCartItemVOs.firstname",
                "registeredCartItemVOs.lastname",
                "registeredCartItemVOs.phone",
                "registeredCartItemVOs.birthdate"
        ]

        Map mapItem = RenderUtil.asMapForJSON(null, included, null, cartItem)
        mapItem << renderPriceCartItem(locale, currencyCode, cartItem);
        return mapItem
    }

    public Map renderPriceCartItem(Locale locale, String currencyCode, CartItemVO cartItem) throws CurrencyRateException {
        Map price = [:]

        price["price"] = cartItem.price
        price["endPrice"] = cartItem.endPrice
        price["totalPrice"] = cartItem.totalPrice
        price["totalEndPrice"] = cartItem.totalEndPrice

        price["formatedPrice"] = rateService.format(cartItem.price, currencyCode, locale);
        price["formatedEndPrice"] = rateService.format(cartItem.endPrice, currencyCode, locale);
        price["formatedTotalPrice"] = rateService.format(cartItem.totalPrice, currencyCode, locale);
        price["formatedTotalEndPrice"] = rateService.format(cartItem.totalEndPrice, currencyCode, locale);

        // Traduction aussi du nom en traduisant le produit et le sku
        price["productName"] = translateName(cartItem.productId, locale.language, cartItem.productName)
        price["skuName"] = translateName(cartItem.skuId, locale.language, cartItem.skuName)

        return price
    }

    private String translateName(long target, String lang, String defaultValue) {
        if (lang) {
            Translation translation = Translation.createCriteria().get {
                eq("lang", lang)
                eq("target", target)
            }

            if (translation) {
                Map map = new JsonSlurper().parseText(translation.value)
                if (map != null && map["name"] != null) {
                    return map["name"]
                }
            }
        }
        return defaultValue;
    }

    public CartItemVO retrieveCartItem(CartVO cart, String id) {
        return cart.cartItemVOs.find { CartItemVO item -> item.id.equals(id) }
    }

    /**
     * Récupère le contenu du panier correspond au cookie tracking.
     * S'il n'y a aucun panier correspondant, un panier est créé
     * @return
     */
    CartVO initCart() {
        CartVO cartVO = uuidDataService.getCart();
        if (cartVO == null) {
            // si le data est vide, on créé un nouveau panier
            cartVO = new CartVO();
            cartVO.endPrice = 0;
            uuidDataService.setCart(cartVO);
        }
        return cartVO;
    }

    /**
     * Add a item to the cart
     * @param locale
     * @param currencyCode
     * @param cartVO
     * @param ticketType
     * @param quantity
     * @param dateTime
     * @param registeredCartItems
     * @return
     * @throws CurrencyRateException
     */
    AjaxResponse addItem(Locale locale, String currencyCode, CartVO cartVO, TicketType ticketType, int quantity, Calendar dateTime, List<RegisteredCartItemVO> registeredCartItems) throws CurrencyRateException {
        AjaxResponse result = new AjaxResponse()
        Product product = ticketType.product;
        Calendar[] startEndDate = IperUtil.verifyAndExtractStartEndDate(ticketType, dateTime);

        // Vérification des paramètres
        if (cartVO.uuid) {
            // un paiement a été initialisé, on ne peut plus modifier le contenu du panier avant la fin du paiement (ou l'abandon)
            result = ajaxResponseService.addError(result, "cart", "initiate.payment.error", null, locale)
        }
        if (ticketType.minOrder > quantity || (ticketType.maxOrder < quantity && ticketType.maxOrder > -1)) {
            result = ajaxResponseService.addError(result, "quantity", "min.max.error", [ticketType.minOrder, ticketType.maxOrder] as Object[], locale)
        }
        if (dateTime == null && !ProductCalendar.NO_DATE.equals(product.calendarType)) {
            result = ajaxResponseService.addError(result, "dateTime", "nullable.error", null, locale)
        } else if (dateTime != null && startEndDate == null) {
            result = ajaxResponseService.addError(result, "dateTime", "unsaleable.error", null, locale)
        }
        if (product.xtype == ProductType.SERVICE) {
            if (registeredCartItems == null || registeredCartItems.size() != quantity) {
                result = ajaxResponseService.addError(result, "registeredCartItems", "size.error", null, locale)
            } else {
                for (RegisteredCartItemVO item : registeredCartItems) {
                    if (item.email == null || "".equals(item.email.trim())) {
                        result = ajaxResponseService.addError(result, "registeredCartItems", "email.error", null, locale)
                        break;
                    }
                }
            }
        }

        if (result.success) {
            // On décrémente le stock
            try {
                productService.decrement(ticketType, quantity, startEndDate[0])
            }
            catch (InsufficientStockException ex) {
                result = ajaxResponseService.addError(result, "quantity", "stock.error", null, locale)
            }
        }

        if (result.success) {
            // reprise des items existants
            def cartItemVOs = []
            long oldCartPrice = 0;
            Long oldCartEndPrice = 0;
            cartVO.cartItemVOs.each { CartItemVO item ->
                cartItemVOs << item;
                oldCartPrice += item.totalPrice
                if (oldCartEndPrice != null && item.totalEndPrice != null) {
                    oldCartEndPrice += item.totalEndPrice
                } else {
                    oldCartEndPrice = null;
                }
            }

            // ajout du nouvel item
            CartItemVO newItem = new CartItemVO();
            newItem.id = new Date().time
            newItem.productId = product.id;
            newItem.productName = product.name;
            newItem.xtype = product.xtype;
            newItem.calendarType = product.calendarType;
            newItem.skuId = ticketType.id;
            newItem.skuName = ticketType.name;
            newItem.quantity = quantity;
            newItem.price = ticketType.price;
            newItem.tax = taxRateService.findTaxRateByProduct(product, locale?.country)
            newItem.endPrice = taxRateService.calculateEndPrix(ticketType.price, newItem.tax)
            newItem.totalPrice = quantity * newItem.price;
            if (newItem.endPrice != null) {
                newItem.totalEndPrice = quantity * newItem.endPrice;
            }
            newItem.startDate = startEndDate[0];
            newItem.endDate = startEndDate[1];
            newItem.registeredCartItemVOs = registeredCartItems.toArray(new RegisteredCartItemVO[0]);
            newItem.registeredCartItemVOs?.each { RegisteredCartItemVO rcItem ->
                rcItem.cartItemId = newItem.id;
            }

            // Shipping
            if (product.shipping != null) {
                Shipping shipping = product.shipping
                ShippingVO shippingVO = new ShippingVO();
                shippingVO.weight = shipping.weight
                shippingVO.weightUnit = shipping.weightUnit
                shippingVO.width = shipping.width
                shippingVO.height = shipping.height
                shippingVO.depth = shipping.depth
                shippingVO.linearUnit = shipping.linearUnit
                shippingVO.amount = shipping.amount
                shippingVO.free = shipping.free
                newItem.shipping = shippingVO
            }

            cartItemVOs << newItem

            cartVO.price = oldCartPrice + newItem.totalPrice;
            if (newItem.totalEndPrice != null && oldCartEndPrice != null) {
                cartVO.endPrice = oldCartEndPrice + newItem.totalEndPrice;
            } else {
                cartVO.endPrice = null;
            }
            cartVO.count = cartItemVOs.size();
            cartVO.cartItemVOs = cartItemVOs
            uuidDataService.setCart(cartVO);

        }

        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    /**
     * Update the quantity of the cartItem if it is not corresponding to a SERVICE product
     * @param locale
     * @param currencyCode
     * @param cartVO
     * @param cartItemId
     * @param quantity
     * @return
     */
    AjaxResponse updateItem(Locale locale, String currencyCode, CartVO cartVO, String cartItemId, int quantity) throws CurrencyRateException {
        AjaxResponse result = new AjaxResponse()

        if (cartVO.uuid) {
            // un paiement a été initialisé, on ne peut plus modifier le contenu du panier avant la fin du paiement (ou l'abandon)
            result = ajaxResponseService.addError(result, "cart", "initiate.payment.error", null, locale)
        }
        if (result.success) {
            CartItemVO cartItem = cartVO.cartItemVOs.find { CartItemVO item -> item.id.equals(cartItemId) }
            if (cartItem && !ProductType.SERVICE.equals(cartItem.xtype) && cartItem.quantity != quantity) {
                Product produit = Product.get(cartItem.getProductId())
                TicketType sku = TicketType.get(cartItem.getSkuId())

                if (sku.minOrder > quantity || (sku.maxOrder < quantity && sku.maxOrder > -1)) {
                    result = ajaxResponseService.addError(result, "quantity", "min.max.error", [sku.minOrder, sku.maxOrder] as Object[], locale)
                }

                int oldQuantity = cartItem.quantity;
                long oldTotalPrice = cartItem.totalPrice
                Long oldTotalEndPrice = cartItem.totalEndPrice
                if (result.success) {
                    if (oldQuantity < quantity) {
                        try {
                            // On décrémente le stock
                            productService.decrement(sku, quantity - oldQuantity, cartItem.startDate)
                        }
                        catch (InsufficientStockException ex) {
                            result = ajaxResponseService.addError(result, "quantity", "stock.error", null, locale)
                        }
                    } else {
                        // On incrémente le stock
                        productService.increment(sku, oldQuantity - quantity, cartItem.startDate)
                    }
                }
                if (result.success) {
                    cartItem.quantity = quantity
                    cartItem.totalPrice = quantity * cartItem.price;
                    cartVO.price = cartVO.price - oldTotalPrice + cartItem.totalPrice;
                    if (oldTotalEndPrice != null && cartVO.endPrice != null && cartItem.endPrice != null) {
                        cartItem.totalEndPrice = quantity * cartItem.endPrice;
                        cartVO.endPrice = cartVO.endPrice - oldTotalEndPrice + cartItem.totalEndPrice;
                    }
                    uuidDataService.setCart(cartVO);
                }
            }
        }
        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    /**
     * Add a coupon to the cart
     * @param companyId
     * @param locale
     * @param currencyCode
     * @param cartVO
     * @param couponCode
     * @return
     */
    AjaxResponse addCoupon(long companyId, Locale locale, String currencyCode, CartVO cartVO, String couponCode) {
        AjaxResponse result = new AjaxResponse()

        Coupon coupon = couponService.findByCode(companyId, couponCode)

        if (!coupon) {
            result = ajaxResponseService.addError(result, "coupon", "unknown.error", null, locale)
        } else {
            CouponVO couponVO = cartVO.coupons?.find { CouponVO c -> couponCode.equals(c.code) }
            if (couponVO) {
                result = ajaxResponseService.addError(result, "coupon", "already.exist", null, locale)
            } else if (!couponService.consumeCoupon(coupon)) {
                result = ajaxResponseService.addError(result, "coupon", "stock.error", null, locale)
            } else {
                couponVO = new CouponVO()
                couponVO.id = coupon.id
                couponVO.code = coupon.code

                // reprise des items existants
                def coupons = []
                cartVO.coupons.each { CouponVO c ->
                    coupons << c;
                }
                coupons << couponVO
                cartVO.coupons = coupons
                uuidDataService.setCart(cartVO);
            }
        }
        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    /**
     * Remove the coupon from the cart
     * @param companyId
     * @param locale
     * @param currencyCode
     * @param cartVO
     * @param couponCode
     * @return
     */
    AjaxResponse removeCoupon(long companyId, Locale locale, String currencyCode, CartVO cartVO, String couponCode) {
        AjaxResponse result = new AjaxResponse()

        Coupon coupon = couponService.findByCode(companyId, couponCode)

        CouponVO couponVO = cartVO.coupons?.find { CouponVO c -> couponCode.equals(c.code) }

        if (!coupon || !couponVO) {
            result = ajaxResponseService.addError(result, "coupon", "unknown.error", null, locale)
        } else {
            couponService.releaseCoupon(coupon)

            // reprise des items existants sauf celui à supprimer
            def coupons = []
            cartVO.coupons.each { CouponVO c ->
                if (!couponCode.equals(c.code)) {
                    coupons << c;
                }
            }
            cartVO.coupons = coupons
            uuidDataService.setCart(cartVO);
        }
        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    /**
     * Supprime un élément du panier en fonction de l'id de l'élément du panier
     * @param cartVO
     * @param cartItemId
     */
    AjaxResponse removeItem(Locale locale, String currencyCode, CartVO cartVO, String cartItemId) throws CurrencyRateException {
        AjaxResponse result = new AjaxResponse()

        if (cartVO.uuid) {
            // un paiement a été initialisé, on ne peut plus modifier le contenu du panier avant la fin du paiement (ou l'abandon)
            result = ajaxResponseService.addError(result, "cart", "initiate.payment.error", null, locale)
        }
        if (result.success) {
            CartItemVO cartItem = cartVO.cartItemVOs.find { CartItemVO item -> item.id.equals(cartItemId) }
            if (cartItem) {
                TicketType sku = TicketType.get(cartItem.getSkuId())

                productService.increment(sku, cartItem.quantity, cartItem.startDate)
                cartVO.cartItemVOs -= cartItem
                cartVO.price -= cartItem.totalPrice;
                if (cartVO.endPrice != null && cartItem.totalEndPrice != null) {
                    cartVO.endPrice -= cartItem.totalEndPrice;
                }
                cartVO.count -= 1;
            }
            uuidDataService.setCart(cartVO);
        }

        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    AjaxResponse clear(Locale locale, String currencyCode, CartVO cartVO) throws CurrencyRateException {
        AjaxResponse result = new AjaxResponse()
        if (cartVO.uuid) {
            // un paiement a été initialisé, on ne peut plus modifier le contenu du panier avant la fin du paiement (ou l'abandon)
            result = ajaxResponseService.addError(result, "cart", "initiate.payment.error", null, locale)
        }
        if (result.success) {
            cartVO.cartItemVOs.each { CartItemVO cartItem ->
                TicketType sku = TicketType.get(cartItem.getSkuId())
                productService.increment(sku, cartItem.quantity, cartItem.startDate)
            }
            cartVO.price = 0
            cartVO.endPrice = 0
            cartVO.count = 0;
            cartVO.cartItemVOs = null
            uuidDataService.removeCart();
        }

        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    AjaxResponse prepareBeforePayment(Company company, String countryCode, String stateCode, String currencyCode, CartVO cartVO) throws CurrencyRateException {
        MogopayRate rate = rateService.getMogopayRate(currencyCode)
        AjaxResponse result = new AjaxResponse()

        // Calcul des montants TTC
        cartVO.endPrice = 0;
        cartVO.cartItemVOs?.each { CartItemVO cartItem ->
            Product product = Product.get(cartItem.getProductId());
            cartItem.tax = taxRateService.findTaxRateByProduct(product, countryCode, stateCode)
            if (cartItem.tax == null) {
                // Le pays/state de livraison n'ont pas de taxRate associé,
                // la taxe est donc de 0
                cartItem.tax = 0;
            }
            cartItem.endPrice = taxRateService.calculateEndPrix(cartItem.price, cartItem.tax)
            cartItem.totalEndPrice = cartItem.quantity * cartItem.endPrice;
            cartVO.endPrice += cartItem.totalEndPrice
        }

        if (cartVO.uuid) {
            // On supprime tout ce qui concerne l'ancien BOCart (s'il est en attente)
            BOCart boCart = BOCart.findByTransactionUuidAndStatus(cartVO.uuid, TransactionStatus.PENDING)
            if (boCart) {
                BOCartItem.findByBOCart(boCart).each { BOCartItem boCartItem ->
                    boCartItem.bOProducts.each { BOProduct boProduct ->
                        Product product = boProduct.product;
                        BOTicketType.findByBOProduct(boProduct.id).each { BOTicketType boTicketType ->
                            boTicketType.delete()
                        }
                        boProduct.delete()
                    }
                    boCartItem.delete()
                }
                boCart.delete()
            }
        }
        if (result.success) {
            cartVO.uuid = UUID.randomUUID();


            BOCart boCart = new BOCart(
                    transactionUuid: cartVO.uuid,
                    date: Calendar.getInstance(),
                    price: cartVO.price,
                    status: TransactionStatus.PENDING,
                    currencyCode: currencyCode,
                    currencyRate: rate.rate,
                    company: company
            )
            boCart.save()

            cartVO.cartItemVOs.each { CartItemVO cartItem ->
                Product product = Product.get(cartItem.productId)
                TicketType ticketType = TicketType.get(cartItem.skuId)

                // Création du BOProduct correspondant au produit principal
                BOProduct boProduct = new BOProduct(
                        principal: true,
                        product: product,
                        price: cartItem.totalEndPrice)
                boProduct.save()

                cartItem.registeredCartItemVOs.each { RegisteredCartItemVO registeredCartItem ->
                    // Création des BOTicketType (SKU)
                    BOTicketType boTicket = new BOTicketType(
                            quantity: 1,
                            price: cartItem.totalEndPrice,
                            ticketType: ticketType.name,
                            firstname: registeredCartItem.firstname,
                            lastname: registeredCartItem.lastname,
                            email: registeredCartItem.email,
                            phone: registeredCartItem.phone,
                            birthdate: registeredCartItem.birthdate?.getTime(),
                            startDate: cartItem.startDate,
                            endDate: cartItem.endDate,
                            bOProduct: boProduct)
                    boTicket.save()

                    //génération du qr code uniquement pour les services
                    if (product.xtype == ProductType.SERVICE) {
                        boTicket.shortCode = "P" + boProduct.id + "T" + boTicket.id
                        String qrCodeContent = "EventId:" + product.id + ";BoProductId:" + boProduct.id + ";BoTicketId:" + boTicket.id
                        qrCodeContent += ";EventName:" + product.name + ";EventDate:" + DateUtilitaire.format(cartItem.startDate, "dd/MM/yyyy HH:mm") + ";FirstName:"
                        qrCodeContent += boTicket.firstname + ";LastName:" + boTicket.lastname + ";Phone:" + boTicket.phone
                        qrCodeContent += ";TicketType:" + boTicket.ticketType + ";shortCode:" + boTicket.shortCode
                        qrCodeContent = SecureCodec.encrypt(qrCodeContent, product.company.aesPassword);
                        ByteArrayOutputStream output = new ByteArrayOutputStream()
                        QRCodeUtils.createQrCode(output, qrCodeContent, 256, "png")
                        String qrCodeBase64 = Base64.encode(output.toByteArray())
                        boTicket.qrcode = qrCodeBase64
                        boTicket.qrcodeContent = qrCodeContent
                    }
                    boTicket.save()
                }

                //create Sale
                BOCartItem sale = new BOCartItem(
                        code: "SALE_" + boCart.id + "_" + boProduct.id,
                        price: cartItem.price,
                        tax: cartItem.tax,
                        endPrice: cartItem.endPrice,
                        totalPrice: cartItem.totalPrice,
                        totalEndPrice: cartItem.totalEndPrice,
                        hidden: false,
                        quantity: cartItem.quantity,
                        startDate: product.startDate,
                        endDate: product.stopDate,
                        bOCart: boCart,
                        bOProducts: [boProduct])
                sale.save()
            }
        }

        // Construit la map correspondant au panier pour le jsoniser
        Map map = [:]
        map["count"] = cartVO.count

        List<Map> listMapItem = []
        cartVO.cartItemVOs?.each { CartItemVO cartItem ->
            String[] included = [
                    "id", "productId", "productName", "xtype", "skuId", "skuName", "quantity", "tax", "startDate", "endDate",
                    "shipping",
                    "shipping.weight",
                    "shipping.weightUnit",
                    "shipping.width",
                    "shipping.height",
                    "shipping.depth",
                    "shipping.linearUnit",
                    "shipping.amount",
                    "shipping.free",
                    "registeredCartItemVOs",
                    "registeredCartItemVOs.cartItemId",
                    "registeredCartItemVOs.id",
                    "registeredCartItemVOs.email",
                    "registeredCartItemVOs.firstname",
                    "registeredCartItemVOs.lastname",
                    "registeredCartItemVOs.phone",
                    "registeredCartItemVOs.birthdate"
            ]
            Map mapItem = RenderUtil.asMapForJSON(null, included, null, cartItem)
            mapItem["price"] = rateService.calculateAmount(cartItem.price, rate);
            mapItem["endPrice"] = rateService.calculateAmount(cartItem.endPrice, rate);
            mapItem["totalPrice"] = rateService.calculateAmount(cartItem.totalPrice, rate);
            mapItem["totalEndPrice"] = rateService.calculateAmount(cartItem.totalEndPrice, rate);
            listMapItem << mapItem
        }
        map["cartItemVOs"] = listMapItem

        // Calcul du prix des coupons
        updateCoupons(cartVO);
        List<Map> listCoupon = []
        cartVO.coupons?.each { CouponVO c ->
            String[] included = [
                    "id",
                    "name",
                    "code",
                    "active",
                    "startDate",
                    "endDate"
            ]
            Map mapCoupon = RenderUtil.asMapForJSON(null, included, null, c)
            mapCoupon["price"] = rateService.calculateAmount(c.price, rate);
            listCoupon << mapCoupon
        }
        map["coupons"] = listCoupon

        map["price"] = rateService.calculateAmount(cartVO.price, rate)
        map["endPrice"] = rateService.calculateAmount(cartVO.endPrice, rate)
        map["reduction"] = rateService.calculateAmount(cartVO.reduction, rate)
        map["finalPrice"] = rateService.calculateAmount(cartVO.finalPrice, rate)

        Map data = [:]
        data["amount"] = rateService.calculateAmount(cartVO.finalPrice, rate)
        data["currencyCode"] = currencyCode
        data["currencyRate"] = rate.rate.doubleValue();
        data["transactionExtra"] = new JsonBuilder(map).toPrettyString();

        result.data = data
        uuidDataService.setCart(cartVO);
        return result;
    }

    AjaxResponse commit(CartVO cartVO, String transactionUuid) {
        def emailingData = []

        BOCart boCart = BOCart.findByTransactionUuid(cartVO.uuid)
        if (!boCart) {
            throw new IllegalArgumentException("Unabled to retrieve Cart " + cartVO.uuid + " into BO. It has not been initialized or has already been validated")
        }

        BOCartItem.findByBOCart(boCart).each { BOCartItem boCartItem ->
            boCartItem.bOProducts.each { BOProduct boProduct ->
                Product product = boProduct.product;
                BOTicketType.findByBOProduct(boProduct.id).each { BOTicketType boTicketType ->
                    def emailContent = [:]
                    emailContent.put("email", boTicketType.email)
                    emailContent.put("eventName", product.name)
                    emailContent.put("startDate", RenderUtil.asMapForJSON(boTicketType.startDate))
                    emailContent.put("stopDate", RenderUtil.asMapForJSON(boTicketType.endDate))
                    emailContent.put("location", toEventLocationVO(product.poi))
                    emailContent.put("type", boTicketType.ticketType)
                    emailContent.put("price", boTicketType.price)
                    emailContent.put("qrcode", boTicketType.qrcodeContent)
                    emailContent.put("shortCode", boTicketType.shortCode)
                    emailingData << emailContent
                }
            }
        }

        // Mise à jour du statut et du transactionUUID
        boCart.transactionUuid = transactionUuid;
        boCart.status = TransactionStatus.COMPLETE;
        boCart.save()

        cartVO.uuid = null;
        cartVO.price = 0
        cartVO.endPrice = 0
        cartVO.count = 0;
        cartVO.cartItemVOs = null
        uuidDataService.removeCart();

        sendEmails(emailingData)

        AjaxResponse result = new AjaxResponse()
        result.data = emailingData
        return result;
    }

    AjaxResponse cancel(Locale locale, String currencyCode, CartVO cartVO) throws CurrencyRateException {
        AjaxResponse result = new AjaxResponse()
        BOCart boCart = BOCart.findByTransactionUuid(cartVO.uuid)
        if (!boCart) {
            throw new IllegalArgumentException("Unabled to retrieve Cart " + cartVO.uuid + " into BO. It has not been initialized or has already been validated")
        }

        // Mise à jour du statut et du transactionUUID
        boCart.status = TransactionStatus.FAILED;
        boCart.save()

        cartVO.uuid = null;
        uuidDataService.setCart(cartVO);

        result.data = renderCart(locale, currencyCode, cartVO);
        return result;
    }

    /**
     * @param poi
     * @return formated location
     */
    private String toEventLocationVO(Poi poi) {
        def strLocation
        if (poi) {
            strLocation = poi.road1 ? poi.road1 + ", " : ""
            strLocation += poi.road2 ? poi.road2 + ", " : ""
            strLocation += poi.city ? poi.city + " " : ""
            strLocation += poi.postalCode ? poi.postalCode + " " : ""
            strLocation += poi.state ? poi.state + ". " : ""
            strLocation += poi.countryCode ? poi.countryCode + "." : ""
        }
        return strLocation
    }

    /**
     * @param liste
     */
    private void sendEmails(liste) {
        liste.each { ligne ->
            mailService.sendMail {
                to ligne.email
                subject 'Your Ticket : ' + ligne.eventName
                body(view: "/email/ticket",
                        model: [eventName: ligne.eventName,
                                startDate: ligne.startDate,
                                stopDate : ligne.stopDate,
                                startTime: ligne.startTime,
                                stopTime : ligne.stopTime,
                                location : ligne.location,
                                price    : ligne.price,
                                type     : ligne.type,
                                shortCode: ligne.shortCode,
                                qrcodeUrl: IperConstant.GET_QRCODE_URL + ligne.qrcode])
            }
        }
    }
}

