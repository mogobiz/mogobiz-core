/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package bootstrap

import com.mogobiz.store.domain.*
import com.mogobiz.store.vo.RegisteredCartItemVO
import com.mogobiz.tools.QRCodeUtils
import com.mogobiz.utils.DateUtilitaire
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.PermissionType
import com.mogobiz.utils.SecureCodec
import com.sun.org.apache.xml.internal.security.utils.Base64
import grails.util.Holders
import groovy.json.JsonBuilder

import static com.mogobiz.utils.ProfileUtils.ALL

public class CommonService {

	def sanitizeUrlService

    def profileService

    def countryImportService

    def translationService

    def destroy() {}
	def init() {

        // import countries
        final codes = (Holders.config.importCountries?.codes as String)?.split(',')?.collect{it.trim().toUpperCase()} ?: ['DE','ES','FR','GB','US']
        final countryCodes = Country.findAll().collect {it.code}
        def countries = codes.collect {code -> if(!countryCodes.contains(code)) code else []}.flatten() as Collection<String>
        if(countries.size() > 0){
            File countriesDir = new File(Holders.config.importCountries.dir as String)
            countryImportService.importAll(countries, countriesDir)
        }

		// création des roles
		Role admin = createRole(RoleName.ADMINISTRATOR)
		createRole(RoleName.CLIENT)
		createRole(RoleName.PARTNER)
		createRole(RoleName.VALIDATOR)

		// permissions
		Permission permission = Permission.findByTypeAndPossibleActions('org.apache.shiro.authz.permission.WildcardPermission', '*');
		if (!permission) {
			permission = new Permission(type:'org.apache.shiro.authz.permission.WildcardPermission', possibleActions:'*')
			saveEntity(permission)
            profileService.saveRolePermission(admin, true, PermissionType.ADMIN_COMPANY, ALL)
            profileService.saveRolePermission(admin, true, PermissionType.ADMIN_STORE_PROFILES, ALL)
            profileService.saveRolePermission(admin, true, PermissionType.ADMIN_STORE_USERS, ALL)
		}


        // création de l'admin
        User userAdmin = User.findByLogin(Holders.config.superadmin.login)
        if(userAdmin == null) {
            userAdmin = new User(login:Holders.config.superadmin.login, email:Holders.config.superadmin.email, password:Holders.config.superadmin.password, active:true)
            userAdmin.addToRoles(admin)
            saveEntity(userAdmin)
        }

        // création des variations google
        GoogleVariationType gender = GoogleVariationType.findByXtype('gender')
        if(gender == null){
            gender = new GoogleVariationType(xtype: 'gender')
            saveEntity(gender)
        }
        ['male', 'female', 'unisex'].each {value ->
            GoogleVariationValue val = GoogleVariationValue.findByValueAndType(value, gender)
            if(val == null){
                val = new GoogleVariationValue(value:value, type: gender)
                saveEntity(val)
            }
        }
        GoogleVariationType age_group = GoogleVariationType.findByXtype('age group')
        if(age_group == null){
            age_group = new GoogleVariationType(xtype: 'age group')
            saveEntity(age_group)
        }
        ['adults', 'kids'].each {value ->
            GoogleVariationValue val = GoogleVariationValue.findByValueAndType(value, age_group)
            if(val == null){
                val = new GoogleVariationValue(value:value, type: age_group)
                saveEntity(val)
            }
        }
        GoogleVariationType color = GoogleVariationType.findByXtype('color')
        if(color == null){
            color = new GoogleVariationType(xtype: 'color')
            saveEntity(color)
        }
        GoogleVariationType size = GoogleVariationType.findByXtype('size')
        if(size == null){
            size = new GoogleVariationType(xtype: 'size')
            saveEntity(size)
        }
        GoogleVariationType material = GoogleVariationType.findByXtype('material')
        if(material == null){
            material = new GoogleVariationType(xtype: 'material')
            saveEntity(material)
        }
        GoogleVariationType pattern = GoogleVariationType.findByXtype('pattern')
        if(pattern == null){
            pattern = new GoogleVariationType(xtype: 'pattern')
            saveEntity(pattern)
        }

        EsEnv.findAll().each {env ->
            env.running = false
            env.save()
        }

        def name = "admin"
        def parent = Profile.findByCodeAndCompanyIsNull(name) ?: new Profile(name: name, code:name).save(flush:true)
        def oldPermissions = ProfilePermission.findAllByProfile(parent)
        oldPermissions.each { it.delete(flush: true) }
        PermissionType.admin().each {pt ->
            profileService.saveProfilePermission(parent, true, pt)
        }
        profileService.upgradeChildProfiles(parent)
        Company.findAll().each {company ->
            def child = Profile.findByCompanyAndParent(company, parent)
            if(!child) {
                child = profileService.applyProfile(parent, company.id)
            }
            Seller.findAllByCompanyAndAdmin(company, true).each {administrator ->
                profileService.saveUserPermission(administrator, false, PermissionType.ADMIN_COMPANY, company.id as String)
                profileService.addUserProfile(administrator, child)
                administrator.refresh()
                administrator.admin = false
                administrator.save(flush: true)
            }
        }

        name = "validator"
        parent = Profile.findByCodeAndCompanyIsNull(name) ?: new Profile(name: name, code:name).save(flush:true)
        oldPermissions = ProfilePermission.findAllByProfile(parent)
        oldPermissions.each { it.delete(flush: true) }
        PermissionType.validator().each {pt ->
            profileService.saveProfilePermission(parent, true, pt)
        }
        profileService.upgradeChildProfiles(parent)
        Company.findAll().each {company ->
            def child = Profile.findByCompanyAndParent(company, parent)
            if(!child) {
                child = profileService.applyProfile(parent, company.id)
            }
            Seller.findAllByCompanyAndValidator(company, true).each {validator ->
                PermissionType.validator().each { pt ->
                    profileService.saveUserPermission(validator, false, pt, company.id as String)
                }
                profileService.addUserProfile(validator, child)
                validator.refresh()
                validator.validator = false
                validator.save(flush: true)
            }
        }

        name = "seller"
        parent = Profile.findByCodeAndCompanyIsNull(name) ?: new Profile(name: name, code:name).save(flush:true)
        oldPermissions = ProfilePermission.findAllByProfile(parent)
        oldPermissions.each { it.delete(flush: true) }
        PermissionType.seller().each {pt ->
            profileService.saveProfilePermission(parent, true, pt)
        }
        profileService.upgradeChildProfiles(parent)
        Company.findAll().each {company ->
            def child = Profile.findByCompanyAndParent(company, parent)
            if(!child){
                child = profileService.applyProfile(parent, company.id)
            }
            Seller.findAllByCompanyAndSell(company, true).each {seller ->
                PermissionType.seller().each {pt ->
                    profileService.saveUserPermission(seller, false, pt, company.id as String)
                }
                profileService.addUserProfile(seller, child)
                Catalog.findAllByCompany(company).each {catalog ->
                    profileService.saveUserPermission(
                            seller,
                            true,
                            PermissionType.UPDATE_STORE_CATALOG,
                            company.id as String,
                            catalog.id as String
                    )
                    profileService.saveUserPermission(
                            seller,
                            false,
                            PermissionType.UPDATE_STORE_CATEGORY_WITHIN_CATALOG,
                            company.id as String,
                            catalog.id as String,
                            ALL
                    )
                    Category.findAllByCatalog(catalog).each { category ->
                        profileService.saveUserPermission(
                                seller,
                                true,
                                PermissionType.UPDATE_STORE_CATEGORY_WITHIN_CATALOG,
                                company.id as String,
                                catalog.id as String,
                                category.id as String
                        )
                    }
                }
                EsEnv.findAllByCompany(company).each {env ->
                    profileService.saveUserPermission(
                            seller,
                            true,
                            PermissionType.PUBLISH_STORE_CATALOGS_TO_ENV,
                            company.id as String,
                            env.id as String
                    )
                }
                MiraklEnv.findAllByCompany(company).each {env ->
                    profileService.saveUserPermission(
                            seller,
                            true,
                            PermissionType.PUBLISH_STORE_CATALOGS_TO_ENV,
                            company.id as String,
                            env.id as String
                    )
                }
                seller.refresh()
                seller.sell = false
                seller.save(flush: true)
            }
        }

        // begin stuff to export bo entities for mogopay
        def all = Company.findByCode(ALL)
        if(!all){
            all = new Company(code: ALL, name: "ALL", aesPassword: SecureCodec.genKey())
            saveEntity(all)
        }
        def esEnv = EsEnv.findByCompanyAndName(all, "mogopay")
        if(!esEnv){
            esEnv = new EsEnv(company: all, name: "mogopay", cronExpr: "0 0 0 * * ?", url: Holders.config.mogopay?.elasticsearch?.serverURL as String, active: false)
            saveEntity(esEnv)
        }
        profileService.saveUserPermission(
                userAdmin,
                true,
                PermissionType.PUBLISH_BO_TO_MOGOPAY,
                ALL,
                esEnv.id as String
        )
        // end stuff to export bo entities for mogopay

        // update categories full path
        Category.executeQuery('FROM Category where fullpath is null').each {
            it.fullpath = ''
            it.validate()
            if(!it.hasErrors()){
                it.save(flush: true)
            }
        }
	}

