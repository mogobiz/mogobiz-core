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

    ADMIN_STORE_SOCIAL_NETWORKS("networks:{0}:admin"),

    ACCESS_STORE_BO("bo:{0}:show"),
    EXECUTE_STORE_BO_OPERATION("bo:{0}:admin"),

    PUBLISH_STORE_CATALOGS_TO_ENV("catalogs:{0}:publish:{1}"), // UserPermission per environment
    CREATE_STORE_CATALOGS("catalogs:{0}:create"),
    DELETE_STORE_CATALOGS("catalogs:{0}:delete"),
    IMPORT_STORE_CATALOGS("catalogs:{0}:import"),
    EXPORT_STORE_CATALOGS("catalogs:{0}:export"),
    UPDATE_STORE_CATALOG("catalogs:{0}:catalog:{1}:update"), // UserPermission per catalog
    UPDATE_STORE_CATEGORY_WITHIN_CATALOG("catalogs:{0}:catalog:{1}:category:{2}:update") // UserPermission per catalog and category

    private String value

    private PermissionType(String value){
        this.value = value
    }

    public String getValue(){
        value
    }

    public static Collection<PermissionType> admin(){
        [
                ADMIN_COMPANY,
                ADMIN_STORE_PROFILES,
                ADMIN_STORE_USERS,
                ADMIN_STORE_SHIPPING,
                ADMIN_STORE_TAXES,
                ADMIN_STORE_PAYMENT,
                ADMIN_STORE_BRANDS,
                ADMIN_STORE_COUPONS,
                ADMIN_STORE_KEYS,
                ADMIN_STORE_BEACONS,
                ADMIN_STORE_TAGS,
                ADMIN_STORE_SOCIAL_NETWORKS,
                ACCESS_STORE_BO,
                EXECUTE_STORE_BO_OPERATION
        ]
    }

    public static Collection<PermissionType> seller() {
        [
                CREATE_STORE_CATALOGS,
                DELETE_STORE_CATALOGS,
                IMPORT_STORE_CATALOGS,
                EXPORT_STORE_CATALOGS
        ]
    }

    public static Collection<PermissionType> user() {
        [
                PUBLISH_STORE_CATALOGS_TO_ENV,
                UPDATE_STORE_CATALOG,
                UPDATE_STORE_CATEGORY_WITHIN_CATALOG
        ]
    }

}