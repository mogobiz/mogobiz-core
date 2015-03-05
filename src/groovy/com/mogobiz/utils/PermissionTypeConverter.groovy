package com.mogobiz.utils

import org.grails.databinding.converters.ValueConverter

/**
 *
 * Created by smanciot on 05/03/15.
 */
class PermissionTypeConverter implements ValueConverter{
    @Override
    boolean canConvert(Object o) {
        return PermissionType.keys().contains(o.toString())
    }

    @Override
    Object convert(Object o) {
        return PermissionType.findByKey(o.toString())
    }

    @Override
    Class<?> getTargetType() {
        return PermissionType.class
    }
}
