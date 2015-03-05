package com.mogobiz.utils

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 *
 * Created by smanciot on 05/03/15.
 */
@TestMixin(GrailsUnitTestMixin)
class ProfileUtilsSpec extends Specification{

    def setup(){}

    def cleanup(){}

    def "when permission target is specific then the corresponding permission should be retrieved"(){
        given:
        def target = "company:15:admin"
        when:
        def permission = ProfileUtils.retrievePermissionFrom(target)
        then:
        assertNotNull(permission)
        assertEquals(PermissionType.ADMIN_COMPANY, permission)
    }

    def "when permission target is generic then the corresponding permission should be retrieved"(){
        given:
        def target = "company:*:admin"
        when:
        def permission = ProfileUtils.retrievePermissionFrom(target)
        then:
        assertNotNull(permission)
        assertEquals(PermissionType.ADMIN_COMPANY, permission)
    }

    def "when permission target is partially generic then the corresponding permission should be retrieved"(){
        given:
        def target = "company:1*:admin"
        when:
        def permission = ProfileUtils.retrievePermissionFrom(target)
        then:
        assertNotNull(permission)
        assertEquals(PermissionType.ADMIN_COMPANY, permission)
    }

    def "when permission target is invalid then no permission should be retrieved"(){
        given:
        def target = "company:1a:admin"
        when:
        def permission = ProfileUtils.retrievePermissionFrom(target)
        then:
        assertNull(permission)
    }
}
