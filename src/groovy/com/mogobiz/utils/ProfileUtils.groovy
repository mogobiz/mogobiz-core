package com.mogobiz.utils

import java.text.MessageFormat
import java.util.regex.Pattern

/**
 *
 * Created by smanciot on 03/03/15.
 */
final class ProfileUtils {

    private ProfileUtils(){}

    public static final String WILDCARD_PERMISSION = 'org.apache.shiro.authz.permission.WildcardPermission'
    public static final String ALL = '*'
    public static final Pattern ARG_PATTERN = ~/([0-9]|\*)+/

    static String computeStorePermission(PermissionType type, Long idStore){
        computePermission(type, idStore ? idStore.toString() : ALL)
    }

    static String computePermission(PermissionType type, String ... args){
        MessageFormat.format(type.getValue(), args)
    }

    static PermissionType retrievePermissionFrom(String target){
        final args = target.split(":").findAll {ARG_PATTERN.matcher(it).matches()}
        PermissionType.values().find {computePermission(it, args.toArray(new String[args.size()])).equals(target)}
    }

}
