package com.mogobiz.utils

/**
 *
 * Created by smanciot on 03/03/15.
 */
enum PermissionType {

    ADMIN_COMPANY("company:{0}:admin"),
    ADMIN_STORE_PROFILES("profiles:{0}:admin"),
    ADMIN_STORE_USERS("users:{0}:admin"),
    ADMIN_STORE_SHIPPING("shipping:{0}:admin"),
    ADMIN_STORE_TAXES("taxes:{0}:admin"),
    ADMIN_STORE_PAYMENT("payment:{0}:admin"),
    ADMIN_STORE_BRANDS("brands:{0}:admin"),
    ADMIN_STORE_COUPONS("coupons:{0}:admin"),
    ADMIN_STORE_KEYS("keys:{0}:admin"),
    ADMIN_STORE_BEACONS("beacons:{0}:admin"),
    ADMIN_STORE_TAGS("tags:{0}:admin"),
    PUBLISH_STORE_CATALOGS("catalogs:{0}:publish:{1}"), // UserPermission per environment
    CREATE_STORE_CATALOGS("catalogs:{0}:create"),
    DELETE_STORE_CATALOGS("catalogs:{0}:delete"),
    IMPORT_STORE_CATALOGS("catalogs:{0}:import"),
    EXPORT_STORE_CATALOGS("catalogs:{0}:export"),
    UPDATE_CATALOG("catalog:{0}:update"), // UserPermission
    UPDATE_CATALOG_CATEGORY("catalog:{0}:category:{1}:update"), // UserPermission per catalog and category
    ACCESS_STORE_BO("bo:{0}:show"),
    EXECUTE_STORE_BO_OPERATION("bo:{0}:admin"),
    ADMIN_STORE_SOCIAL_NETWORKS("networks:{0}:admin")

    private String value

    private PermissionType(String value){
        this.value = value
    }

    public String getValue(){
        value
    }
}