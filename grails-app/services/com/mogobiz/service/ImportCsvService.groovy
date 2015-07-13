package com.mogobiz.service

import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Brand
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Coupon
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.FeatureValue
import com.mogobiz.store.domain.LocalTaxRate
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Product2Resource
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.ProductProperty
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.ProductType
import com.mogobiz.store.domain.ReductionRule
import com.mogobiz.store.domain.ReductionRuleType
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.store.domain.ShippingRule
import com.mogobiz.store.domain.Stock
import com.mogobiz.store.domain.Tag
import com.mogobiz.store.domain.TaxRate
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.Variation
import com.mogobiz.store.domain.VariationValue
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.ZipFileUtil
import grails.util.Holders
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hibernate.SessionFactory
import org.jsoup.Jsoup
import grails.transaction.Transactional

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.concurrent.Future
import java.util.zip.ZipFile

@Transactional
class ImportCsvService {

    SanitizeUrlService sanitizeUrlService
    ResService resService
    def grailsApplication
    SessionFactory sessionFactory

    int FLUSHSIZE = Holders.config.importCatalog.flushsize ?: 100

    int NBTHREADS = Holders.config.importCatalog.nbthreads ?: 3

    private String getFormulaCell(String cellValue, Map<String, XSSFSheet> sheets) {
        String[] tab = cellValue.split('!')
        XSSFSheet sheet = sheets.get(tab[0])
        CellReference cellReference = new CellReference(tab[1])
        Row xrow = sheet.getRow(cellReference.getRow());
        XSSFCell xcell = xrow.getCell(cellReference.getCol());
        return xcell.toString()
    }

    private Category getParentCategoryFromPath(String path, Catalog catalog) {
        String[] paths = path.split('/')
        Category parent = null
        for (int i = 0; i < paths.size() - 1; i++) {
            String parentName = paths[i]
            parent = Category.findByNameAndParentAndCatalog(parentName, parent, catalog)
        }
        return parent
    }

    private Category getCategoryFromPath(String path, Catalog catalog) {
        String[] paths = path.split('/')
        Category parent = null
        for (int i = 0; i < paths.size(); i++) {
            String parentName = paths[i]
            parent = Category.findByNameAndParentAndCatalog(parentName, parent, catalog)
        }
        return parent
    }

