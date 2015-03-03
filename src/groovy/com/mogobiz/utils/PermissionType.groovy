package com.mogobiz.utils

/**
 *
 * Created by smanciot on 03/03/15.
 */
enum PermissionType {

    ADMIN_COMPANY("company:{0}:admin"), UPDATE_USERS("company:{0}:users:update"), UPDATE_SHIPPING("company:{0}:shipping:update")

    private String value

    private PermissionType(String value){
        this.value = value
    }
}