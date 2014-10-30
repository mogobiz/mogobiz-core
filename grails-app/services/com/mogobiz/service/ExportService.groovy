package com.mogobiz.service

import com.mogobiz.store.domain.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataValidationConstraint
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.XSSFDataValidation
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

class ExportService {
    CategoryService categoryService
    FeatureService featureService

    final List<String> catHeaders = ["id", "uuid", "external-code", "path", "name", "description", "keywords", "hide", "seo", "google", "deleted"]
    final List<String> featHeaders = ["category-id", "category-path", "product-id", "product-path", "id", "uuid", "external-code", "domain", "name", "value", "hide"]
    final List<String> varHeaders = ["category-id", "category-path", "id", "uuid", "external-code", "google", "hide"]
    final List<String> varValHeaders = ["category-id", "category-path", "variation-id", "variation-name", "id", "uuid", "external-code", "value", "google", "hide"]
    final List<String> prdHeaders = ["category-id", "category-path", "id", "uuid", "external-code", "code", "name", "xtype", "price", "state", "description", "sales", "display-stock", "calendar", "start-date", "stop-date", "start-featured-date", "stop-featured-date", "seo", "tags", "keywords"]
    final List<String> skuHeaders = ["category-id", "category-path", "product-id", "product-path", "id", "uuid", "external-code", "sku", "name", "price", "min-order", "max-order", "sales", "start-date", "stop-date", "private", "remaining-stock", "unlimited-stock", "outsell-stock", "description", "availability-date", "google-gtin", "google-mpn", "variation-value-1", "variation-value-21", "variation-value-3"]

    List<String> toArray(Category it) {
        [it.id, it.uuid, it.externalCode, categoryService.path(it), it.name, it.description, it.keywords, it.hide, it.sanitizedName, it.googleCategory, it.deleted]
    }

    List<String> toArrayForCat(Feature it, int rowNum) {
        ["category!A" + rowNum, "category!D" + rowNum, null, null, it.id, it.uuid, it.externalCode, it.domain, it.name, it.value, it.hide]
    }

    List<String> toArrayForPrd(Feature it, int rowNum) {
        ["product!A" + rowNum, "product!B" + rowNum, "product!C" + rowNum, "product!G" + rowNum, it.id, it.uuid, it.externalCode, it.domain, it.name, it.value, it.hide]
    }

    List<String> toArray(Variation it, int rowNum) {
        ["category!A" + rowNum, "category!D" + rowNum, it.id, it.uuid, it.externalCode, it.name, it.googleVariationType, it.hide]
    }

    List<String> toArray(VariationValue it, int rowNum) {
        ["category!A" + rowNum, "category!D" + rowNum, "product!A" + rowNum, "product!B" + rowNum, "product!C" + rowNum, "product!G" + rowNum, it.id, it.uuid, it.externalCode, it.value, it.googleVariationValue]
    }

