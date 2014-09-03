package com.mogobiz.service

import com.mogobiz.store.domain.Catalog

/**
 * Management service catalogs
 */
class CatalogService {

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
}
