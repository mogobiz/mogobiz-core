/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.common.client.ClientConfig
import com.mogobiz.common.client.Credentials
import com.mogobiz.common.rivers.spi.RiverConfig
import com.mogobiz.mirakl.client.MiraklClient
import com.mogobiz.mirakl.client.domain.AttributeType
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.FeatureValue
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.Variation
import com.mogobiz.store.domain.VariationValue
import com.mogobiz.store.exception.ProductNotFoundException
import com.mogobiz.utils.PermissionType
import groovy.sql.Sql

/**
 * Management service catalogs
 */
class CatalogService {
	def dataSource

    def sanitizeUrlService

    def profileService

    def authenticationService

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
            log.info("delete from coupon_category where category_id in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from coupon_category where category_id in (select id from category where catalog_fk = ${catalog.id})")
            log.info("delete from feature where category_fk in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from feature where category_fk in (select id from category where catalog_fk = ${catalog.id})")
            log.info("delete from coupon_product  where product_id in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from coupon_product  where product_id in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from product_tag  where tags_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk =  ${catalog.id})")
            sql.execute("delete from product_tag  where tags_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk =  ${catalog.id})")
            log.info("delete from tag where id not in (select tag_id from product_tag)")
            sql.execute("delete from tag where id not in (select tag_id from product_tag)")
            log.info("delete from suggestion  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from suggestion  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from suggestion  where pack_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from suggestion  where pack_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from date_period  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from date_period  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from intra_day_period  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from intra_day_period  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from feature  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from feature  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from product_property  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from product_property  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from stock_calendar  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from stock_calendar  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from event_period_sale  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from event_period_sale  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from coupon_ticket_type  where ticket_type_id in (select sku.id from ticket_type sku, product p, category c where sku.product_fk = p.id and p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from coupon_ticket_type  where ticket_type_id in (select sku.id from ticket_type sku, product p, category c where sku.product_fk = p.id and p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from stock_calendar  where ticket_type_fk in (select sku.id from ticket_type sku, product p, category c where sku.product_fk = p.id and p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from stock_calendar  where ticket_type_fk in (select sku.id from ticket_type sku, product p, category c where sku.product_fk = p.id and p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("update ticket_type set variation1_fk = null, variation2_fk=null, variation3_fk= null where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("update ticket_type set variation1_fk = null, variation2_fk=null, variation3_fk= null where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from ticket_type  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from ticket_type  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from product2_resource  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from product2_resource  where product_fk in (select p.id from product p, category c where p.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from xresource where id not in (select resource_fk from product2_resource)")
            sql.execute("delete from xresource where id not in (select resource_fk from product2_resource)")
            log.info("delete from product  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from product  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
            log.info("delete from variation_value where variation_fk in (select v.id from variation v, category c where v.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            sql.execute("delete from variation_value where variation_fk in (select v.id from variation v, category c where v.category_fk = c.id and c.catalog_fk = ${catalog.id})")
            log.info("delete from variation  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
            sql.execute("delete from variation  where category_fk  in (select id from category where catalog_fk = ${catalog.id})")
            log.info("delete from category where catalog_fk = ${catalog.id}")
            sql.execute("delete from category where catalog_fk = ${catalog.id}")
            log.info("delete from xtranslation where catalog id = ${catalog.id}")
            sql.execute("delete from xtranslation where catalog_fk = ${catalog.id}")
            log.info("delete from xcatalog where id = ${catalog.id}")
            sql.execute("delete from xcatalog where id = ${catalog.id}")
		}
		return count
	}

    def refreshMiraklCatalog(Catalog catalog, Seller seller = null){
        final env = catalog.miraklEnv
        if(env){
            RiverConfig config = new RiverConfig(
                    debug: true,
                    clientConfig: new ClientConfig(
                            store: catalog.company.code,
                            merchant_id: env.shopId,
                            merchant_url: env.url,
                            debug: true,
                            credentials: new Credentials(
                                    frontKey: env.frontKey as String,
                                    apiKey: env.apiKey
                            )
                    ),
                    idCatalogs: [catalog.id] as List<Long>,
                    languages: null,
                    defaultLang: catalog.company.defaultLanguage
            )
            handleMiraklCategoriesByHierarchyAndLevel(catalog, config, seller ?: authenticationService.retrieveAuthenticatedSeller())
            catalog.readOnly = true
            catalog.save(flush: true)
        }
    }

    def handleMiraklCategoriesByHierarchyAndLevel(Catalog catalog, RiverConfig config, Seller seller = null, String hierarchyCode = null, int level = 1) {
        if(seller?.company?.id != catalog?.company?.id){
            throw new IllegalArgumentException()
        }
        def listHierarchiesResponse = MiraklClient.listHierarchies(config, hierarchyCode, level)
        listHierarchiesResponse.hierarchies?.findAll {
            it.level as int == level && (!hierarchyCode || it.parentCode == hierarchyCode)
        }?.each {hierarchie ->
            final externalCode = "mirakl::${hierarchie.code}"
            def category = Category.findByCatalogAndExternalCode(catalog, externalCode) ?:
                    new Category(
                            company: catalog.company,
                            catalog: catalog,
                            externalCode: externalCode,
                            name: hierarchie.label,
                            sanitizedName: sanitizeUrlService.sanitizeWithDashes(hierarchie.label)
                    )
            if(hierarchie.parentCode?.trim()?.length() > 0){
                category.parent = Category.findByCatalogAndExternalCode(
                        catalog,
                        "mirakl::${hierarchie.parentCode}"
                )
            }
            category.validate()
            if(!category.hasErrors()){
                category.save(flush: true)
                if(seller){
                    profileService.saveUserPermission(
                            seller,
                            true,
                            PermissionType.UPDATE_STORE_CATEGORY_WITHIN_CATALOG,
                            catalog.company.id as String,
                            catalog.id as String,
                            category.id as String
                    )
                }
                // récupération des features et variations
                def listAttributesResponse = MiraklClient.listAttributes(config, hierarchie.code)
                listAttributesResponse.attributes?.findAll {it.type == AttributeType.LIST && it.typeParameter && it.hierarchyCode == hierarchie.code}?.each { attribute ->
                    if(!attribute.variant){
                        def feature = Feature.findByCategoryAndExternalCode(category, "mirakl::${attribute.code}") ?: new Feature(
                                name: attribute.label,
                                externalCode: "mirakl::${attribute.code}",
                                category: category
                        )
                        feature.validate()
                        if(!feature.hasErrors()){
                            feature.save(flush: true)
                            def listValuesResponse = MiraklClient.listValues(config, attribute.typeParameter)
                            listValuesResponse.valuesLists?.first()?.values?.each {value ->
                                def featureValue = FeatureValue.findByFeatureAndExternalCode(feature, "mirakl::${value.code}") ?: new FeatureValue(
                                        value: value.label,
                                        externalCode: "mirakl::${value.code}",
                                        feature: feature
                                )
                                featureValue.validate()
                                if(!featureValue.hasErrors()){
                                    featureValue.save(flush: true)
                                }
                            }
                        }
                    }
                    else{
                        def variation = Variation.findByCategoryAndExternalCode(category, "mirakl::${attribute.code}") ?: new Variation(
                                name: attribute.label,
                                externalCode: "mirakl::${attribute.code}",
                                category: category
                        )
                        variation.validate()
                        if(!variation.hasErrors()){
                            variation.save(flush: true)
                            def listValuesResponse = MiraklClient.listValues(config, attribute.typeParameter)
                            listValuesResponse.valuesLists?.first()?.values?.each {value ->
                                def variationValue = VariationValue.findByVariationAndExternalCode(variation, "mirakl::${value.code}") ?: new VariationValue(
                                        value: value.label,
                                        externalCode: "mirakl::${value.code}",
                                        variation: variation
                                )
                                variationValue.validate()
                                if(!variationValue.hasErrors()){
                                    variationValue.save(flush: true)
                                }
                            }
                        }
                    }
                }
                handleMiraklCategoriesByHierarchyAndLevel(catalog, config, seller, hierarchie.code, level+1)
            }
            else {
                category.errors.allErrors.each { log.error(it) }
            }
        }
    }
}