    List<String> toArray(Product it, rowNum) {
        ["category!A" + rowNum, "category!D" + rowNum, it.id, it.uuid, it.externalCode, it.code, it.name, it.xtype, it.price, it.state, it.description, it.nbSales, it.stockDisplay, it.calendarType, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : null, it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : null, it.startFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startFeatureDate.getTime()) : null, it.stopFeatureDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopFeatureDate.getTime()) : null, it.sanitizedName, it.tags.collect {
            it.name
        }.join(","), it.keywords, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : null]
    }

    List<String> toArray(TicketType it, int rowNum) {
        ["category!A" + rowNum, "category!D" + rowNum, "product!A" + rowNum, "product!B" + rowNum, "product!C" + rowNum, it.id, it.uuid, it.externalCode, it.sku, it.name, it.price, it.minOrder, it.maxOrder, it.nbSales, it.startDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.startDate.getTime()) : null, it.stopDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.stopDate.getTime()) : null, it.xprivate, it.stock?.stock, it.stock?.stockUnlimited, it.stock?.stockOutSelling, it.description, it.availabilityDate ? new SimpleDateFormat("yyyy-MM-dd").format(it.availabilityDate.getTime()) : null, it.gtin, it.mpn, it.variation1?.value, it.variation2?.value, it.variation3?.value]
    }

    CellStyle unlockedCellStyle

    File export(long catalogId, Category parent = null, boolean deleted = false) {
        int catRownum = 0
        int catfeatRownum = 0
        int varRownum = 0
        int varValRownum = 0
        int prdRownum = 0
        int prdFeatRownum = 0
        int skuRownum = 0

        XSSFWorkbook workbook = new XSSFWorkbook();
        unlockedCellStyle = workbook.createCellStyle();
        unlockedCellStyle.setLocked(false);


        XSSFSheet catSheet = workbook.createSheet("category");
        catSheet.protectSheet("")
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(catSheet);
        DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(["TRUE", "FALSE"] as String[]);
        CellRangeAddressList addressList = new CellRangeAddressList(0, 65000, 7, 7);
        XSSFDataValidation dataValidation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        dataValidation.setShowErrorBox(true);
        catSheet.addValidationData(dataValidation)

        addressList = new CellRangeAddressList(0, 65000, 10, 10);
        dataValidation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        dataValidation.setShowErrorBox(true);
        catSheet.addValidationData(dataValidation)

        Row catRow = catSheet.createRow(catRownum++)
        int catCellnum = 0
        catHeaders.each {
            Cell catCell = catRow.createCell(catCellnum++)
            catCell.setCellValue(it)
        }

        XSSFSheet catFeatSheet = workbook.createSheet("cat-feature");
        catFeatSheet.protectSheet("")
        dvHelper = new XSSFDataValidationHelper(catFeatSheet);
        dvConstraint = dvHelper.createExplicitListConstraint(["TRUE", "FALSE"] as String[]);
        addressList = new CellRangeAddressList(0, 65000, 10, 10);
        dataValidation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        dataValidation.setShowErrorBox(true);
        catFeatSheet.addValidationData(dataValidation)

        Row catFeatRow = catFeatSheet.createRow(catfeatRownum++)
        int catFeatCellnum = 0
        featHeaders.each {
            Cell catFeatCell = catFeatRow.createCell(catFeatCellnum++)
            catFeatCell.setCellValue(it)
        }

        XSSFSheet varSheet = workbook.createSheet("variation");
        varSheet.protectSheet("")
        Row varRow = varSheet.createRow(varRownum++)
        int varCellnum = 0
        varHeaders.each {
            Cell varCell = varRow.createCell(varCellnum++)
            varCell.setCellValue(it)
        }

        XSSFSheet varValSheet = workbook.createSheet("variation-value");
        varValSheet.protectSheet("")
        int varValCellnum = 0
        Row varValRow = varValSheet.createRow(varValRownum++)
        varValHeaders.each {
            Cell varValCell = varValRow.createCell(varValCellnum++)
            varValCell.setCellValue(it)
        }

        XSSFSheet prdSheet = workbook.createSheet("product");
        prdSheet.protectSheet("")
        Row prdRow = prdSheet.createRow(prdRownum++)
        int prdCellnum = 0
        prdHeaders.each {
            Cell prdCell = prdRow.createCell(prdCellnum++)
            prdCell.setCellValue(it)
        }

        XSSFSheet prdFeatSheet = workbook.createSheet("product-feature");
        prdFeatSheet.protectSheet("")
        Row prdFeatRow = prdFeatSheet.createRow(prdFeatRownum++)
        int prdFeatCellnum = 0
        featHeaders.each {
            Cell prdFeatCell = prdFeatRow.createCell(prdFeatCellnum++)
            prdFeatCell.setCellValue(it)
        }

        XSSFSheet skuSheet = workbook.createSheet("sku");
        skuSheet.protectSheet("")
        Row skuRow = skuSheet.createRow(skuRownum++)
        int skuCellnum = 0
        skuHeaders.each {
            Cell skuCell = skuRow.createCell(skuCellnum++)
            skuCell.setCellValue(it)
        }

        categories(catalogId, workbook, parent, deleted, [catRownum, catfeatRownum, varRownum, varValRownum, prdRownum, prdFeatRownum, skuRownum])
        File outFile = File.createTempFile("mogobiz-" + (new SimpleDateFormat("yyyy-MM-dd").format(new Date())), "xlsx")
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(outFile);
        workbook.write(out);
        out.close();
        return outFile
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
                if (catCellnum <= 4) {
                    catCell.setCellValue(it)
                } else {
                    catCell.setCellValue(it)
                    catCell.setCellStyle(unlockedCellStyle)
                }
            }

            List<Feature> features = featureService.getCategoryFeatures(it.id, false)
            features.each {
                int catFeatCellnum = 0
                Row catFeatRow = catFeatSheet.createRow(catfeatRownum++)
                toArrayForCat(it, catRownum).each {
                    Cell catFeatCell = catFeatRow.createCell(catFeatCellnum++)
                    if (catFeatCellnum <= 4) {
                        catFeatCell.setCellFormula(it)
                    } else {
                        catFeatCell.setCellValue(it)
                        catFeatCell.setCellStyle(unlockedCellStyle)

                    }
                }
            }

            List<Variation> variations = Variation.findAllByCategory(it, [sort: 'position', order: 'asc'])
            variations.each {
                int varCellnum = 0
                Row varRow = varSheet.createRow(varRownum++)
                toArray(it, varRownum).each {
                    Cell varCell = varRow.createCell(varCellnum++)
                    if (varCellnum <= 2)
                        varCell.setCellFormula(it)
                    else {
                        varCell.setCellValue(it)
                        varCell.setCellStyle(unlockedCellStyle)
                    }
                }
                List<VariationValue> values = VariationValue.findAllByVariation(it)
                values.each {
                    int varValCellnum = 0
                    Row varValRow = varValSheet.createRow(varValRownum++)
                    toArray(it, varValRownum).each {
                        Cell varValCell = varValRow.createCell(varValCellnum++)
                        if (varValCellnum <= 4)
                            varValCell.setCellFormula(it)
                        else {
                            varValCell.setCellValue(it)
                            varValCell.setCellStyle(unlockedCellStyle)
                        }
                    }
                }
            }

            List<Product> products = Product.findAllByCategoryAndDeleted(it, deleted)
            products.each {
                int prdCellnum = 0
                Row prdRow = prdSheet.createRow(prdRownum++)
                toArray(it, prdRownum).each {
                    Cell prdCell = prdRow.createCell(prdCellnum++)
                    if (prdCellnum <= 2)
                        prdCell.setCellFormula(it)
                    else {
                        prdCell.setCellValue(it.toString())
                        prdCell.setCellStyle(unlockedCellStyle)
                    }
                }

                List<Feature> pfeatures = featureService.getProductFeatures(it.id, false)
                pfeatures.each {
                    int prdFeatCellnum = 0
                    Row prdFeatRow = prdFeatSheet.createRow(prdFeatRownum++)
                    toArrayForPrd(it, prdFeatRownum).each {
                        Cell prdFeatCell = prdFeatRow.createCell(prdFeatCellnum++)
                        prdFeatCell.setCellValue(it)
                        if (prdFeatCellnum <= 4)
                            prdFeatCell.setCellFormula(it)
                        else {
                            prdFeatCell.setCellValue(it)
                            prdFeatCell.setCellStyle(unlockedCellStyle)
                        }
                    }
                }

                List<TicketType> ticketTypes = TicketType.findAllByProduct(it)
                ticketTypes.each {
                    int skuCellnum = 0
                    Row skuRow = skuSheet.createRow(skuRownum++)
                    toArray(it, skuRownum).each {
                        Cell skuCell = skuRow.createCell(skuCellnum++)
                        if (skuCellnum <= 4)
                            skuCell.setCellFormula(it)
                        else {
                            skuCell.setCellValue(it)
                            skuCell.setCellStyle(unlockedCellStyle)
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
            categories(catalogId, workbook, it, deleted, rownums)
        }
    }

}

