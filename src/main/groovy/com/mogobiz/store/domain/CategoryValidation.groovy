// license-header java merge-point
//
// Generated by: GrailsEntityValidation.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain

import com.mogobiz.ValidationBase

/**
 *
 */
class CategoryValidation
    extends ValidationBase<Category>
{

    def beforeInsert(Category entity) {
        super.beforeInsert(entity)
        entity.fullpath = retrieveCategoryPath(entity)
    }

    def beforeUpdate(Category entity) {
        super.beforeUpdate(entity)
        entity.fullpath = retrieveCategoryPath(entity)
    }

    def beforeDelete(Category entity) {
        super.beforeDelete(entity)
    }

    def afterInsert(Category entity) {
        super.afterInsert(entity)
    }

    def afterUpdate(Category entity) {
        super.afterUpdate(entity)
    }

    def afterDelete(Category entity) {
        super.afterDelete(entity)
    }

    def onLoad(Category entity) {
        super.onLoad(entity)
    }

    static String retrieveCategoryPath(Category cat, String path = cat?.sanitizedName){
        def parent = cat?.parent
        parent ? retrieveCategoryPath(parent, parent.sanitizedName + '/' + path) : path
    }
}