package com.mogobiz.service

import com.mogobiz.store.domain.Category

/**
 * Management service categories
 */
class CategoryService {

    /**
     * This method returns all visible categories of the merchant's site and of the parent category if the parameter "parentId" is defined
     * @param locale
     * @param companyId
     * @param parentId
     * @return : json map of all categories found
     */
    List<Map> listVisibleByCompany(Locale locale, long companyId, Long parentId) {
        List<Category> liste = Category.createCriteria().list {
            company { eq("id", companyId) }
            if (parentId) {
                parent { eq("id", parentId) }
            } else {
                isNull("parent")
            }
            eq("hide", false)
        }

        List<Map> result = [];
        liste.each { Category c ->
            result << c.asMapForJSON(null, null, locale?.language)
        }
        return result;
    }

    String path(Category cat) {
        def res = []
        res.push(cat.name)
        while (cat.parent) {
            cat = cat.parent
            res.add(0, cat.name)
        }
        return "/" + res.join("/")
    }

    boolean isChildOf(Category child, Category parent) {
        if (child == null) return false
        if (child == parent) return true
        return isChildOf(child.parent, parent)
    }
}