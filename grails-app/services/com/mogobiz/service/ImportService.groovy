package com.mogobiz.service

import com.mogobiz.store.domain.*
import groovy.sql.Sql
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

class ImportService {
    CategoryService categoryService
    def dataSource

    final List<String> catHeaders = ["id", "uuid", "external-code", "path", "name", "description", "keywords", "hide", "seo", "google", "deleted"]
    final List<String> featHeaders = ["category-id", "category-path", "product-id", "product-path", "id", "uuid", "external-code", "domain", "name", "value", "hide"]
    final List<String> varHeaders = ["category-id", "category-path", "id", "uuid", "external-code", "name", "google", "hide"]
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

    Map ximport(Catalog catalog, File inputFile) {
        XSSFWorkbook workbook = new XSSFWorkbook(XSSFWorkbook.openPackage(inputFile.getAbsolutePath()))
        XSSFSheet catSheet = workbook.getSheet("category");
        XSSFSheet catFeatSheet = workbook.getSheet("cat-feature");
        XSSFSheet varSheet = workbook.getSheet("variation");
        XSSFSheet varValSheet = workbook.getSheet("variation-value");
        XSSFSheet prdSheet = workbook.getSheet("product");
        XSSFSheet prdFeatSheet = workbook.getSheet("product-feature");
        XSSFSheet skuSheet = workbook.getSheet("sku");

        int maxdepth = 0
        for (int rownum = 1; rownum < catSheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = catSheet.getRow(rownum)
            if (row != null) {
                String cell = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString
                if (cell.length() > 0) {
                    String path = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString
                    int depth = path.split('/')
                    if (depth > maxdepth) maxdepth = depth
                }
            }
        }
        for (int currentDepth = 1; currentDepth < maxdepth; currentDepth++) {
            for (int rownum = 1; rownum < catSheet.getPhysicalNumberOfRows(); rownum++) {
                XSSFRow row = catSheet.getRow(rownum)
                if (row != null) {
                    String cell = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString
                    if (cell.length() > 0) {
                        String sid = row.getCell(0, Row.CREATE_NULL_AS_BLANK).toString
                        String uuid = row.getCell(1, Row.CREATE_NULL_AS_BLANK).toString
                        String externalCode = row.getCell(2, Row.CREATE_NULL_AS_BLANK).toString
                        String path = row.getCell(3, Row.CREATE_NULL_AS_BLANK).toString
                        String name = row.getCell(4, Row.CREATE_NULL_AS_BLANK).toString
                        String description = row.getCell(5, Row.CREATE_NULL_AS_BLANK).toString
                        String keywords = row.getCell(6, Row.CREATE_NULL_AS_BLANK).toString
                        String hide = row.getCell(7, Row.CREATE_NULL_AS_BLANK).toString
                        String seo = row.getCell(8, Row.CREATE_NULL_AS_BLANK).toString
                        String google = row.getCell(9, Row.CREATE_NULL_AS_BLANK).toString
                        String deleted = row.getCell(10, Row.CREATE_NULL_AS_BLANK).toString
                        Category parent = null

                        String[] paths = path.split('/')
                        for (int i = 0; i < paths.size() - 1; i++) {
                            parent = Category.findByNameAndParent(name, parent)
                        }

                        int depth = paths.split('/')
                        if (depth == currentDepth) {
                            Category cat = new Category()
                            if (sid.length() > 0) cat.id = sid.toLong()
                            if (uuid.length() > 0)
                                cat.uuid = uuid
                            else
                                cat.uuid = UUID.randomUUID().toString()
                            cat.externalCode = externalCode
                            cat.name = name
                            cat.description = description
                            cat.keywords = keywords
                            cat.hide = hide
                            cat.sanitizedName = seo
                            cat.googleCategory = google
                            cat.deleted = deleted
                            cat.catalog = catalog
                            cat.parent = parent
                            if (cat.validate())
                                cat.save(insert: true, flush: true)
                            else
                                return [errors: cat.errors.allErrors, sheet: "category", line: rownum]
                        }
                    }
                }
            }
        }
    }

    int purge(Catalog catalog) {
        /*
        alter table product drop column creation_fk
        alter table xresource drop column creation_fk
        drop table event
        ALTER TABLE b_o_product ALTER COLUMN product_fk DROP NOT NULL

         */
        catalog = Catalog.get(10)
        def sql = new Sql(dataSource)
        // First check hat noproduct in that catalog has been sold.
        def row = sql.firstRow("select count(*) from b_o_product where product_fk in  (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
        int count = row.getAt(0)
        if (count == 0) {
            sql.execute("delete from coupon_category where category_id in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from feature where category_fk in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from coupon_product  where product_id in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("update product set poi_fk = null  where id in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from product_tag  where tags_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk =  ${catalog.id})")
            sql.execute("delete from suggestion  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from suggestion  where pack_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from date_period  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from intra_day_period  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from feature  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from product_property  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from stock_calendar  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from event_period_sale  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from coupon_ticket_type  where ticket_type_id in (select sku.id from ticket_type sku, product p, category c where sku.product_fk = p.id and p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from stock_calendar  where ticket_type_fk in (select sku.id from ticket_type sku, product p, category c where sku.product_fk = p.id and p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("update ticket_type set variation1_fk = null, variation2_fk=null, variation3_fk= null where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from ticket_type  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from product2_resource  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from product  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from variation_value where variation_fk in (select v.id from variation v, category c where v.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from variation  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from category where catalog_fk = ${catalog.id}")
            sql.execute("delete from xcatalog where id = ${catalog.id}")
        }
        return count
    }
}