	public Feature createFeature(String name, String value, Product product, int position, boolean flush = true)
	{
		Feature feature = Feature.findByNameAndValueAndProduct(name, value, product)
		if (feature == null) {
			feature = new Feature(name : name, position: position, uuid:UUID.randomUUID().toString(), value:value, product:product)
			saveEntity(feature, flush)
		}
		return feature
	}

	public Brand createBrand(String name, String website, Company company, boolean hide = false)
	{
		Brand brand = Brand.findByNameAndCompany(name, company);
		if (brand == null)
		{
			brand = new Brand(name: name, website: website, company: company, hide: hide);
			saveEntity(brand);
		}
		return brand;
	}

    public Tag createTag(String name, Company company)
    {
        Tag tag = Tag.findByName(name);
        if (tag == null)
        {
            tag = new Tag(name: name, company:company);
            saveEntity(tag);
        }
        return tag;
    }

    public Translation createTranslation(Company company, String lang, long target, Map translations, boolean flush = true){
        Translation tr = new Translation(companyId: company?.id, lang: lang, target: target)
        JsonBuilder builder = new JsonBuilder()
        builder.call(translations)
        tr.value = builder.toString()
        tr.validate()
        if(!tr.hasErrors()){
            saveEntity(tr, false)
        }
        else{
            tr.errors.allErrors.each {
                log.warn(it)
            }
        }
        tr
    }

