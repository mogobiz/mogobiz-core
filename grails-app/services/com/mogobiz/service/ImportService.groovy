package com.mogobiz.service

import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Brand
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.FeatureValue
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Product2Resource
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.ProductProperty
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.ProductType
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.store.domain.Stock
import com.mogobiz.store.domain.Tag
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.Variation
import com.mogobiz.store.domain.VariationValue
import com.mogobiz.utils.IperUtil
import com.mogobiz.utils.ZipFileUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hibernate.SessionFactory
import org.jsoup.Jsoup
import grails.transaction.Transactional
import java.text.SimpleDateFormat
import java.util.zip.ZipFile

@Transactional
class ImportService {

    SanitizeUrlService sanitizeUrlService
    ResService resService
    def grailsApplication
    SessionFactory sessionFactory
    ThreadLocal<Map> propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

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
        log.info("IMPORT STARTED")
        String now = new SimpleDateFormat("yyyy-MM-dd.HHmmss").format(new Date())
        File impexDir = getImportDir(now)
        ZipFileUtil.unzipFileIntoDirectory(zipFile, impexDir)
        File dateDir = impexDir.listFiles().find {
            it.isDirectory()
        }
        File inputFile = new File(dateDir, "mogobiz.xlsx")

        XSSFWorkbook workbook = new XSSFWorkbook(inputFile.getAbsolutePath())
        XSSFSheet brandSheet = workbook.getSheet("brand");
        XSSFSheet catSheet = workbook.getSheet("category");
        XSSFSheet catFeatSheet = workbook.getSheet("cat-feature");
        XSSFSheet varSheet = workbook.getSheet("variation");
        XSSFSheet varValSheet = workbook.getSheet("variation-value");
        XSSFSheet prdSheet = workbook.getSheet("product");
        XSSFSheet prdFeatSheet = workbook.getSheet("product-feature");
        XSSFSheet prdPropSheet = workbook.getSheet("product-property");
        XSSFSheet skuSheet = workbook.getSheet("sku");
        Map<String, XSSFSheet> sheets = [
                'category'       : catSheet,
                'cat-feature'    : catFeatSheet,
                'variation'      : varSheet,
                'variation-value': varValSheet,
                'product'        : prdSheet,
                'product-feature': prdFeatSheet,
                'sku'            : skuSheet
        ]

