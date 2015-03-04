package com.mogobiz.utils

/**
 *
 * Created by smanciot on 03/03/15.
 */
enum PermissionType {

    ADMIN_COMPANY("company:{0}:admin"),
    ADMIN_PROFILES("company:{0}:profiles:admin"),
    ADMIN_USERS("company:{0}:users:admin"),
    ADMIN_SHIPPING("company:{0}:shipping:admin")

    private String value

    private PermissionType(String value){
        this.value = value
    }

    public String getValue(){
        value
    }
}