    public Variation createVariation(String name, int position, Category category, List listeValeurs)
	{
		Variation variation = Variation.findByNameAndCategory(name, category);
		if (variation == null)
		{
			variation = new Variation(name: name, position: position, hide: false, uuid: name, category: category);
            saveEntity(variation);

			// création des valeurs possibles
			listeValeurs.eachWithIndex { String valeur, int index ->
				VariationValue variationValue = new VariationValue(value: valeur, position: index, variation:variation);
				saveEntity(variationValue);
				variation.addToVariationValues(variationValue);
			}
			saveEntity(variation);
		}
		return variation;
	}

    public Suggestion createSuggestion(boolean required, int position, String discount, Product product, Product pack){
        Suggestion suggestion = new Suggestion(
                required:required,
                position:position,
                discount:discount,
                product: product,
                pack:pack
        )
        saveEntity(suggestion)
        return suggestion
    }

    public Shipping createShipping(long weight, long width, long height, long depth) {
        Shipping shipping = new Shipping();
        shipping.weight = weight
        shipping.weightUnit = WeightUnit.KG
        shipping.width = width
        shipping.height = height
        shipping.depth = depth
        shipping.linearUnit = LinearUnit.CM
        shipping.amount = 0
        shipping.free = false
        return shipping
    }

	public Product createProduct(String code, String name, long montant, ProductType xtype, ProductCalendar calendarType, Company company, Brand brand, Category category, TaxRate taxRate, String description, Collection<Tag> tags = [], Shipping shipping = null, String keywords = null, boolean flush = true)
	{
		Product product = Product.findByCode(code);
		if (product == null)
		{
            if (shipping) {
                saveEntity(shipping)
            }

			product = new Product(code: code,
				name: name,
				price: montant,
				description: description,
				descriptionAsText: description,
				xtype: xtype,
				state: ProductState.ACTIVE,
				startDate: getDateDebutAnnee(),
				stopDate: getDateFinAnnee(),
				startFeatureDate: getDateDebutMois(),
				stopFeatureDate: getDateFinMois(),
				calendarType: calendarType,
				sanitizedName: sanitizeUrlService.sanitizeWithDashes(name),
				company: company,
				brand: brand,
				category: category,
                shipping: shipping,
                taxRate: taxRate,
                keywords: keywords
			);
			saveEntity(product, flush);

            if(!tags?.isEmpty()){
                tags.each {
                    product.addToTags(it)
                }
                saveEntity(product)
            }
		}
		return product;
	}

	public Translation createTranslation(Company company, long target, String lang, String value, String type, boolean flush = true)
	{
		Translation translation = Translation.findByTargetAndLang(target, lang);
		if (translation == null)
		{
			translation = new Translation(companyId: company.id, target: target, lang: lang, value: value, type: type);
			saveEntity(translation, flush);
		}
		return translation;
	}

