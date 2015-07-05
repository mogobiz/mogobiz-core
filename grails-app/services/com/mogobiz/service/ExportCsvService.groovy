package com.mogobiz.service

import com.mogobiz.store.domain.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportCsvService {
    CategoryService categoryService
    FeatureService featureService
    def grailsApplication

    final List<String> brandHeaders = ["uuid", "name", "website", "facebook", "twitter", "description", "hide"]
    final List<String> catHeaders = ["uuid", "external-code", "path", "description", "keywords", "hide", "seo", "google", "deleted"]
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
        [it.uuid, it.externalCode, categoryService.path(it), it.description, it.keywords, it.hide, it.sanitizedName, it.googleCategory, it.deleted]
    }

    List<String> toArrayForCat(Feature it) {
        [it.categoryId + "", null, it.uuid, it.externalCode, it.domain, it.name, it.value, it.hide]
    }

    List<String> toArrayForPrd(Feature it) {
        [null, it.productId + "", it.uuid, it.externalCode, it.domain, it.name, it.value?.indexOf("||||") >= 0 ? it.value.substring(it.value.indexOf("||||") + 4) : it.value, it.hide]
    }

    List<String> toArray(Variation it) {
        [it.categoryId + "", it.uuid, it.externalCode, it.name, it.googleVariationType, it.hide]
    }

    List<String> toArray(VariationValue it) {
        [it.variationId + "", it.uuid, it.externalCode, it.value, it.googleVariationValue]
    }

    List<String> toArray(Product it) {
        [it.categoryId + "", it.uuid, it.externalCode ?: "", it.code, it.name, it.xtype, it.price, it.state, it.description ?: "", it.nbSales, it.stockDisplay, it.calendarType, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "", it.startFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startFeatureDate.getTime()) : "", it.stopFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopFeatureDate.getTime()) : "", it.sanitizedName, it.tags.collect {
            it.name
        }.join(","), it.keywords ?: "", it.brand ? it.brand.name : "", it.taxRate.name, new SimpleDateFormat("yyyy-MM-dd").format(it.dateCreated), new SimpleDateFormat("yyyy-MM-dd").format(it.lastUpdated)]
    }

    List<String> toArray(TicketType it) {
        [it.productId, it.uuid, it.externalCode, it.sku, it.name, it.price, it.minOrder, it.maxOrder, it.nbSales, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : "", it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : "", it.xprivate, it.stock ? it.stock.stock : "", it.stock ? it.stock.stockUnlimited : "", it.stock ? it.stock.stockOutSelling : "", it.description, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : "", it.gtin, it.mpn, it.variation1?.variation?.name, it.variation1?.value, it.variation2?.variation?.name, it.variation2?.value, it.variation3?.variation?.name, it.variation3?.value]
    }

    List<String> toArray(ProductProperty it, int catRowNum, int prdRowNum) {
        [it.productId, it.uuid, it.name, it.value]
    }

    List<String> toArray(LocalTaxRate it, String name) {
        [it.uuid, name, it.countryCode ?: "", it.stateCode ?: "", it.rate, it.active]
    }

    private File createFile(File directory, String name, List<String> headers) {
        File file = new File(directory, name)
        file << headers.join("\t") << lineSeparator
        return file
    }

    String lineSeparator = System.getProperty("line.separator")

    File export(long catalogId, Category parent = null, boolean deleted = false) {
        String now = new SimpleDateFormat("yyyy-MM-dd.HHmmss").format(new Date())
        File outDir = getExportDir(now)
        File zipFile = new File(outDir.getParentFile(), "mogobiz-${now}.zip")
        File brandFile = createFile(outDir, "brand.csv", brandHeaders)
        File categoryFile = createFile(outDir, "category.csv", catHeaders)
        File catFeatureFile = createFile(outDir, "cat-feature.csv", featHeaders)
        File varFile = createFile(outDir, "variation.csv", varHeaders)
        File variationValueFile = createFile(outDir, "variation-value.csv", varValHeaders)
        File prodFile = createFile(outDir, "product.csv", prdHeaders)
        File prodFeatureFile = createFile(outDir, "product-feature.csv", featHeaders)
        File prodPropertyFile = createFile(outDir, "product-property.csv", prdPropHeaders)
        File skuFile = createFile(outDir, "sku.csv", skuHeaders)
        File taxrateFile = createFile(outDir, "taxrate.csv", taxHeaders)
        File shippingFile = createFile(outDir, "shipping.csv", shipHeaders)
        File couponFile = createFile(outDir, "coupon.csv", couponHeaders)
        File couponRuleFile = createFile(outDir, "coupon-rule.csv", reductionRuleHeaders)
        File couponUseFile = createFile(outDir, "coupon-use.csv", couponUseHeaders)


        List<Brand> brands = Brand.findAllByCompany(Catalog.get(catalogId).company)
        brands.each {
            brandFile << toArray(it).join("\t") << lineSeparator
        }

        List<TaxRate> taxRates = TaxRate.findAllByCompany(Catalog.get(catalogId).company)
        taxRates.each { tax ->
            tax.localTaxRates.each { local ->
                taxrateFile << toArray(local, tax.name).join("\t") << lineSeparator
            }
        }

        List<ShippingRule> shippingRules = ShippingRule.findAllByCompany(Catalog.get(catalogId).company)
        shippingRules.each { shippingRule ->
            shippingFile << toArray(shippingRule).join("\t") << lineSeparator
        }

        List<Coupon> coupons = Coupon.findAllByCompany(Catalog.get(catalogId).company)
        coupons.each { coupon ->
            couponFile << toArray(coupon).join("\t") << lineSeparator
            coupon.rules.each { rule ->
                couponRuleFile << toArray(rule, coupon.code).join("\t") << lineSeparator
            }

            coupon.categories?.each { category ->
                couponUseFile << coupon.uuid << "\t" << coupon.code << "\t" << category.uuid << "\t" << "" << "\t" << "" << "\t" << category.name
            }

            coupon.products?.each { product ->
                couponUseFile << coupon.uuid << "\t" << coupon.code << "\t" << "" << "\t" << product.uuid << "\t" << "" << "\t" << product.name
            }

            coupon.ticketTypes?.each { sku ->
                couponUseFile << coupon.uuid << "\t" << coupon.code << "\t" << "" << "\t" << "" << "\t" << sku.uuid << "\t" << sku.name
            }
        }

        doExport(catalogId, parent, deleted, categoryFile, catFeatureFile, varFile, variationValueFile, prodFile, prodFeatureFile, skuFile, prodPropertyFile, outDir)
        //Write the workbook in file system
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


    void doExport(long catalogId, Category parent, boolean deleted, File categoryFile, File catFeatureFile, File varFile, File variationValueFile, File prodFile, File prodFeatureFile, File skuFile, File prodPropertyFile, File exportDir) {
        String resourcesPath = grailsApplication.config.resources.path


        List<Category> cats = Category.findAllByCatalogAndParent(Catalog.get(catalogId), parent, deleted)
        cats.each {
            categoryFile << toArray(it).join("\t") << lineSeparator

            List<Feature> features = featureService.getCategoryFeatures(it.id, false)
            features.each {
                catFeatureFile << toArray(it).join("\t") << lineSeparator
            }

            List<Variation> variations = Variation.findAllByCategory(it, [sort: 'position', order: 'asc'])
            variations.each { varit ->
                varFile << toArray(varit).join("\t") << lineSeparator
                List<VariationValue> values = VariationValue.findAllByVariation(varit)
                values.each {
                    variationValueFile << toArray(it).join("\t") << lineSeparator
                }
            }

            List<Product> products = Product.findAllByCategoryAndDeleted(it, deleted)
            products.each { prd ->
                prodFile << toArray(prd).join("\t") << lineSeparator
                (new File(exportDir, prd.sanitizedName)).mkdirs()
                List<Product2Resource> prdres = Product2Resource.findAllByProduct(prd, [sort: "position", order: "asc"])
                prdres.each {
                    Path resUrl = Paths.get(resourcesPath + (it.resource.url - resourcesPath))
                    try {
                        Files.copy(resUrl, Paths.get(exportDir.getAbsolutePath(), prd.sanitizedName, it.resource.name))
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace()
                    }
                }

                List<Feature> pfeatures = featureService.getProductFeatures(prd.id, false)
                pfeatures.each {
                    prodFeatureFile << toArrayForPrd(it).join("\t") << lineSeparator
                }

                List<ProductProperty> pproperties = ProductProperty.findAllByProduct(Product.get(prd.id))
                pproperties.each {
                    prodPropertyFile << toArray(it).join("\t") << lineSeparator
                }

                List<TicketType> ticketTypes = TicketType.findAllByProduct(prd)
                ticketTypes.each {
                    skuFile << toArray(it).join("\t") << lineSeparator
                }
            }

            log.info(it.name)
            doExport(catalogId, it, deleted, categoryFile, catFeatureFile, varFile, variationValueFile, prodFile, prodFeatureFile, skuFile, prodPropertyFile, exportDir)
        }
    }
}
