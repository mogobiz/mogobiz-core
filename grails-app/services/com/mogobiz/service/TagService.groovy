// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.mogobiz.service
import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Ibeacon
import com.mogobiz.store.domain.ProductState
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.Tag

class TagService
{

    PagedList<Tag> list(Company company, PagedListCommand cmd) {
        if (seller?.company == null || cmd == null) {
            throw new IllegalArgumentException()
        }

//        def params = [companyId: seller.company.id]
//        final String query = "SELECT DISTINCT tag FROM Product p RIGHT JOIN p.tags AS tag WHERE p.company.id = :companyId ORDER BY tag.name ASC";
//        List<Tag> tags = Tag.executeQuery(query, params << cmd.getPagination())

//        final String count = "SELECT count(DISTINCT tag) FROM Product p RIGHT JOIN p.tags AS tag WHERE p.company.id = :companyId"
//        int totalCount = Tag.executeQuery(count, params).get(0) as Integer
        List<Tag> tags = Tag.findAllByCompany(company, cmd.getPagination())
        int totalCount = Tag.countByCompany(company)
        new PagedList<Tag>(list:tags, totalCount:totalCount)
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
}

class PagedList<E>{
    int totalCount = 0
    List<E> list = []
}