	public IntraDayPeriod createIntraDayPeriod(Product product, Calendar startDate, Calendar endDate, boolean weekday1, boolean weekday2, boolean weekday3, boolean weekday4, boolean weekday5, boolean weekday6, boolean weekday7) {
		IntraDayPeriod period = IntraDayPeriod.findByStartDateAndProduct(startDate, product);
		if (period == null) {
			period = new IntraDayPeriod(startDate: startDate, endDate: endDate,
				weekday1: false, weekday2: false, weekday3: false, weekday4: false, weekday5: false, weekday6: true, weekday7: true,
				product: product
			);
			saveEntity(period);
		}
		return period
	}

	public TicketType createSKU(String name, long montant, Product produit, String variation1, String variation2, String variation3, long nombreEnStock, boolean flush = true)
	{
		TicketType sku = TicketType.findByNameAndProduct(name, produit);
		if (sku == null)
		{
			Stock stock = new Stock(stock: Math.max(0, nombreEnStock), stockUnlimited: (nombreEnStock.intValue() == -1));
			saveEntity(stock);

			sku = new TicketType(sku:UUID.randomUUID().toString(), name: name, price: montant, minOrder: 1, maxOrder: 10, product: produit, stock: stock, startDate: produit.startDate, stopDate: produit.stopDate);
			if (variation1 != null)
			{
				sku.variation1 = VariationValue.findByValue(variation1);
			}
			if (variation2 != null)
			{
				sku.variation2 = VariationValue.findByValue(variation2);
			}
			if (variation3 != null)
			{
				sku.variation3 = VariationValue.findByValue(variation3);
			}
			saveEntity(sku, flush);
		}
		return sku;
	}

    public BOCart createBOCart(Company company, String transactionUUID) {
        BOCart boCart = BOCart.findByTransactionUuid(transactionUUID)
        if(boCart == null){
            boCart = new BOCart(
                    transactionUuid : transactionUUID,
                    buyer: "yoann.baudy@ebiznext.com",
                    date : Calendar.getInstance(),
                    price : 10000,
                    status : TransactionStatus.PENDING,
                    currencyCode : "EUR",
                    currencyRate : 0.01,
                    company: company
            )
            saveEntity(boCart);
        }
        return boCart;
    }

    public BOCartItem createBOCartItem(BOCart boCart, Product product, TicketType ticketType, Calendar startDate, Calendar endDate, RegisteredCartItemVO registeredCartItem) {
        // Création du BOProduct correspondant au produit principal
        BOProduct boProduct = new BOProduct(
                principal : true,
                product : product,
                price : 10000)
        saveEntity(boProduct);

        if (registeredCartItem != null) {
            // Création des BOTicketType (SKU)
            BOTicketType boTicket = new BOTicketType(
                    quantity : 1,
                    price : 10000,
                    ticketType : ticketType.name,
                    firstname : registeredCartItem.firstname,
                    lastname : registeredCartItem.lastname,
                    email : registeredCartItem.email,
                    phone : registeredCartItem.phone,
                    birthdate : registeredCartItem.birthdate?.getTime(),
                    startDate : startDate,
                    endDate : endDate,
                    bOProduct : boProduct)
            saveEntity(boTicket);

            //génération du qr code uniquement pour les services
            if (product.xtype == ProductType.SERVICE)
            {
                boTicket.shortCode = "P" + boProduct.id + "T" + boTicket.id
                String qrCodeContent = "EventId:"+product.id+";BoProductId:"+boProduct.id+";BoTicketId:"+boTicket.id
                qrCodeContent += ";EventName:" + product.name + ";EventDate:" + DateUtilitaire.format(startDate, "dd/MM/yyyy HH:mm") + ";FirstName:"
                qrCodeContent += boTicket.firstname + ";LastName:" + boTicket.lastname + ";Phone:" + boTicket.phone
                qrCodeContent += ";TicketType:" +boTicket.ticketType + ";shortCode:" + boTicket.shortCode
                qrCodeContent = SecureCodec.encrypt(qrCodeContent, product.company.aesPassword);
                ByteArrayOutputStream output = new ByteArrayOutputStream()
                QRCodeUtils.createQrCode(output, qrCodeContent, 256,"png")
                String qrCodeBase64 = Base64.encode(output.toByteArray())
                boTicket.qrcode = qrCodeBase64
                boTicket.qrcodeContent = qrCodeContent
            }
            saveEntity(boTicket);
        }

        //create Sale
        BOCartItem sale = new BOCartItem(
                code : "SALE_" + boCart.id + "_" + boProduct.id,
                price: 10000,
                tax: 0,
                endPrice: 10000,
                totalPrice: 10000,
                totalEndPrice: 10000,
                hidden : false,
                quantity : 1,
                startDate : product.startDate,
                endDate : product.stopDate,
                bOCart : boCart,
                bOProducts : [boProduct])
        saveEntity(sale);
        return sale;
    }

