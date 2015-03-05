package com.mogobiz.utils

/**
 *
 * Created by smanciot on 03/03/15.
 */
enum PermissionType {

    ADMIN_COMPANY("updateCompany", "company:{0}:admin"),
    ADMIN_STORE_PROFILES("updateProfiles", "profiles:{0}:admin"),
    ADMIN_STORE_USERS("updateUsers", "users:{0}:admin"),
    ADMIN_STORE_SHIPPING("updateShipping", "shipping:{0}:admin"),
    ADMIN_STORE_TAXES("updateTaxes", "taxes:{0}:admin"),
    ADMIN_STORE_PAYMENT("updatePayment", "payment:{0}:admin"),
    ADMIN_STORE_BRANDS("updateBrands", "brands:{0}:admin"),
    ADMIN_STORE_COUPONS("updateCoupons", "coupons:{0}:admin"),
    ADMIN_STORE_KEYS("updateKeys", "keys:{0}:admin"),
    ADMIN_STORE_BEACONS("updateBeacons", "beacons:{0}:admin"),
    ADMIN_STORE_TAGS("updateTags", "tags:{0}:admin"),

    ADMIN_STORE_SOCIAL_NETWORKS("updateNetworks", "networks:{0}:admin"),

    ACCESS_STORE_BO("showBo", "bo:{0}:show"),
    EXECUTE_STORE_BO_OPERATION("updateBoOperations", "bo:{0}:admin"),

    PUBLISH_STORE_CATALOGS_TO_ENV("publishCatalogs", "catalogs:{0}:publish:{1}"), // UserPermission per environment
    CREATE_STORE_CATALOGS("createCatalogs", "catalogs:{0}:create"),
    DELETE_STORE_CATALOGS("deleteCatalogs", "catalogs:{0}:delete"),
    IMPORT_STORE_CATALOGS("importCatalogs", "catalogs:{0}:import"),
    EXPORT_STORE_CATALOGS("exportCatalogs", "catalogs:{0}:export"),
    UPDATE_STORE_CATALOG("updateCatalog", "catalogs:{0}:catalog:{1}:update"), // UserPermission per catalog
    UPDATE_STORE_CATEGORY_WITHIN_CATALOG("updateCatalogCategory", "catalogs:{0}:catalog:{1}:category:{2}:update") // UserPermission per catalog and category

    private String key

    private String value

    private PermissionType(String key, String value){
        this.key = key
        this.value = value
    }

    public String getKey(){
        key
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

    public static Set<String> keys(){
        values().collect {it.key}.toSet()
    }

    public static PermissionType findByKey(String key){
        values().find {it.key == key}
    }

    public static Collection<PermissionType> minus(Collection<PermissionType> excluded){
        values().toList().minus(excluded)
    }
}