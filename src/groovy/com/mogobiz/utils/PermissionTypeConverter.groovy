package com.mogobiz.utils

import groovy.util.logging.Log4j
import org.grails.databinding.converters.ValueConverter

/**
 *
 * Created by smanciot on 05/03/15.
 */
@Log4j
class PermissionTypeConverter implements ValueConverter{
    @Override
    boolean canConvert(Object o) {
        def key = o.toString()
        return PermissionType.keys().contains(key)
    }

    @Override
    Object convert(Object o) {
        def key = o.toString()
        def permission = PermissionType.findByKey(key)
        log.info("converting $key to ${permission.value}")
        return permission
    }

    @Override
    Class<?> getTargetType() {
        return PermissionType.class
    }
}