	/**
	 * Create a Role if it does not exist
	 * @param roleName
	 * @return
	 */
	public Role createRole(RoleName roleName) {
		Role role = Role.findByName(roleName)
		if (role == null) {
			role = new Role(name:roleName)
			saveEntity(role)
		}
		return role
	}

	/**
	 * Create a category
	 * @param nom
	 * @param parent
	 * @param company
	 * @param catalog
	 * @param position
	 * @return
	 */
	public Category createCategory(String nom, Category parent, Company company, Catalog catalog, int position, String keywords = null)
	{
		Category category = Category.findByName(nom)
		if (category == null) {
			category = new Category(name : nom, sanitizedName: sanitizeUrlService.sanitizeWithDashes(nom), catalog:catalog, company : company, parent:parent, uuid:UUID.randomUUID().toString(), position:position)
            category.keywords = keywords
			saveEntity(category)
		}
        return category
	}

    public ReductionRule createReductionRule(ReductionRuleType xtype, String discount = "10%", Long xPurchased = null, Long yOffered = null) {
        ReductionRule rule = new ReductionRule()
        rule.xtype = xtype
        rule.discount = discount
        rule.xPurchased = xPurchased
        rule.yOffered = yOffered
        return rule
    }

    public Coupon createCoupon(Company company, List<ReductionRule> rules, String code, String name, Long numberOfUses = null, List<Category> categories = null, List<Product> products = null, List<TicketType> ticketTypes = null, String description = null, boolean anonymous=false) {
        Coupon coupon = Coupon.findByCode(code)
        if (coupon == null) {
            coupon = new Coupon()
            coupon.company = company
            coupon.name = name
            coupon.code = code
            coupon.active = true
            coupon.numberOfUses = numberOfUses
            coupon.categories = categories
            coupon.products = products
            coupon.ticketTypes = ticketTypes
            coupon.rules = rules
            rules?.each { ReductionRule rule ->
                saveEntity(rule)
            }
            coupon.description = description
            coupon.startDate = Calendar.getInstance()
            def endDate = Calendar.getInstance()
            endDate.add(Calendar.DAY_OF_YEAR, 7)
            coupon.endDate = endDate
            coupon.anonymous = anonymous
            saveEntity(coupon)
        }
        return coupon
    }

    public ShippingRule createShippingRule(Company company, String countryCode, long minAmount, long maxAmount, String price ){
        ShippingRule shippingRule = new ShippingRule(
                company : company,
                countryCode: countryCode,
                minAmount: minAmount,
                maxAmount: maxAmount,
                price: price
        )
        saveEntity(shippingRule)
        shippingRule
    }

	public void saveEntity(def entite, boolean flush = true)
	{
		String msg = "";
		if (entite.validate())
		{
			entite.save(flush: flush)
		}
		else
		{
			entite.errors?.allErrors?.each{error ->
				msg += (error.toString() + "\n")
			}
			throw new Exception(msg);
		}
	}

	private Calendar getDateDebutAnnee()
	{
		Calendar c = Calendar.getInstance();
        c = IperUtil.resetCalendarTime(c)
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c;
	}

	private  Calendar getDateFinAnnee()
	{
		Calendar c = Calendar.getInstance();
        c = IperUtil.resetCalendarTime(c)
		c.set(Calendar.MONTH, 11);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.MINUTE, -1);
		return c;
	}

	private  Calendar getDateDebutMois(Integer heure = null)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		if (heure) {
			c.set(Calendar.HOUR_OF_DAY, heure);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
		}
		else {
			c = IperUtil.resetCalendarTime(c)
		}
		return c;
	}

	private  Calendar getDateFinMois(Integer heure = null)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		if (heure) {
			c.set(Calendar.HOUR_OF_DAY, heure);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
		}
		else {
			c = IperUtil.resetCalendarTime(c)
		}
		return c;
	}

    public static void main(String [] args) {
        String password1 = "5c3f3da15cae1bf2bc736b95bda10c78";
        String qrCodeContent = "EventId:88;BoProductId:102;BoTicketId:103;EventName:Le père noël est une ordure;EventDate:01/02/2014 00:00;FirstName:Test;LastName:Test;Phone:null;TicketType:Adult;shortCode:P102T103";
        String r1 = SecureCodec.encrypt(qrCodeContent, password1);
        System.out.println(r1);
        FileOutputStream output = new FileOutputStream("/Users/yoannbaudy/Desktop/qrCode.png");
        QRCodeUtils.createQrCode(output, r1, 256,"png")
    }
}