    private Calendar getCalendar(String dateStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(dateStr);
            Calendar cal = new GregorianCalendar();
            cal.setTime(date)
            return cal
        }
        catch (Exception e) {
            return null
        }
    }

    private getVariationValue(String variationName, String variationValue, String catpath, Catalog catalog) {
        if (variationName && variationValue) {
            Variation v = Variation.findByNameAndCategory(variationName, getCategoryFromPath(catpath, catalog))
            List<VariationValue> vvs = VariationValue.findAllByVariation(v)
            VariationValue vv = vvs.find {
                it.value == variationValue
            }
            return vv
        }
        return null
    }

    public File getImportDir(String now) {
        String impexPath = grailsApplication.config.impex.path
        if (!impexPath)
            impexPath = System.getProperty("java.io.tmpdir")
        File impexDir = new File(new File(impexPath, "import"), now)
        impexDir.mkdirs()
        return impexDir
    }

    Map ximport(Catalog catalog, ZipFile zipFile) {
        String now = new SimpleDateFormat("yyyy-MM-dd.HHmmss").format(new Date())
        File impexDir = getImportDir(now)
        ZipFileUtil.unzipFileIntoDirectory(zipFile, impexDir)
        File dateDir = impexDir.listFiles().find {
            it.isDirectory()
        }
        File inputFile = new File(dateDir, "mogobiz.xlsx")
        log.info("Loading file ...")
        XSSFWorkbook workbook = new XSSFWorkbook(inputFile.getAbsolutePath())
        log.info("File loaded")
        XSSFSheet brandSheet = workbook.getSheet("brand");
        XSSFSheet catSheet = workbook.getSheet("category");
        XSSFSheet catFeatSheet = workbook.getSheet("cat-feature");
        XSSFSheet varSheet = workbook.getSheet("variation");
        XSSFSheet varValSheet = workbook.getSheet("variation-value");
        XSSFSheet prdSheet = workbook.getSheet("product");
        XSSFSheet prdFeatSheet = workbook.getSheet("product-feature");
        XSSFSheet prdPropSheet = workbook.getSheet("product-property");
        XSSFSheet skuSheet = workbook.getSheet("sku");
        XSSFSheet taxSheet = workbook.getSheet("taxrate");
        XSSFSheet shipSheet = workbook.getSheet("shipping");
        XSSFSheet couponSheet = workbook.getSheet("coupon");
        XSSFSheet couponRuleSheet = workbook.getSheet("coupon-rule");
        XSSFSheet couponUseSheet = workbook.getSheet("coupon-use");

        Map<String, XSSFSheet> sheets = [
                'category'       : catSheet,
                'cat-feature'    : catFeatSheet,
                'variation'      : varSheet,
                'variation-value': varValSheet,
                'product'        : prdSheet,
                'product-feature': prdFeatSheet,
                'sku'            : skuSheet,
        ]

        if(taxSheet){
            log.info("Importing tax rates")
            // Tax Rate
            TaxRate.withNewTransaction {
                for (int rownum = 1; rownum < taxSheet.getPhysicalNumberOfRows(); rownum++) {
                    XSSFRow row = taxSheet.getRow(rownum)
                    if (row != null) {
                        String cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                        if (cell.length() > 0) {
                            String uuid = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString()
                            String name = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                            String countryCode = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                            String stateCode = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                            String rate = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                            String active = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                            TaxRate t = TaxRate.findByNameAndCompany(name, catalog.company)
                            if (!t) {
                                t = new TaxRate()
                                t.company = catalog.company
                                t.uuid = uuid ? uuid : UUID.randomUUID().toString()
                                t.name = name
                                if (t.validate())
                                    t.save(flush: true)
                                else {
                                    t.errors.allErrors.each { println(it) }
                                    return [errors: t.errors.allErrors, sheet: "taxrate", line: rownum]
                                }
                            }
                            LocalTaxRate l = new LocalTaxRate()
                            l.uuid = uuid
                            l.active = active.equalsIgnoreCase("true")
                            l.countryCode = countryCode ?: null
                            l.stateCode ?: null
                            l.rate = rate.toFloat()

                            if (l.validate()) {
                                l.save(flush: true)
                            } else {
                                l.errors.allErrors.each { println(it) }
                                return [errors: l.errors.allErrors, sheet: "taxrate", line: rownum]
                            }
                        }
                    }
                }
            }
        }

        if(shipSheet){
            log.info("Importing shipping")
            // Shipping
            ShippingRule.withNewTransaction {
                for (int rownum = 1; rownum < shipSheet.getPhysicalNumberOfRows(); rownum++) {
                    XSSFRow row = shipSheet.getRow(rownum)
                    if (row != null) {
                        String cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                        if (cell.length() > 0) {
                            String uuid = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString()
                            String countryCode = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                            String minAmount = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                            String maxAmount = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                            String price = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                            ShippingRule sr = ShippingRule.findByUuid(uuid)
                            if (!sr) {
                                sr = new ShippingRule()
                            }
                            sr.uuid = uuid ? uuid : UUID.randomUUID().toString()
                            sr.countryCode = countryCode
                            sr.minAmount = minAmount.toFloat().toLong()
                            sr.maxAmount = maxAmount.toFloat().toLong()
                            sr.price = price
                            sr.company = catalog.company
                            if (sr.validate())
                                sr.save(flush: true)
                            else {
                                sr.errors.allErrors.each { println(it) }
                                return [errors: sr.errors.allErrors, sheet: "shipping", line: rownum]
                            }
                        }
                    }
                }
            }
        }

        if(couponSheet){
            log.info("Importing Coupons")
            Coupon.withNewTransaction {
                for (int rownum = 1; rownum < couponSheet.getPhysicalNumberOfRows(); rownum++) {
                    XSSFRow row = couponSheet.getRow(rownum)
                    if (row != null) {
                        String cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                        if (cell.length() > 0) {
                            String uuid = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString()
                            String name = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                            String code = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                            String active = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                            String numberOfUses = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                            String startDate = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                            String endDate = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                            String catalogWise = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                            String forSale = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString()
                            String description = row.getCell(9, Row.CREATE_NULL_AS_BLANK).toString()
                            String anonymous = row.getCell(10, Row.CREATE_NULL_AS_BLANK).toString()
                            String pastille = row.getCell(11, Row.CREATE_NULL_AS_BLANK).toString()
                            String consumed = row.getCell(12, Row.CREATE_NULL_AS_BLANK).toString()
                            Coupon coupon = Coupon.findByUuid(uuid)
                            if (!coupon) {
                                coupon = new Coupon()
                            }
                            coupon.uuid = uuid ? uuid : UUID.randomUUID().toString()
                            coupon.name = name
                            coupon.code = code
                            coupon.active = active.equalsIgnoreCase("true")
                            coupon.numberOfUses = numberOfUses.length() > 0 ? numberOfUses.toFloat().toLong() : null
                            coupon.startDate = getCalendar(startDate)
                            coupon.endDate = getCalendar(endDate)
                            coupon.catalogWise = catalogWise.equalsIgnoreCase("true")
                            coupon.forSale = forSale.equalsIgnoreCase("true")
                            coupon.description = description
                            coupon.anonymous = anonymous.equalsIgnoreCase("true")
                            coupon.pastille = pastille
                            coupon.consumed = consumed.length() > 0 ? consumed.toFloat().toLong() : null
                            coupon.company = catalog.company
                            if (coupon.validate()) {
                                coupon.save(flush: true)

                            } else {
                                coupon.errors.allErrors.each { println(it) }
                                return [errors: coupon.errors.allErrors, sheet: "coupon", line: rownum]
                            }
                        }
                    }
                }
            }
        }

        if(couponRuleSheet){
            log.info("Importing Reduction rules")
            //TODO improve inserts
            Coupon.withNewTransaction {
                for (int rownum = 1; rownum < couponRuleSheet.getPhysicalNumberOfRows(); rownum++) {
                    XSSFRow row = couponRuleSheet.getRow(rownum)
                    if (row != null) {
                        String cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                        if (cell.length() > 0) {
                            String couponCode = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString()
                            String uuid = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                            String xtype = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                            String qMin = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                            String qMax = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                            String discount = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                            String xpurchased = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                            String yoffered = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                            Coupon coupon = Coupon.findByCompanyAndCode(catalog.company, couponCode)
                            if (coupon) {
                                ReductionRule rr = ReductionRule.findByUuid(uuid)
                                if (!rr) {
                                    rr = new ReductionRule()
                                }
                                rr.uuid = uuid ? uuid : UUID.randomUUID().toString()
                                rr.xtype = ReductionRuleType.valueOf(xtype)
                                rr.quantityMin == qMin.length() > 0 ? qMin.toFloat().toLong() : null
                                rr.quantityMax = qMax.length() > 0 ? qMax.toFloat().toLong() : null
                                rr.discount = discount.length() > 0 ? discount : null
                                rr.xPurchased = xpurchased.length() > 0 ? xpurchased.toFloat().toLong() : null
                                rr.yOffered = yoffered.length() > 0 ? yoffered.toFloat().toLong() : null
                                rr.save(flush: true)
                                coupon.addToRules(rr)
                                if (coupon.validate()) {
                                    coupon.save(flush: true)

                                } else {
                                    coupon.errors.allErrors.each { println(it) }
                                    return [errors: coupon.errors.allErrors, sheet: "coupon-rule", line: rownum]
                                }
                            }
                        }
                    }
                }
            }
        }

        if(couponUseSheet){
            log.info("Importing Coupon Uses")
            //TODO improve Coupon Use
            Coupon.withNewTransaction {
                for (int rownum = 1; rownum < couponUseSheet.getPhysicalNumberOfRows(); rownum++) {
                    XSSFRow row = couponUseSheet.getRow(rownum)
                    if (row != null) {
                        String cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                        if (cell.length() > 0) {
                            String uuid = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                            String code = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                            String catUuid = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                            String prodUuid = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                            String skuUuid = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                            Coupon coupon = Coupon.findByUuid(uuid)
                            if (coupon) {
                                if (catUuid.length() > 0) {
                                    coupon.addToCategories(Category.findByUuid(catUuid))
                                } else if (prodUuid.length() > 0) {
                                    coupon.addToProducts(Product.findByUuid(prodUuid))
                                } else if (skuUuid.length() > 0) {
                                    coupon.addToTicketTypes(TicketType.findByUuid(skuUuid))
                                }
                                if (coupon.validate()) {
                                    coupon.save(flush: true)

                                } else {
                                    coupon.errors.allErrors.each { println(it) }
                                    return [errors: coupon.errors.allErrors, sheet: "coupon-use", line: rownum]
                                }
                            }
                        }
                    }
                }
            }
        }

        log.info("Importing brands")
        Map<String, Brand> brands = new HashMap<>()
        // Brand
        Brand.withNewTransaction {
            for (int rownum = 1; rownum < brandSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = brandSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                    if (cell.length() > 0) {
                        String uuid = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString()
                        String name = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                        String website = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                        String facebook = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                        String twitter = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                        String description = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                        String hide = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()

                        if (!Brand.findByNameAndCompany(name, catalog.company)) {
                            Brand b = new Brand()
                            b.company = catalog.company
                            b.uuid = uuid ? uuid : UUID.randomUUID().toString()
                            b.name = name
                            b.website = website
                            b.facebooksite = facebook
                            b.twitter = twitter
                            b.description = description
                            b.hide = hide.equalsIgnoreCase("true")
                            if (b.validate()) {
                                b.save(flush: true)
                                brands.put(name, b)
                            } else {
                                b.errors.allErrors.each { println(it) }
                                return [errors: b.errors.allErrors, sheet: "cat-feature", line: rownum]
                            }
                        }
                    }
                }
            }
        }


        log.info("Importing categories")
        Map<String, Category> categories = new HashMap<>()
        int maxdepth = 0
        for (int rownum = 1; rownum < catSheet.getPhysicalNumberOfRows(); rownum++) {
            XSSFRow row = catSheet.getRow(rownum)
            if (row != null) {
                String path = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                if (path.length() > 0) {
                    int depth = path.substring(1).split('/').size()
                    if (depth > maxdepth) maxdepth = depth
                }
            }
        }

        Category.withNewTransaction {
            for (int currentDepth = 1; currentDepth <= maxdepth; currentDepth++) {
                for (int rownum = 1; rownum < catSheet.getPhysicalNumberOfRows(); rownum++) {
                    XSSFRow row = catSheet.getRow(rownum)
                    if (row != null) {
                        String cell = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                        if (cell.length() > 0) {
                            String uuid = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString()
                            String externalCode = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString()
                            String path = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                            String description = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                            String keywords = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                            String hide = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                            String seo = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                            String google = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                            String deleted = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString()

                            String[] paths = path.substring(1).split('/')

                            int depth = paths.size()
                            if (depth == currentDepth) {
                                Category parent = getParentCategoryFromPath(path.substring(1), catalog)
                                Category cat = new Category()
                                if (uuid.length() > 0)
                                    cat.uuid = uuid
                                else
                                    cat.uuid = UUID.randomUUID().toString()
                                cat.externalCode = externalCode
                                cat.name = paths[depth - 1]
                                cat.description = description
                                cat.keywords = keywords
                                cat.hide = hide.equalsIgnoreCase("true")
                                cat.sanitizedName = seo.length() == 0 ? sanitizeUrlService.sanitizeWithDashes(cat.name) : seo
                                cat.googleCategory = google
                                cat.deleted = deleted.toLowerCase().equals("false") ? false : true
                                cat.catalog = catalog
                                cat.company = catalog.company
                                cat.parent = parent
                                if (cat.validate()) {
                                    cat.save(flush: true)
                                    categories.put(path, cat)
                                } else {
                                    cat.errors.allErrors.each { println(it) }
                                    return [errors: cat.errors.allErrors, sheet: "category", line: rownum]
                                }
                            }
                        }
                    }
                }
            }
        }

        log.info("Importing category features")
        // Cat Features
        Map<String, Feature> features = new HashMap<>()
        Feature.withNewTransaction {
            for (int rownum = 1; rownum < catFeatSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = catFeatSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                    if (cell.length() > 0) {
                        String catuuid = getFormulaCell(row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String catpath = getFormulaCell(row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String uuid = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                        String externalCode = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                        String domain = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                        String name = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                        String value = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString()
                        String hide = row.getCell(9, Row.CREATE_NULL_AS_BLANK).toString()


                        Feature f = new Feature()
                        f.uuid = uuid ? uuid : UUID.randomUUID().toString()
                        f.category = categories.get(catpath) //getCategoryFromPath(catpath, catalog)
                        f.externalCode = externalCode
                        f.domain = domain
                        f.name = name
                        f.value = value
                        f.hide = hide.equalsIgnoreCase("true")
                        if (f.validate()) {
                            f.save(flush: true)
                            features.put(uuid, f)
                        } else {
                            f.errors.allErrors.each { println(it) }
                            return [errors: f.errors.allErrors, sheet: "cat-feature", line: rownum]
                        }

                    }
                }
            }
        }

        log.info("Importing variations")
        // Cat Variations
        Map<String, Variation> variations = new HashMap<String, Variation>()
        Variation.withNewTransaction {
            for (int rownum = 1; rownum < varSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = varSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                    if (cell.length() > 0) {
                        String catuuid = getFormulaCell(row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String catpath = getFormulaCell(row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String uuid = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                        String externalCode = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                        String name = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                        String google = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                        String hide = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()


                        Variation v = new Variation()
                        v.uuid = uuid ? uuid : UUID.randomUUID().toString()
                        v.category = categories.get(catpath) //getCategoryFromPath(catpath, catalog)
                        v.externalCode = externalCode
                        v.name = name
                        v.googleVariationType = google
                        v.position = row.getRowNum()
                        v.hide = hide.equalsIgnoreCase("true")
                        if (v.validate()) {
                            v.save(flush: false)
                            variations.put(catpath + "*" + name, v)
                        } else {
                            v.errors.allErrors.each { println(it) }
                            return [errors: v.errors.allErrors, sheet: "variation", line: rownum]
                        }

                    }
                }
            }
        }

        Map<String, VariationValue> variationValues = new HashMap<String, VariationValue>()

        // Variation Values
        VariationValue.withNewTransaction {
            for (int rownum = 1; rownum < varValSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = varValSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                    if (cell.length() > 0) {
                        String catuuid = getFormulaCell(row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String catpath = getFormulaCell(row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String varuuid = getFormulaCell(row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String varname = getFormulaCell(row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String uuid = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                        String externalCode = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                        String value = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                        String google = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()


                        VariationValue v = new VariationValue()
                        v.uuid = uuid ? uuid : UUID.randomUUID().toString()
                        v.variation = variations.get(catpath + "*" + varname)
                        // Variation.findByNameAndCategory(varname, categories.get(catpath)) // getCategoryFromPath(catpath, catalog))

                        if (v.variation == null) {
                            println(varname + "->" + catpath)
                        }
                        v.externalCode = externalCode
                        v.value = value
                        v.googleVariationValue = google
                        v.position = row.getRowNum()
                        if (v.validate()) {
                            v.save(flush: true)
                            variationValues.put(catpath + "*" + varname + "*" + value, v)
                        } else {
                            v.errors.allErrors.each { println(it) }
                            return [errors: v.errors.allErrors, sheet: "variation-value", line: rownum]
                        }

                    }
                }
            }
        }
        Map<String, TaxRate> taxRates = new HashMap<String, TaxRate>().asSynchronized()

        log.info("Importing products")
        Map<String, Product> products = new HashMap<String, Product>().asSynchronized()
        int countRows = prdSheet.getPhysicalNumberOfRows()
        importProducts(brands, categories, sheets, prdSheet, taxRates, catalog, dateDir, products, 1, countRows)

        int countInserts = 0;
        log.info("Importing product features")
        // Product Features
        Feature.withNewTransaction {
            for (int rownum = 1; rownum < prdFeatSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = prdFeatSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                    if (cell.length() > 0) {
                        String prduuid = getFormulaCell(row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String prdcode = getFormulaCell(row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String uuid = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                        String externalCode = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                        String domain = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                        String name = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                        String value = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString()
                        String hide = row.getCell(9, Row.CREATE_NULL_AS_BLANK).toString()
                        Product product = products.get(prdcode) ?: Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and d.id = :catalog and p.code = :code", [catalog: catalog.id, code: prdcode]).get(0)
                        boolean created = false
                        if (uuid) {
                            Feature feat = features.get(uuid)
                            if (feat != null) {
                                FeatureValue featVal = new FeatureValue(value: value, product: product, feature: feat)
                                featVal.save(flush: false)
                                created = true
                            }
                        }
                        if (!created) {
                            Feature f = new Feature()
                            f.uuid = uuid ?: UUID.randomUUID().toString()
                            f.product = product
                            f.externalCode = externalCode
                            f.domain = domain
                            f.name = name
                            f.value = value
                            f.hide = hide
                            if (f.validate()) {
                                f.save(flush: false)
                            } else {
                                f.errors.allErrors.each { println(it) }
                                return [errors: f.errors.allErrors, sheet: "product-feature", line: rownum]
                            }
                        }
                        countInserts++;
                        if (countInserts % 100 == 0) {
                            log.info(countInserts + " product features")
                            this.cleanUpGorm()
                        }
                    }
                }
            }
            this.cleanUpGorm()
        }

        log.info("Importing product properties")
        // Product Properties
        countInserts = 0
        ProductProperty.withNewTransaction {
            for (int rownum = 1; rownum < prdPropSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = prdPropSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                    if (cell.length() > 0) {
                        String catuuid = getFormulaCell(row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String catpath = getFormulaCell(row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String prduuid = getFormulaCell(row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String prdcode = getFormulaCell(row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                        String uuid = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                        String name = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                        String value = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()


                        ProductProperty pp = new ProductProperty()
                        Category category = categories.get(catpath) //getCategoryFromPath(catpath, catalog)
                        pp.uuid = uuid ? uuid : UUID.randomUUID().toString()
                        pp.product = products.get(prdcode) ?: Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and d.id = :catalog and p.code = :code and c.id = :category", [catalog: catalog.id, code: prdcode, category: category.id]).get(0)
                        pp.name = name
                        pp.value = value
                        if (pp.validate())
                            pp.save(flush: false)
                        else {
                            pp.errors.allErrors.each { println(it) }
                            return [errors: pp.errors.allErrors, sheet: "product-property", line: rownum]
                        }
                        countInserts++;
                        if (countInserts % 100 == 0) {
                            log.info(countInserts + " product properties")
                            this.cleanUpGorm()
                        }
                    }
                }
            }
            this.cleanUpGorm()
        }

        countInserts = 0
        log.info("Importing SKUs")
        // SKU
        List<String[]> skuList = new ArrayList<>(skuSheet.getPhysicalNumberOfRows())
        for (int rownum = 1; rownum < skuSheet.getPhysicalNumberOfRows(); rownum++) {
            XSSFRow row = skuSheet.getRow(rownum)
            if (row != null) {
                String cell = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                if (cell.length() > 0) {
                    String catuuid = getFormulaCell(row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                    String catpath = getFormulaCell(row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                    String prduuid = getFormulaCell(row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                    String prdcode = getFormulaCell(row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                    String uuid = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                    String externalCode = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                    String sku = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                    String name = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                    String price = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString()
                    String minorder = row.getCell(9, Row.CREATE_NULL_AS_BLANK).toString()
                    String maxorder = row.getCell(10, Row.CREATE_NULL_AS_BLANK).toString()
                    String sales = row.getCell(11, Row.CREATE_NULL_AS_BLANK).toString()
                    String startDate = row.getCell(12, Row.CREATE_NULL_AS_BLANK).toString()
                    String stopDate = row.getCell(13, Row.CREATE_NULL_AS_BLANK).toString()
                    String xprivate = row.getCell(14, Row.CREATE_NULL_AS_BLANK).toString()
                    String remainingStock = row.getCell(15, Row.CREATE_NULL_AS_BLANK).toString()
                    String unlimitedStock = row.getCell(16, Row.CREATE_NULL_AS_BLANK).toString()
                    String outsellStock = row.getCell(17, Row.CREATE_NULL_AS_BLANK).toString()
                    String description = row.getCell(18, Row.CREATE_NULL_AS_BLANK).toString()
                    String availability = row.getCell(19, Row.CREATE_NULL_AS_BLANK).toString()
                    String googleGtin = row.getCell(20, Row.CREATE_NULL_AS_BLANK).toString()
                    String googleMpn = row.getCell(21, Row.CREATE_NULL_AS_BLANK).toString()
                    String variationName1 = row.getCell(22, Row.CREATE_NULL_AS_BLANK).toString()
                    String variationValue1 = row.getCell(23, Row.CREATE_NULL_AS_BLANK).toString()
                    String variationName2 = row.getCell(24, Row.CREATE_NULL_AS_BLANK).toString()
                    String variationValue2 = row.getCell(25, Row.CREATE_NULL_AS_BLANK).toString()
                    String variationName3 = row.getCell(26, Row.CREATE_NULL_AS_BLANK).toString()
                    String variationValue3 = row.getCell(27, Row.CREATE_NULL_AS_BLANK).toString()

                    String[] cols = [
                            catuuid, catpath, prduuid, prdcode, uuid, externalCode, sku, name, price, minorder, maxorder,
                            sales, startDate, stopDate, xprivate, remainingStock, unlimitedStock, outsellStock, description,
                            availability, googleGtin, googleMpn, variationName1, variationValue1,
                            variationName2, variationValue2, variationName3, variationValue3
                    ]
                    skuList.add(cols)
                }
            }
        }

        int countLines = skuList.size()
        int nbthreads = NBTHREADS - 1
        List<Future<Integer>> futures = new ArrayList<>(nbthreads + 1)
        Range<Integer> range = 0..nbthreads
        int countPerThread = countLines / (nbthreads + 1)
        range.each { index ->
            Future<Integer> future = callAsync {
                int startIndex = index * countPerThread
                log.info("sku-worker-"+ index+" = " + startIndex)
                int rownum = startIndex
                int nbProductsToInsert
                if (index == nbthreads) {
                    nbProductsToInsert = countLines - countPerThread * nbthreads
                } else {
                    nbProductsToInsert = countPerThread
                }
                TicketType.withNewTransaction {
                    while (rownum < startIndex + nbProductsToInsert) {
                        int max = rownum + FLUSHSIZE
                        max = max > startIndex + countPerThread ? startIndex + nbProductsToInsert : max
                        while (rownum < max) {
                            String[] cols = skuList.get(rownum)
                            String catuuid = cols[0]
                            String catpath = cols[1]
                            String prduuid = cols[2]
                            String prdcode = cols[3]
                            String uuid = cols[4]
                            String externalCode = cols[5]
                            String sku = cols[6]
                            String name = cols[7]
                            String price = cols[8]
                            String minorder = cols[9]
                            String maxorder = cols[9]
                            String sales = cols[11]
                            String startDate = cols[12]
                            String stopDate = cols[13]
                            String xprivate = cols[14]
                            String remainingStock = cols[15]
                            String unlimitedStock = cols[16]
                            String outsellStock = cols[17]
                            String description = cols[18]
                            String availability = cols[19]
                            String googleGtin = cols[20]
                            String googleMpn = cols[21]
                            String variationName1 = cols[22]
                            String variationValue1 = cols[23]
                            String variationName2 = cols[24]
                            String variationValue2 = cols[25]
                            String variationName3 = cols[26]
                            String variationValue3 = cols[27]

                            Product prod = products.get(prdcode)
                            TicketType t = new TicketType()
                            t.uuid = uuid ? uuid : UUID.randomUUID().toString()
                            t.product = prod ?: Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and d.id = :catalog and p.code = :code", [catalog: catalog.id, code: prdcode]).get(0)
                            t.externalCode = externalCode
                            t.sku = sku
                            t.name = name
                            t.price = price.toDouble().toLong()
                            t.minOrder = minorder.toDouble().toInteger()
                            t.maxOrder = maxorder.toDouble().toInteger()
                            t.nbSales = sales.toDouble().toLong()
                            t.startDate = getCalendar(startDate)
                            t.stopDate = getCalendar(stopDate)
                            t.xprivate = xprivate.toLowerCase().equals("false") ? false : true
                            t.stock = new Stock()
                            if (remainingStock?.length() > 0) {
                                t.stock.stockUnlimited = unlimitedStock.toLowerCase().equals("false") ? false : true
                                t.stock.stockOutSelling = outsellStock.toLowerCase().equals("false") ? false : true
                                t.stock.stock = remainingStock.toDouble().toLong()
                            }
                            t.description = description
                            t.availabilityDate = getCalendar(availability)
                            t.gtin = googleGtin
                            t.mpn = googleMpn
                            VariationValue vv1 = variationValues.get(catpath + "*" + variationName1 + "*" + variationValue1) ?: getVariationValue(variationName1, variationValue1, catpath, catalog)
                            if (vv1)
                                t.variation1 = vv1

                            VariationValue vv2 = variationValues.get(catpath + "*" + variationName2 + "*" + variationValue2) ?: getVariationValue(variationName2, variationValue2, catpath, catalog)
                            if (vv2)
                                t.variation2 = vv2

                            VariationValue vv3 = variationValues.get(catpath + "*" + variationName3 + "*" + variationValue3) ?: getVariationValue(variationName3, variationValue3, catpath, catalog)
                            if (vv3)
                                t.variation3 = vv3


                            if (t.validate())
                                t.save(flush: false)
                            else {
                                t.errors.allErrors.each { println(it) }
                                return [errors: t.errors.allErrors, sheet: "sku", line: rownum]
                            }
                            rownum++
                        }
                        this.cleanUpGorm()
                        log.info("sku-worker-"+ index+" = " + rownum)
                    }
                }
            }
            futures.add(future)
        }
        futures.each {
            it.get()
        }
        impexDir.deleteDir()
        return [errors: [], sheet: "", line: -1]
    }

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        ThreadLocal<Map> propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
        propertyInstanceMap.get().clear()
    }

    @Transactional
    Integer importProducts(Map<String, Brand> brands, Map<String, Category> categories, Map<String, XSSFSheet> sheets, XSSFSheet prdSheet, Map<String, TaxRate> taxRates, Catalog catalog, File dateDir, Map<String, Product> products, int startLine, int countLines) {
        List<String[]> prodList = new ArrayList<>(countLines)
        log.info("Preloading products ...")
        for (int rownum = startLine; rownum < startLine + countLines; rownum++) {
            XSSFRow row
            row = prdSheet.getRow(rownum)
            if (row != null) {
                String cell
                cell = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                if (cell?.length() > 0) {
                    String catuuid = getFormulaCell(row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                    String catpath = getFormulaCell(row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString(), sheets)
                    String uuid = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString()
                    String externalCode = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString()
                    String code = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString()
                    String name = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString()
                    String xtype = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                    String price = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString()
                    String state = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString()
                    String description = row.getCell(9, Row.CREATE_NULL_AS_BLANK).toString()
                    String descriptionAsText = Jsoup.parse(description).text()
                    String sales = row.getCell(10, Row.CREATE_NULL_AS_BLANK).toString()
                    String displayStock = row.getCell(11, Row.CREATE_NULL_AS_BLANK).toString()
                    String calendar = row.getCell(12, Row.CREATE_NULL_AS_BLANK).toString()
                    String startDate = row.getCell(13, Row.CREATE_NULL_AS_BLANK).toString()
                    String stopDate = row.getCell(14, Row.CREATE_NULL_AS_BLANK).toString()
                    String startFeatDate = row.getCell(15, Row.CREATE_NULL_AS_BLANK).toString()
                    String stopFeatDate = row.getCell(16, Row.CREATE_NULL_AS_BLANK).toString()
                    String seo = row.getCell(17, Row.CREATE_NULL_AS_BLANK).toString()
                    String tags = row.getCell(18, Row.CREATE_NULL_AS_BLANK).toString()
                    String keywords = row.getCell(19, Row.CREATE_NULL_AS_BLANK).toString()
                    String brandName = row.getCell(20, Row.CREATE_NULL_AS_BLANK).toString()
                    String taxRateName = row.getCell(21, Row.CREATE_NULL_AS_BLANK).toString()
                    String dateCreated = row.getCell(22, Row.CREATE_NULL_AS_BLANK).toString()
                    String lastUpdated = row.getCell(23, Row.CREATE_NULL_AS_BLANK).toString()
                    String[] cols = [
                            catuuid, catpath, uuid, externalCode, code, name, xtype, price, state, description,
                            sales, displayStock, calendar, startDate, stopDate, startFeatDate, stopFeatDate, seo,
                            tags, keywords, brandName, taxRateName, dateCreated, lastUpdated
                    ]
                    prodList.add(cols)
                }
            }
        }
        log.info("Products preloaded")
        Map<String, Tag> tagMap = (new HashMap<>()).asSynchronized()

        countLines = prodList.size()
        int nbthreads = NBTHREADS - 1
        List<Future<Integer>> futures = new ArrayList<>(nbthreads + 1)
        Range<Integer> range = 0..nbthreads
        int countPerThread = countLines / (nbthreads + 1)
        range.each { index ->
            Future<Integer> future = callAsync {
                int startIndex = index * countPerThread
                log.info("product-worker-"+ index+" = " + startIndex)
                int rownum = startIndex
                int nbProductsToInsert
                if (index == nbthreads) {
                    nbProductsToInsert = countLines - countPerThread * nbthreads
                } else {
                    nbProductsToInsert = countPerThread
                }
                Product.withNewTransaction {
                    while (rownum < startIndex + nbProductsToInsert) {
                        int max = rownum + 100
                        max = max > startIndex + countPerThread ? startIndex + nbProductsToInsert : max
                        while (rownum < max) {
                            String[] cols = prodList.get(rownum)
                            String catuuid = cols[0]
                            String catpath = cols[1]
                            String uuid = cols[2]
                            String externalCode = cols[3]
                            String code = cols[4]
                            String name = cols[5]
                            String xtype = cols[6]
                            String price = cols[7]
                            String state = cols[8]
                            String description = cols[9]
                            String descriptionAsText = Jsoup.parse(description).text()
                            String sales = cols[10]
                            String displayStock = cols[11]
                            String calendar = cols[12]
                            String startDate = cols[13]
                            String stopDate = cols[14]
                            String startFeatDate = cols[15]
                            String stopFeatDate = cols[16]
                            String seo = cols[17]
                            String tags = cols[18]
                            String keywords = cols[19]
                            String brandName = cols[20]
                            String taxRateName = cols[21]
                            String dateCreated = cols[22]
                            String lastUpdated = cols[23]

                            Product p = new Product()
                            p.category = categories.get(catpath)
                            TaxRate tr = taxRates.get(taxRateName)
                            if (tr == null) {
                                tr = TaxRate.findByName(taxRateName)
                                taxRates.put(taxRateName, tr)
                            }
                            p.company = catalog.company
                            p.uuid = uuid ? uuid : UUID.randomUUID().toString()
                            p.externalCode = externalCode
                            p.code = code
                            p.name = name
                            p.xtype = ProductType.valueOf(xtype)
                            p.price = price.toDouble().toLong()
                            p.state = ProductState.valueOf(state)
                            p.description = description
                            p.descriptionAsText = descriptionAsText
                            p.nbSales = sales.toDouble().toLong()
                            p.stockDisplay = displayStock.toLowerCase().equals("false") ? false : true
                            p.calendarType = ProductCalendar.valueOf(calendar)
                            p.startDate = getCalendar(startDate)
                            p.stopDate = getCalendar(stopDate)
                            p.startFeatureDate = getCalendar(startFeatDate)
                            p.stopFeatureDate = getCalendar(stopFeatDate)
                            p.sanitizedName = seo.length() == 0 ? sanitizeUrlService.sanitizeWithDashes(name) : seo
                            p.keywords = keywords
                            p.taxRate = tr
                            p.dateCreated = parseDate(dateCreated)
                            p.lastUpdated = parseDate(lastUpdated, p.dateCreated)
                            if (brandName.length() > 0)
                                p.brand = brands.get(brandName) ?: Brand.findByNameAndCompany(brandName, catalog.company)

                            if (tags.size() > 0) {
                                tags.split(',').each {
                                    Tag tag = tagMap.get(it)
                                    if (tag == null) {
                                        tag = Tag.findByNameAndCompany(it, catalog.company)
                                        if (tag == null) {
                                            tag = new Tag(name: it, company: catalog.company)
                                            tag.save()
                                        }
                                    }
                                    p.addToTags(tag)
                                }
                            }
                            if (p.validate()) {
                                IperUtil.withAutoTimestampSuppression(p) {
                                    p.save(flush: false)
                                }
                                products.put(p.code, p)

                                File resDir = new File(dateDir, p.sanitizedName)
                                if (resDir.exists() && resDir.list().size() > 0) {
                                    File[] files = resDir.listFiles()
                                    int countRes = 1
                                    files.each { file ->
                                        Resource res = new Resource()
                                        res.code = ""
                                        res.description = ""
                                        res.name = file.getName()
                                        String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1)
                                        res.content = ""
                                        res.contentType = "image/$ext"
                                        res.deleted = false
                                        res.uploaded = true
                                        res.xtype = ResourceType.PICTURE
                                        res.active = true
                                        resService.uploadResource(catalog.company, res, file, res.contentType)
                                        Product2Resource product2Resource = new Product2Resource(
                                                montant: 0,
                                                product: p,
                                                resource: res,
                                                position: countRes++
                                        )
                                        if (product2Resource.validate()) {
                                            product2Resource.save(flush: false)
                                        }
                                    }
                                }
                            } else {
                                p.errors.allErrors.each { println(it) }
                                return [errors: p.errors.allErrors, sheet: "product", line: rownum]
                            }
                            rownum++
                        }
                        this.cleanUpGorm()
                        log.info("product-worker-"+ index+" = " + rownum)
                    }
                }
                return 0
            }
            futures.add(future)
        }
        futures.each {
            it.get()
        }
        return countLines
    }

    def Date parseDate(String date, Date d = new Date()) {
        try{
            new SimpleDateFormat("yyyy-MM-dd").parse(date)
        }
        catch(ParseException e){
            d
        }
    }
}