        // Brand
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
                        if (b.validate())
                            b.save(flush: true)
                        else {
                            b.errors.allErrors.each { println(it) }
                            return [errors: b.errors.allErrors, sheet: "cat-feature", line: rownum]
                        }
                    }
                }
            }
        }


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
                            if (cat.validate())
                                cat.save(flush: true)
                            else {
                                cat.errors.allErrors.each { println(it) }
                                return [errors: cat.errors.allErrors, sheet: "category", line: rownum]
                            }
                        }
                    }
                }
            }
        }

        // Cat Features
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
                    f.category = getCategoryFromPath(catpath, catalog)
                    f.externalCode = externalCode
                    f.domain = domain
                    f.name = name
                    f.value = value
                    f.hide = hide
                    if (f.validate())
                        f.save(flush: true)
                    else {
                        f.errors.allErrors.each { println(it) }
                        return [errors: f.errors.allErrors, sheet: "cat-feature", line: rownum]
                    }

                }
            }
        }

        // Cat Variations
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
                    v.category = getCategoryFromPath(catpath, catalog)
                    v.externalCode = externalCode
                    v.name = name
                    v.googleVariationType = google
                    v.position = row.getRowNum()
                    v.hide = hide.equalsIgnoreCase("true")
                    if (v.validate())
                        v.save(flush: true)
                    else {
                        v.errors.allErrors.each { println(it) }
                        return [errors: v.errors.allErrors, sheet: "variation", line: rownum]
                    }

                }
            }
        }

        // Variation Values
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
                    v.variation = Variation.findByNameAndCategory(varname, getCategoryFromPath(catpath, catalog))
                    if (v.variation == null) {
                        println(varname + "->" + catpath)
                    }
                    v.externalCode = externalCode
                    v.value = value
                    v.googleVariationValue = google
                    v.position = row.getRowNum()
                    if (v.validate())
                        v.save(flush: true)
                    else {
                        v.errors.allErrors.each { println(it) }
                        return [errors: v.errors.allErrors, sheet: "variation-value", line: rownum]
                    }

                }
            }
        }

        int countInserts = 0;
        // Products
        for (int rownum = 1; rownum < prdSheet.getPhysicalNumberOfRows(); rownum++) {
            XSSFRow row = prdSheet.getRow(rownum)
            if (row != null) {
                String cell = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString()
                if (cell.length() > 0) {
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
                    String dateCreated = row.getCell(21, Row.CREATE_NULL_AS_BLANK).toString()
                    String lastUpdated = row.getCell(22, Row.CREATE_NULL_AS_BLANK).toString()

                    Product p = new Product()
                    p.category = getCategoryFromPath(catpath, catalog)
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
                    p.dateCreated = new SimpleDateFormat("yyyy-MM-dd").parse(dateCreated)
                    p.lastUpdated = new SimpleDateFormat("yyyy-MM-dd").parse(lastUpdated)
                    if (brandName.length() > 0)
                        p.brand = Brand.findByNameAndCompany(brandName, catalog.company)

                    if (tags.size() > 0) {
                        tags.split(',').each {
                            Tag tag = Tag.findByNameAndCompany(it, catalog.company)
                            if (tag == null) {
                                tag = new Tag(name: it, company: catalog.company)
                                tag.save()
                            }
                            p.addToTags(tag)
                        }
                    }
                    if (p.validate()) {
                        IperUtil.withAutoTimestampSuppression(p) {
                            p.save(flush: false)
                        }
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
                        countInserts++;
                        if (countInserts % 100 == 0) {
                            log.info(countInserts)
                            this.cleanUpGorm()
                        }
                    } else {
                        p.errors.allErrors.each { println(it) }
                        return [errors: p.errors.allErrors, sheet: "product", line: rownum]
                    }
                }
            }
        }

        // Product Features
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
                    Product product = Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and d.id = :catalog and p.code = :code", [catalog: catalog.id, code: prdcode]).get(0)
                    boolean created = false
                    if (uuid) {
                        Feature feat = Feature.findByUuid(uuid)
                        if (feat?.category != null) {
                            FeatureValue featVal = new FeatureValue(value:value, product: product, feature: feat)
                            featVal.save(flush: false)
                            created = true
                        }
                    }
                    if (!created) {
                        Feature f = new Feature()
                        f.uuid = uuid ? uuid : UUID.randomUUID().toString()
                        f.product = product
                        f.externalCode = externalCode
                        f.domain = domain
                        f.name = name
                        f.value = value
                        f.hide = hide
                        if (f.validate()) {
                            f.save(flush: false)
                        }
                        else {
                            f.errors.allErrors.each { println(it) }
                            return [errors: f.errors.allErrors, sheet: "product-feature", line: rownum]
                        }
                    }
                    countInserts++;
                    if (countInserts % 100 == 0) {
                        log.info(countInserts)
                        this.cleanUpGorm()
                    }
                }
            }
        }

        // Product Properties
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
                    Category category = getCategoryFromPath(catpath, catalog)
                    pp.uuid = uuid ? uuid : UUID.randomUUID().toString()
                    pp.product = Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and d.id = :catalog and p.code = :code and c.id = :category", [catalog: catalog.id, code: prdcode, category: category.id]).get(0)
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
                        log.info(countInserts)
                        this.cleanUpGorm()
                    }
                }
            }
        }

        // SKU
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


                    TicketType t = new TicketType()
                    t.uuid = uuid ? uuid : UUID.randomUUID().toString()
                    t.product = Product.executeQuery("select p from Product p, Category c, Catalog d where p.category = c and c.catalog = d and d.id = :catalog and p.code = :code", [catalog: catalog.id, code: prdcode]).get(0)
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
                    VariationValue vv1 = getVariationValue(variationName1, variationValue1, catpath, catalog)
                    if (vv1)
                        t.variation1 = vv1

                    VariationValue vv2 = getVariationValue(variationName2, variationValue2, catpath, catalog)
                    if (vv2)
                        t.variation2 = vv2

                    VariationValue vv3 = getVariationValue(variationName3, variationValue3, catpath, catalog)
                    if (vv3)
                        t.variation3 = vv3


                    if (t.validate())
                        t.save(flush: false)
                    else {
                        t.errors.allErrors.each { println(it) }
                        return [errors: t.errors.allErrors, sheet: "sku", line: rownum]
                    }
                    countInserts++;
                    if (countInserts % 100 == 0) {
                        log.info(countInserts)
                        this.cleanUpGorm()
                    }
                }
            }
        }
        impexDir.deleteDir()
        log.info("IMPORT FINISHED")
        return [errors: [], sheet: "", line: -1]
    }
    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

}

