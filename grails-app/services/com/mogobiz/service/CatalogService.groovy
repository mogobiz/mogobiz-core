/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.exception.ProductNotFoundException
import groovy.sql.Sql

/**
 * Management service catalogs
 */
class CatalogService {
	def dataSource

	Catalog addNew(Catalog catalog) {
		catalog.save(flush:true)
		return catalog
	}

	void remove(long id) {
		Catalog.get(id).delete()
	}

	boolean exist(long id) {
		return Catalog.get(id) != null
	}

	Catalog update(Catalog catalog) {
		catalog.save(flush:true)
		return catalog
	}

	Catalog get(long id) {
		Catalog.get(id)
	}

	/**
	 * This methode returns the id of the default catalog (root)
	 * of the company.
	 * @param companyId
	 * @return
	 */
	Long getDefaultCatalogId(long companyId) {
		List<Catalog> catalog = Catalog.createCriteria().list {
			company { eq("id", companyId) }
			le("activationDate", new Date())
			order("activationDate", "desc")
		}
        if (catalog?.size() > 0) {
            return catalog.get(0).id
        }
		return null
	}
	int purge(long catalogId) {
		Catalog catalog = Catalog.get(catalogId)
		if (catalog == null)
			throw new ProductNotFoundException()
		/*
        alter table product drop column creation_fk
        alter table xresource drop column creation_fk
        drop table event
        ALTER TABLE b_o_product ALTER COLUMN product_fk DROP NOT NULL

         */
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
			sql.execute("delete from tag where id not in (select tag_id from product_tag)")
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
			sql.execute("delete from xresource where id not in (select resource_fk from product2_resource)")
			sql.execute("delete from product  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
			sql.execute("delete from variation_value where variation_fk in (select v.id from variation v, category c where v.category_fk = c.id and c.catalog_fk = ${catalog.id})")
			sql.execute("delete from variation  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
			sql.execute("delete from category where catalog_fk = ${catalog.id}")
			sql.execute("delete from xcatalog where id = ${catalog.id}")
		}
		return count
	}
}
