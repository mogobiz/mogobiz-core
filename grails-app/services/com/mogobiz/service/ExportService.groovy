/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.domain.*
import com.mogobiz.utils.IperUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataValidationConstraint
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.XSSFDataValidation
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportService {
    CategoryService categoryService
    FeatureService featureService
    def grailsApplication

    final List<String> brandHeaders = ["uuid", "name", "website", "facebook", "twitter", "description", "hide"]
    final List<String> catHeaders = ["uuid", "external-code", "path", "position", "description", "keywords", "hide", "seo", "google", "deleted"]
    final List<String> featHeaders = ["category-uuid", "category-path", "product-uuid", "product-code", "uuid", "external-code", "domain", "name", "value", "hide"]
    final List<String> varHeaders = ["category-uuid", "category-path", "uuid", "external-code", "name", "google", "hide"]
    final List<String> varValHeaders = ["category-uuid", "category-path", "variation-uuid", "variation-name", "uuid", "external-code", "value", "google"]
    final List<String> prdHeaders = ["category-uuid", "category-path", "uuid", "external-code", "code", "name", "xtype", "price", "state", "description", "sales", "display-stock", "calendar", "start-date", "stop-date", "start-featured-date", "stop-featured-date", "seo", "tags", "keywords", "brand-name", "tax-rate", "date-created", "last-updated"]
    final List<String> prdPropHeaders = ["category-uuid", "category-path", "product-uuid", "product-code", "uuid", "name", "value"]
    final List<String> skuHeaders = ["category-uuid", "category-path", "product-uuid", "product-code", "uuid", "external-code", "sku", "name", "price", "min-order", "max-order", "sales", "start-date", "stop-date", "private", "remaining-stock", "unlimited-stock", "outsell-stock", "description", "availability-date", "google-gtin", "google-mpn", "variation-name-1", "variation-value-1", "variation-name-2", "variation-value-2", "variation-name-3", "variation-value-3"]
    final List<String> taxHeaders = ["uuid", "name", "country-code", "state-code", "rate", "active"]
    final List<String> shipHeaders = ["uuid", "country-code", "min-amount", "max-amount", "price"]
    final List<String> couponHeaders = ["uuid", "name", "code", "active", "number-of-uses", "start-date", "end-date", "catalog-wise", "for-sale", "description", "anonymous", "pastille", "consumed"]
    final List<String> reductionRuleHeaders = ["coupon-code", "uuid", "xtype", "quantity-min", "quantity-max", "discount", "xpurchased", "yoffered"]
    final List<String> couponUseHeaders = ["uuid", "code", "category-uuid", "product-uuid", "sku-uuid", "target-name"]

    List<String> toArray(Coupon it) {
        [it.uuid, it.name, it.code, it.active, it.numberOfUses, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.endDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.endDate.getTime()) : "", it.catalogWise, it.forSale, it.description, it.anonymous, it.pastille, it.consumed]
    }

    List<String> toArray(ReductionRule it, String couponCode) {
        [couponCode, it.uuid, it.xtype.name(), it.quantityMin?.toString(), it.quantityMax?.toString(), it.discount, it.xPurchased?.toString(), it.yOffered?.toString()]
    }

    List<String> toArray(ShippingRule it) {
        [it.uuid, it.countryCode, it.minAmount, it.maxAmount, it.price]
    }

    List<String> toArray(Brand it) {
        [it.uuid, it.name, it.website, it.facebooksite, it.twitter, it.description, it.hide]
    }

    List<String> toArray(Category it) {
        [it.uuid, it.externalCode, categoryService.path(it), it.position.toString(), it.description, it.keywords, it.hide, it.sanitizedName, it.googleCategory, it.deleted]
    }

    List<String> toArrayForCat(Feature it, int catRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, null, null, it.uuid, it.externalCode, it.domain, it.name, it.value, it.hide]
    }

    List<String> toArrayForPrd(Feature it, int catRowNum, int prdRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "product!C" + prdRowNum, "product!E" + prdRowNum, it.uuid, it.externalCode, it.domain, it.name, it.value?.indexOf("||||") >= 0 ? it.value.substring(it.value.indexOf("||||") + 4) : it.value, it.hide]
    }

    List<String> toArray(Variation it, int catRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, it.uuid, it.externalCode, it.name, it.googleVariationType, it.hide]
    }

    List<String> toArray(VariationValue it, int catRowNum, int varRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "variation!C" + varRowNum, "variation!E" + varRowNum, it.uuid, it.externalCode, it.value, it.googleVariationValue]
    }

    List<String> toArray(Product it, int catRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, it.uuid, it.externalCode ?: "", it.code, it.name, it.xtype, it.price, it.state, it.description ?: "", it.nbSales, it.stockDisplay, it.calendarType, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "", it.startFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startFeatureDate.getTime()) : "", it.stopFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopFeatureDate.getTime()) : "", it.sanitizedName, it.tags.collect {
            it.name
        }.join(","), it.keywords ?: "", it.brand ? it.brand.name : "", it.taxRate ? it.taxRate.name : "", new SimpleDateFormat("yyyy-MM-dd").format(it.dateCreated), new SimpleDateFormat("yyyy-MM-dd").format(it.lastUpdated)]
    }

    List<String> toArray(TicketType it, int catRowNum, int prdRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "product!C" + prdRowNum, "product!E" + prdRowNum, it.uuid, it.externalCode, it.sku, it.name, it.price, it.minOrder, it.maxOrder, it.nbSales, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "", it.xprivate, it.stock ? it.stock.stock : "", it.stock ? it.stock.stockUnlimited : "", it.stock ? it.stock.stockOutSelling : "", it.description, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : "", it.gtin, it.mpn, it.variation1?.variation?.name, it.variation1?.value, it.variation2?.variation?.name, it.variation2?.value, it.variation3?.variation?.name, it.variation3?.value]
    }

    List<String> toArray(ProductProperty it, int catRowNum, int prdRowNum) {
        ["category!A" + catRowNum, "category!C" + catRowNum, "product!C" + prdRowNum, "product!E" + prdRowNum, it.uuid, it.name, it.value]
    }

    List<String> toArray(LocalTaxRate it, String name) {
        [it.uuid, name, it.countryCode ?: "", it.stateCode ?: "", it.rate, it.active]
    }


    private static def boolValidateCell(XSSFSheet sheet, List<Integer> cellNums) {
        //sheet.protectSheet("")
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(["TRUE", "FALSE"] as String[]);
        cellNums.each {
            CellRangeAddressList addressList = new CellRangeAddressList(1, 65000, it, it);
            XSSFDataValidation dataValidation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
            dataValidation.setShowErrorBox(true);
            sheet.addValidationData(dataValidation)
        }

    }

    File export(long catalogId, File xlsFile, File zipFile, Category parent = null, boolean deleted = false) {
        int catRownum = 0
        int catfeatRownum = 0
        int varRownum = 0
        int varValRownum = 0
        int prdRownum = 0
        int prdFeatRownum = 0
        int skuRownum = 0
        int taxRownum = 0
        int shipRownum = 0
        int prdPropRownum = 0

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet brandSheet = workbook.createSheet("brand");
        int brandRownum = 0
        Row brandRow = brandSheet.createRow(brandRownum++)
        int brandCellnum = 0
        brandHeaders.each {
            Cell brandCell = brandRow.createCell(brandCellnum++)
            brandCell.setCellValue(it)
        }

        XSSFSheet catSheet = workbook.createSheet("category");
//        catSheet.protectSheet("")
//        boolValidateCell(catSheet, [6, 9])


        Row catRow = catSheet.createRow(catRownum++)
        int catCellnum = 0
        catHeaders.each {
            Cell catCell = catRow.createCell(catCellnum++)
            catCell.setCellValue(it)
        }

        XSSFSheet catFeatSheet = workbook.createSheet("cat-feature");
//        catFeatSheet.protectSheet("")
//        boolValidateCell(catFeatSheet, [9])

        Row catFeatRow = catFeatSheet.createRow(catfeatRownum++)
        int catFeatCellnum = 0
        featHeaders.each {
            Cell catFeatCell = catFeatRow.createCell(catFeatCellnum++)
            catFeatCell.setCellValue(it)
        }

        XSSFSheet varSheet = workbook.createSheet("variation");
//        varSheet.protectSheet("")
//        boolValidateCell(varSheet, [6])
        Row varRow = varSheet.createRow(varRownum++)
        int varCellnum = 0
        varHeaders.each {
            Cell varCell = varRow.createCell(varCellnum++)
            varCell.setCellValue(it)
        }

        XSSFSheet varValSheet = workbook.createSheet("variation-value");
//        varValSheet.protectSheet("")
        int varValCellnum = 0
        Row varValRow = varValSheet.createRow(varValRownum++)
        varValHeaders.each {
            Cell varValCell = varValRow.createCell(varValCellnum++)
            varValCell.setCellValue(it)
        }

        XSSFSheet prdSheet = workbook.createSheet("product");
//        prdSheet.protectSheet("")
        Row prdRow = prdSheet.createRow(prdRownum++)
        int prdCellnum = 0
        prdHeaders.each {
            Cell prdCell = prdRow.createCell(prdCellnum++)
            prdCell.setCellValue(it)
        }

        XSSFSheet prdFeatSheet = workbook.createSheet("product-feature");
//        prdFeatSheet.protectSheet("")
        Row prdFeatRow = prdFeatSheet.createRow(prdFeatRownum++)
        int prdFeatCellnum = 0
        featHeaders.each {
            Cell prdFeatCell = prdFeatRow.createCell(prdFeatCellnum++)
            prdFeatCell.setCellValue(it)
        }

        XSSFSheet prdPropSheet = workbook.createSheet("product-property");
//        prdPropSheet.protectSheet("")
        Row prdPropRow = prdPropSheet.createRow(prdPropRownum++)
        int prdPropCellnum = 0
        prdPropHeaders.each {
            Cell prdPropCell = prdPropRow.createCell(prdPropCellnum++)
            prdPropCell.setCellValue(it)
        }

        XSSFSheet skuSheet = workbook.createSheet("sku");
//        skuSheet.protectSheet("")
        Row skuRow = skuSheet.createRow(skuRownum++)
        int skuCellnum = 0
        skuHeaders.each {
            Cell skuCell = skuRow.createCell(skuCellnum++)
            skuCell.setCellValue(it)
        }

        XSSFSheet taxRateSheet = workbook.createSheet("taxrate");
        Row taxRow = taxRateSheet.createRow(0)
        int taxCellnum = 0
        taxHeaders.each {
            Cell taxCell = taxRow.createCell(taxCellnum++)
            taxCell.setCellValue(it)
        }

        XSSFSheet shipSheet = workbook.createSheet("shipping");
        Row shipRow = shipSheet.createRow(0)
        int shipCellnum = 0
        shipHeaders.each {
            Cell shipCell = shipRow.createCell(shipCellnum++)
            shipCell.setCellValue(it)
        }

        XSSFSheet couponSheet = workbook.createSheet("coupon");
        Row couponRow = couponSheet.createRow(0)
        int couponCellnum = 0
        couponHeaders.each {
            Cell couponCell = couponRow.createCell(couponCellnum++)
            couponCell.setCellValue(it)
        }

        XSSFSheet reductionSheet = workbook.createSheet("coupon-rule");
        Row reductionRow = reductionSheet.createRow(0)
        int reductionCellnum = 0
        reductionRuleHeaders.each {
            Cell reductionRuleCell = reductionRow.createCell(reductionCellnum++)
            reductionRuleCell.setCellValue(it)
        }

        XSSFSheet couponUseSheet = workbook.createSheet("coupon-use");
        Row couponUseRow = couponUseSheet.createRow(0)
        int couponUseCellnum = 0
        couponUseHeaders.each {
            Cell couponUseCell = couponUseRow.createCell(couponUseCellnum++)
            couponUseCell.setCellValue(it)
        }

        List<Brand> brands = Brand.findAllByCompany(Catalog.get(catalogId).company)
        brandRownum = 1
        final String resourcesPath = grailsApplication.config.resources.path
        final String companyCode = Catalog.get(catalogId).company.code
        File outDir = xlsFile.getParentFile()

        Path brandLogosDir = Paths.get(outDir.getAbsolutePath(), "__brandlogos__")
        brandLogosDir.toFile().mkdirs()
        List<String> brandLogos = []
        brands.each {brand ->
            brandCellnum = 0
            Row branRow = brandSheet.createRow(brandRownum++)
            toArray(brand).each {
                Cell brandCell = branRow.createCell(brandCellnum++)
                brandCell.setCellValue(it)
            }
            final String brandId = brand.id.toString()
            String brandLogodPath = "$resourcesPath/brands/logos/$companyCode"
            Path resUrl = Paths.get(brandLogodPath)
            if (Files.exists(resUrl)) {
                new File(brandLogodPath).listFiles(new FilenameFilter() {
                    @Override
                    boolean accept(File f, String name) {
                        return name.startsWith(brandId)
                    }
                }).each {
                    try {
                        String brandNameLogo = IperUtil.normalizeName(brand.name)
                        final name = it.getName().replace(brandId, brandNameLogo)
                        Files.copy(it.toPath(), Paths.get(brandLogosDir.toString(), name))
                        brandLogos.add(name)
                    }
                    catch (IOException ioe) {
                        log.error(resUrl + "->" + outDir.getAbsolutePath() + "/" + brand.name)
                        ioe.printStackTrace()
                    }
                }
            }
        }
        new File(brandLogosDir.toFile(), '__brandlogos__').text = brandLogos.join('\t')

        taxRownum = 1
        List<TaxRate> taxRates = TaxRate.findAllByCompany(Catalog.get(catalogId).company)
        taxRates.each { tax ->
            tax.localTaxRates.each { local ->
                taxCellnum = 0
                taxRow = taxRateSheet.createRow(taxRownum++)
                toArray(local, tax.name).each {
                    Cell taxCell = taxRow.createCell(taxCellnum++)
                    taxCell.setCellValue(it)
                }
            }
        }

        shipRownum = 1
        List<ShippingRule> shippingRules = ShippingRule.findAllByCompany(Catalog.get(catalogId).company)
        shippingRules.each { shippingRule ->
            shipCellnum = 0
            shipRow = shipSheet.createRow(shipRownum++)
            toArray(shippingRule).each {
                Cell shipCell = shipRow.createCell(shipCellnum++)
                shipCell.setCellValue(it)
            }
        }

        int couponUseRownum = 1
        int reductionRownum = 1
        int couponRownum = 1
        List<Coupon> coupons = Coupon.findAllByCompany(Catalog.get(catalogId).company)
        coupons.each { coupon ->
            couponCellnum = 0
            couponRow = couponSheet.createRow(couponRownum++)
            toArray(coupon).each {
                Cell couponCell = couponRow.createCell(couponCellnum++)
                couponCell.setCellValue(it)
            }

            coupon.rules.each { rule ->
                int ruleCellnum = 0
                reductionRow = reductionSheet.createRow(reductionRownum++)
                toArray(rule, coupon.code).each {
                    Cell reductionCell = reductionRow.createCell(ruleCellnum++)
                    reductionCell.setCellValue(it)
                }
            }

            coupon.categories?.each { category ->
                couponUseCellnum = 0
                couponUseRow = couponUseSheet.createRow(couponUseRownum++)
                Cell couponUseCell = couponUseRow.createCell(0)
                couponUseCell.setCellValue(coupon.uuid)
                couponUseCell = couponUseRow.createCell(1)
                couponUseCell.setCellValue(coupon.code)
                couponUseCell = couponUseRow.createCell(2)
                couponUseCell.setCellValue(category.uuid)
                couponUseCell = couponUseRow.createCell(3)
                couponUseCell.setCellValue("")
                couponUseCell = couponUseRow.createCell(4)
                couponUseCell.setCellValue("")
                couponUseCell = couponUseRow.createCell(5)
                couponUseCell.setCellValue(category.name)
            }

            coupon.products?.each { product ->
                couponUseCellnum = 0
                couponUseRow = couponUseSheet.createRow(couponUseRownum++)
                Cell couponUseCell = couponUseRow.createCell(0)
                couponUseCell.setCellValue(coupon.uuid)
                couponUseCell = couponUseRow.createCell(1)
                couponUseCell.setCellValue(coupon.code)
                couponUseCell = couponUseRow.createCell(2)
                couponUseCell.setCellValue("")
                couponUseCell = couponUseRow.createCell(3)
                couponUseCell.setCellValue(product.uuid)
                couponUseCell = couponUseRow.createCell(4)
                couponUseCell.setCellValue("")
                couponUseCell = couponUseRow.createCell(5)
                couponUseCell.setCellValue(product.name)
            }

            coupon.ticketTypes?.each { sku ->
                couponUseCellnum = 0
                couponUseRow = couponUseSheet.createRow(couponUseRownum++)
                Cell couponUseCell = couponUseRow.createCell(0)
                couponUseCell.setCellValue(coupon.uuid)
                couponUseCell = couponUseRow.createCell(1)
                couponUseCell.setCellValue(coupon.code)
                couponUseCell = couponUseRow.createCell(2)
                couponUseCell.setCellValue("")
                couponUseCell = couponUseRow.createCell(3)
                couponUseCell.setCellValue("")
                couponUseCell = couponUseRow.createCell(4)
                couponUseCell.setCellValue(sku.uuid)
                couponUseCell = couponUseRow.createCell(5)
                couponUseCell.setCellValue(sku.name)
            }
        }

        doExport(catalogId, workbook, parent, deleted, [catRownum, catfeatRownum, varRownum, varValRownum, prdRownum, prdFeatRownum, skuRownum, prdPropRownum], outDir)
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(xlsFile);
        workbook.write(out);
        out.close();
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        addFolderToZip("", outDir.getAbsolutePath(), zip)
        zip.close()
        return zipFile
    }

    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder)
        /*
         * check the empty folder
         */
        if (folder.list().length == 0) {
            addFileToZip(path, srcFolder, zip, true)
        } else {
            folder.listFiles().sort { it.lastModified() }.each { File file ->
                def srcFile = srcFolder + File.separator + file.getName()
                if (path.equals("")) {
                    addFileToZip(folder.name, srcFile, zip, false)
                } else {
                    addFileToZip(path + File.separator + folder.name, srcFile, zip, false)
                }
            }
        }
    }

    /*
     * recursively add files to the zip files
     */

    private void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws Exception {
        File f = new File(srcFile)
        if (flag) {
            /*
             * add empty folder to the Zip file
             */
            zip.putNextEntry(new ZipEntry(path + File.separator + f.name + "/")) // Suffix elpty dirs by '/'
        } else {
            if (f.isDirectory()) {
                addFolderToZip(path, srcFile, zip)
            } else {
                byte[] buffer = new byte[1024]
                int len
                FileInputStream is = new FileInputStream(srcFile)
                zip.putNextEntry(new ZipEntry(path + File.separator + f.name))
                while ((len = is.read(buffer)) > 0) {
                    zip.write(buffer, 0, len)
                }
                is.close()
            }
        }
    }

    public File getExportDir(String now) {
        String impexPath = grailsApplication.config.impex.path
        if (!impexPath)
            impexPath = System.getProperty("java.io.tmpdir")
        File impexDir = new File(new File(impexPath), now)
        impexDir.mkdirs()
        return impexDir
    }


    void doExport(long catalogId, XSSFWorkbook workbook, Category parent, boolean deleted, ArrayList<Integer> rownums, File exportDir) {
        String resourcesPath = grailsApplication.config.resources.path
        int catRownum = rownums[0]
        int catfeatRownum = rownums[1]
        int varRownum = rownums[2]
        int varValRownum = rownums[3]
        int prdRownum = rownums[4]
        int prdFeatRownum = rownums[5]
        int skuRownum = rownums[6]
        int prdPropRownum = rownums[7]

        XSSFSheet catSheet = workbook.getSheet("category");
        XSSFSheet catFeatSheet = workbook.getSheet("cat-feature");
        XSSFSheet varSheet = workbook.getSheet("variation");
        XSSFSheet varValSheet = workbook.getSheet("variation-value");
        XSSFSheet prdSheet = workbook.getSheet("product");
        XSSFSheet prdPropSheet = workbook.getSheet("product-property");
        XSSFSheet prdFeatSheet = workbook.getSheet("product-feature");
        XSSFSheet skuSheet = workbook.getSheet("sku");


        List<Category> cats = Category.findAllByCatalogAndParent(Catalog.get(catalogId), parent, deleted)
        cats.each {
            int catCellnum = 0
            Row catRow = catSheet.createRow(catRownum++)
            toArray(it).each {
                Cell catCell = catRow.createCell(catCellnum++)
                if (catCellnum <= 4) {
                    catCell.setCellValue(it)
                } else {
                    catCell.setCellValue(it)
                }
            }

            List<Feature> features = featureService.getCategoryFeatures(it.id, false)
            features.each {
                int catFeatCellnum = 0
                Row catFeatRow = catFeatSheet.createRow(catfeatRownum++)
                toArrayForCat(it, catRownum).each {
                    Cell catFeatCell = catFeatRow.createCell(catFeatCellnum++)
                    if (catFeatCellnum <= 2) {
                        catFeatCell.setCellFormula(it)
                    } else {
                        catFeatCell.setCellValue(it)

                    }
                }
            }

            List<Variation> variations = Variation.findAllByCategory(it, [sort: 'position', order: 'asc'])
            variations.each { varit ->
                int varCellnum = 0
                Row varRow = varSheet.createRow(varRownum++)
                toArray(varit, catRownum).each {
                    Cell varCell = varRow.createCell(varCellnum++)
                    if (varCellnum <= 2)
                        varCell.setCellFormula(it)
                    else {
                        varCell.setCellValue(it)
                    }
                }
                List<VariationValue> values = VariationValue.findAllByVariation(varit)
                values.each {
                    int varValCellnum = 0
                    Row varValRow = varValSheet.createRow(varValRownum++)
                    toArray(it, catRownum, varRownum).each {
                        Cell varValCell = varValRow.createCell(varValCellnum++)
                        if (varValCellnum <= 4)
                            varValCell.setCellFormula(it)
                        else {
                            varValCell.setCellValue(it)
                        }
                    }
                }
            }

            List<Product> products = Product.findAllByCategoryAndDeleted(it, deleted)
            products.each { prd ->
                int prdCellnum = 0
                Row prdRow = prdSheet.createRow(prdRownum++)
                log.debug(prd)
                log.debug("-->" + prdRownum)
                toArray(prd, catRownum).each { col ->
                    Cell prdCell = prdRow.createCell(prdCellnum++)
                    if (prdCellnum <= 2)
                        prdCell.setCellFormula(col)
                    else {
                        prdCell.setCellValue(col.toString())
                    }
                }

                (new File(exportDir, prd.sanitizedName)).mkdirs()
                List<Product2Resource> prdres = Product2Resource.findAllByProduct(prd, [sort: "position", order: "asc"])
                prdres.each {
                    Path resUrl = Paths.get(resourcesPath + (IperUtil.normalizeSeparator(it.resource.url) - resourcesPath))
                    try {
                        Files.copy(resUrl, Paths.get(exportDir.getAbsolutePath(), prd.sanitizedName, it.resource.name))
                    }
                    catch (IOException ioe) {
                        // ioe.printStackTrace()
                    }
                }

                List<Feature> pfeatures = featureService.getProductFeatures(prd.id, false)
                pfeatures.each {
                    int prdFeatCellnum = 0
                    Row prdFeatRow = prdFeatSheet.createRow(prdFeatRownum++)
                    toArrayForPrd(it, catRownum, prdRownum).each {
                        Cell prdFeatCell = prdFeatRow.createCell(prdFeatCellnum++)
                        prdFeatCell.setCellValue(it)
                        if (prdFeatCellnum <= 4)
                            prdFeatCell.setCellFormula(it)
                        else {
                            prdFeatCell.setCellValue(it)
                        }
                    }
                }

                List<ProductProperty> pproperties = ProductProperty.findAllByProduct(Product.get(prd.id))
                pproperties.each {
                    int prdPropCellnum = 0
                    Row prdPropRow = prdPropSheet.createRow(prdPropRownum++)
                    toArray(it, catRownum, prdRownum).each {
                        Cell prdPropCell = prdPropRow.createCell(prdPropCellnum++)
                        prdPropCell.setCellValue(it)
                        if (prdPropCellnum <= 4)
                            prdPropCell.setCellFormula(it)
                        else {
                            prdPropCell.setCellValue(it)
                        }
                    }
                }

                List<TicketType> ticketTypes = TicketType.findAllByProduct(prd)
                ticketTypes.each {
                    int skuCellnum = 0
                    Row skuRow = skuSheet.createRow(skuRownum++)
                    toArray(it, catRownum, prdRownum).each {
                        Cell skuCell = skuRow.createCell(skuCellnum++)
                        if (skuCellnum <= 4)
                            skuCell.setCellFormula(it)
                        else {
                            skuCell.setCellValue(it)
                        }
                    }
                }
            }

            rownums[0] = catRownum
            rownums[1] = catfeatRownum
            rownums[2] = varRownum
            rownums[3] = varValRownum
            rownums[4] = prdRownum
            rownums[5] = prdFeatRownum
            rownums[6] = skuRownum
            rownums[7] = prdPropRownum
            log.info(prdRownum + " products")
            doExport(catalogId, workbook, it, deleted, rownums, exportDir)
            catRownum = rownums[0]
            catfeatRownum = rownums[1]
            varRownum = rownums[2]
            varValRownum = rownums[3]
            prdRownum = rownums[4]
            prdFeatRownum = rownums[5]
            skuRownum = rownums[6]
            prdPropRownum = rownums[7]
        }
    }
}
