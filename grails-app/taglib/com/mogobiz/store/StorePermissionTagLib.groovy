/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store

import com.mogobiz.store.domain.Seller
import com.mogobiz.utils.PermissionType
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject

import static com.mogobiz.utils.ProfileUtils.computeStorePermission

class StorePermissionTagLib {

    static namespace = "store"

    /**
     * This tag only writes its body to the output if the current user
     * has the given permission.
     */
    def hasPermission = { attrs, body ->
        if (checkPermission(attrs, "hasPermission")) {
            // Output the body text.
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * does not have the given permission.
     */
    def lacksPermission = { attrs, body ->
        if (!checkPermission(attrs, "lacksPermission")) {
            // Output the body text.
            out << body()
        }
    }

    /**
     * Checks whether the current user has the permission specified in
     * the given tag attributes. Returns <code>true</code> if the user
     * has the permission, otherwise <code>false</code>.
     */
    private boolean checkPermission(attrs, tagname) {

        def check = false

        List<String> permissions = attrs["in"] as List<String>
        if(!permissions){
            String permission = attrs["permission"]
            if (!permission) {
                throwTagError("Tag [$tagname] must have a [permission] or [in] attribute.")
            }
            permissions = [permission]
        }

        def id = attrs["id"] as Long
        if(!id){
            def seller = request.getAttribute("seller") as Seller ?: retrieveAuthenticatedSeller()
            if(seller){
                id = seller.company?.id
            }
        }

        if(id){
            def storePermissions = permissions.collect {permission ->
                def storePermission = PermissionType.findByKey(permission)
                if(!storePermission){
                    throwTagError("No store permission found for [$permission].")
                }
                computeStorePermission(storePermission, id)
            }

            check = SecurityUtils.subject.isPermitted(storePermissions?.toArray(new String[storePermissions.size()])).any {it}
        }

        return check
    }

    private static Seller retrieveAuthenticatedSeller(){
        def seller = null
        Subject subject = SecurityUtils.getSubject()
        Object principal = subject?.getPrincipal()
        if (principal != null && subject.isAuthenticated()) {
            def login = principal.toString()
            seller = Seller.findByLogin(login, [fetch: [company: 'join']])
        }
        return seller
    }
}
