package com.mogobiz.service

import com.mogobiz.store.domain.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

class ExportService {
    CategoryService categoryService
    FeatureService featureService

    final List<String> catHeaders = ["id", "uuid", "external-code", "path", "name", "description", "keywords", "hide", "seo", "google", "deleted"]
    final List<String> featHeaders = ["category-id", "category-path", "product-id", "product-path", "id", "uuid", "external-code", "domain", "name", "value", "hide"]
    final List<String> varHeaders = ["category-id", "category-path", "id", "uuid", "external-code", "google", "hide"]
    final List<String> varValHeaders = ["category-id", "category-path", "variation-id", "id", "uuid", "external-code", "variation-name", "value", "google", "hide"]
    final List<String> prdHeaders = ["category-id", "category-path", "id", "uuid", "external-code", "code", "name", "xtype", "price", "state", "description", "sales", "display-stock", "calendar", "start-date", "stop-date", "start-featured-date", "stop-featured-date", "seo", "tags", "keywords"]
    final List<String> skuHeaders = ["category-id", "category-path", "product-id", "product-path", "id", "uuid", "external-code", "sku", "name", "price", "min-order", "max-order", "sales", "start-date", "stop-date", "private", "remaining-stock", "unlimited-stock", "outsell-stock", "description", "availability-date", "google-gtin", "google-mpn", "variation-value-1", "variation-value-21", "variation-value-3"]

    List<String> toArray(Category it) {
        [it.id, it.uuid, it.externalCode, categoryService.path(it), it.name, it.description, it.keywords, it.hide, it.sanitizedName, it.googleCategory, it.deleted]
    }

    List<String> toArray(Feature it) {
        [it.category?.id, it.category ? categoryService.path(it.category) : null, it.product?.id, it.product?.name, it.id, it.uuid, it.externalCode, it.domain, it.name, it.value, it.hide]
    }

    List<String> toArray(Variation it) {
        [it.categoryId, categoryService.path(it.category), it.id, it.uuid, it.externalCode, it.name, it.googleVariationType, it.hide]
    }

    List<String> toArray(VariationValue it) {
        [it.variation.categoryId, categoryService.path(it.variation.category), it.variationId, it.id, it.uuid, it.externalCode, it.variation.name, it.value, it.googleVariationValue]
    }

