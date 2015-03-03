package com.mogobiz.utils

import java.text.MessageFormat

/**
 *
 * Created by smanciot on 03/03/15.
 */
final class ProfileUtils {

    private ProfileUtils(){}

    public static final String WILDCARD_PERMISSION = 'org.apache.shiro.authz.permission.WildcardPermission'
    public static final String ALL = '*'

    static String computeStorePermission(PermissionType type, Long idStore){
        computePermission(type, idStore ? idStore.toString() : ALL)
    }

    static String computePermission(PermissionType type, String ... args){
        MessageFormat.format(type.name(), args)
    }

}
