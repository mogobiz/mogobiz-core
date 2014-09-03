// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service
import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.domain.Ibeacon
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.Tag
import com.mogobiz.utils.PagedResultList

class TagService
{

    PagedResultList list(Seller seller, PagedListCommand cmd) {
        if (seller?.company == null || cmd == null) {
            throw new IllegalArgumentException()
        }

        String query = "SELECT DISTINCT tag FROM Product p RIGHT JOIN p.tags AS tag WHERE p.company.id = :companyId ORDER BY tag.name ASC";
        List<Tag> list = Tag.executeQuery(query, [companyId: seller.company.id])
        return new PagedResultList(cmd, list)
    }


    Tag setIbeacon(Seller seller, Long tagId, Long ibeaconId) {
        if (seller?.company == null || tagId == null) {
            throw new IllegalArgumentException()
        }

        Tag tagInBase = Tag.get(tagId)
        if (tagInBase == null) {
            tagInBase = new Tag()
            tagInBase.errors.rejectValue("id", "unknown")
            return tagInBase
        }

        Ibeacon beacon = (ibeaconId != null) ? Ibeacon.get(ibeaconId) : null
        tagInBase.ibeacon = beacon
        if (tagInBase.validate()) {
            tagInBase.save()
        }
        return tagInBase
    }

    /**
	 * This method returns all active tags of the merchant's site for which there is at least one active product associated.
	 * A tag is active if it is referenced by a active product
	 * @param locale
	 * @param categoryId
	 * @return
	 */
	List<Map> listActiveByCompany(Locale locale, long companyId)
	{
		String query = "SELECT DISTINCT tag FROM Product p RIGHT JOIN p.tags AS tag WHERE p.company.id = :companyId AND p.state = :state";
		List<Tag> liste = Tag.executeQuery(query, [companyId: companyId, state: ProductState.ACTIVE])

		List<Map> result = [];
		liste.each { Tag t ->
			result << t.asMapForJSON(null, null, locale?.language)
		}
		return result;
	}

	/**
	 * This method returns the active tags of the merchant's site for which there is at least one active product associated with the given category.
	 * A tag is active if it is referenced by a active product
	 * @param locale
	 * @param companyId
	 * @param categoryId
	 * @return
	 */
    List<Map> listActiveByCategory(Locale locale, long companyId, long categoryId)
    {
		String query = "SELECT DISTINCT tag FROM Product p RIGHT JOIN p.tags AS tag WHERE p.company.id = :companyId AND p.state = :state AND p.category.id = :categoryId";
		List<Tag> liste = Tag.executeQuery(query, [companyId: companyId, state: ProductState.ACTIVE, categoryId: categoryId])

		List<Map> result = [];
		liste.each { Tag t ->
			result << t.asMapForJSON(null, null, locale?.language)
		}
		return result;
    }
	
}