    List<String> toArray(Product it) {
        [it.categoryId, categoryService.path(it.category), it.id, it.uuid, it.externalCode, it.code, it.name, it.xtype, it.price, it.state, it.description, it.nbSales, it.stockDisplay, it.calendarType, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : null, it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : null, it.startFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startFeatureDate.getTime()) : null, it.stopFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopFeatureDate.getTime()) : null, it.sanitizedName, it.tags.collect{it.name}.join(","), it.keywords, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : null]
    }

    List<String> toArray(TicketType it) {
        [it.product.categoryId, categoryService.path(it.product.category), it.product.id, it.product.name, it.id, it.uuid, it.externalCode, it.sku, it.name, it.price, it.minOrder, it.maxOrder, it.nbSales, it.startDate ?new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : null, it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : null, it.xprivate, it.stock?.stock, it.stock?.stockUnlimited, it.stock?.stockOutSelling, it.description, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : null, it.gtin, it.mpn, it.variation1?.value, it.variation2?.value, it.variation3?.value]
    }


    void export(long catalogId, Category parent = null, boolean deleted = false) {
        int catRownum = 0
        int catfeatRownum = 0
        int varRownum = 0
        int varValRownum = 0
        int prdRownum = 0
        int prdFeatRownum = 0
        int skuRownum = 0

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet catSheet = workbook.createSheet("category");
        Row catRow = catSheet.createRow(catRownum++)
        int catCellnum = 0
        catHeaders.each {
            Cell catCell = catRow.createCell(catCellnum++)
            catCell.setCellValue(it)
        }

        XSSFSheet catFeatSheet = workbook.createSheet("cat-feature");
        Row catFeatRow = catFeatSheet.createRow(catfeatRownum++)
        int catFeatCellnum = 0
        featHeaders.each {
            Cell catFeatCell = catFeatRow.createCell(catFeatCellnum++)
            catFeatCell.setCellValue(it)
        }

        XSSFSheet varSheet = workbook.createSheet("variation");
        Row varRow = varSheet.createRow(varRownum++)
        int varCellnum = 0
        varHeaders.each {
            Cell varCell = varRow.createCell(varCellnum++)
            varCell.setCellValue(it)
        }

        XSSFSheet varValSheet = workbook.createSheet("variation-value");
        int varValCellnum = 0
        Row varValRow = varValSheet.createRow(varValRownum++)
        varValHeaders.each {
            Cell varValCell = varValRow.createCell(varValCellnum++)
            varValCell.setCellValue(it)
        }

        XSSFSheet prdSheet = workbook.createSheet("product");
        Row prdRow = prdSheet.createRow(prdRownum++)
        int prdCellnum = 0
        prdHeaders.each {
            Cell prdCell = prdRow.createCell(prdCellnum++)
            prdCell.setCellValue(it)
        }

        XSSFSheet prdFeatSheet = workbook.createSheet("product-feature");
        Row prdFeatRow = prdFeatSheet.createRow(prdFeatRownum++)
        int prdFeatCellnum = 0
        featHeaders.each {
            Cell prdFeatCell = prdFeatRow.createCell(prdFeatCellnum++)
            prdFeatCell.setCellValue(it)
        }

        XSSFSheet skuSheet = workbook.createSheet("sku");
        Row skuRow = skuSheet.createRow(skuRownum++)
        int skuCellnum = 0
        skuHeaders.each {
            Cell skuCell = skuRow.createCell(skuCellnum++)
            skuCell.setCellValue(it)
        }

        categories(catalogId, workbook, parent, deleted, [catRownum, catfeatRownum, varRownum, varValRownum, prdRownum, prdFeatRownum, skuRownum])
        try {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("/Users/hayssams/tmp/test.xlsx"));
            workbook.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    void categories(long catalogId, XSSFWorkbook workbook, Category parent, boolean deleted, ArrayList<Integer> rownums) {
        int catRownum = rownums[0]
        int catfeatRownum = rownums[1]
        int varRownum = rownums[2]
        int varValRownum = rownums[3]
        int prdRownum = rownums[4]
        int prdFeatRownum = rownums[5]
        int skuRownum = rownums[6]

        XSSFSheet catSheet = workbook.getSheet("category");
        XSSFSheet catFeatSheet = workbook.getSheet("cat-feature");
        XSSFSheet varSheet = workbook.getSheet("variation");
        XSSFSheet varValSheet = workbook.getSheet("variation-value");
        XSSFSheet prdSheet = workbook.getSheet("product");
        XSSFSheet prdFeatSheet = workbook.getSheet("product-feature");
        XSSFSheet skuSheet = workbook.getSheet("sku");


        List<Category> cats = Category.findAllByCatalogAndParent(Catalog.get(catalogId), parent, deleted)
        cats.each {
            int catCellnum = 0
            Row catRow = catSheet.createRow(catRownum++)
            toArray(it).each {
                Cell catCell = catRow.createCell(catCellnum++)
                catCell.setCellValue(it)
            }

            List<Feature> features = featureService.getCategoryFeatures(it.id, false)
            features.each {
                int catFeatCellnum = 0
                Row catFeatRow = catFeatSheet.createRow(catfeatRownum++)
                toArray(it).each {
                    Cell catFeatCell = catFeatRow.createCell(catFeatCellnum++)
                    catFeatCell.setCellValue(it)
                }
            }

            List<Variation> variations = Variation.findAllByCategory(it, [sort: 'position', order: 'asc'])
            variations.each {
                int varCellnum = 0
                Row varRow = varSheet.createRow(varRownum++)
                toArray(it).each {
                    Cell varCell = varRow.createCell(varCellnum++)
                    varCell.setCellValue(it)
                }
                List<VariationValue> values = VariationValue.findAllByVariation(it)
                values.each {
                    int varValCellnum = 0
                    Row varValRow = varValSheet.createRow(varValRownum++)
                    toArray(it).each {
                        Cell varValCell = varValRow.createCell(varValCellnum++)
                        varValCell.setCellValue(it)
                    }
                }
            }

            List<Product> products = Product.findAllByCategoryAndDeleted(it, deleted)
            products.each {
                int prdCellnum = 0
                Row prdRow = prdSheet.createRow(prdRownum++)
                toArray(it).each {
                    Cell prdCell = prdRow.createCell(prdCellnum++)
                    prdCell.setCellValue(it.toString())
                }

                List<Feature> pfeatures = featureService.getProductFeatures(it.id, false)
                pfeatures.each {
                    int prdFeatCellnum = 0
                    Row prdFeatRow = prdFeatSheet.createRow(prdFeatRownum++)
                    toArray(it).each {
                        Cell prdFeatCell = prdFeatRow.createCell(prdFeatCellnum++)
                        prdFeatCell.setCellValue(it)
                    }
                }

                List<TicketType> ticketTypes = TicketType.findAllByProduct(it)
                ticketTypes.each {
                    int skuCellnum = 0
                    Row skuRow = skuSheet.createRow(skuRownum++)
                    toArray(it).each {
                        Cell skuCell = skuRow.createCell(skuCellnum++)
                        skuCell.setCellValue(it)
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
            categories(catalogId, workbook, it, deleted, rownums)
        }
    }

}
