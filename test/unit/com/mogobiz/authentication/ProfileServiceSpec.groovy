package com.mogobiz.authentication

import bootstrap.CommonService
import com.mogobiz.geolocation.domain.Location
import com.mogobiz.geolocation.domain.LocationValidation
import com.mogobiz.service.SanitizeUrlService
import com.mogobiz.store.domain.*
import com.mogobiz.utils.PermissionType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static com.mogobiz.utils.ProfileUtils.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ProfileService)
@Mock([Company, Location, Role, RolePermission, User, Permission, UserPermission, Profile, ProfilePermission])
class ProfileServiceSpec extends Specification {

    CommonService commonService

    def setup() {
        defineBeans {
            sanitizeUrlService(SanitizeUrlService)
        }

        commonService = new CommonService()

        Permission.metaClass.getPermissionValidation = {new PermissionValidation()}
        Permission permission = new Permission(type: WILDCARD_PERMISSION, possibleActions: ALL)
        commonService.saveEntity(permission)

        Location.metaClass.getLocationValidation = {new LocationValidation()}
        Location location = new Location(road1: "4 Place de la Défense", postalCode: "92800", city: "Puteaux", countryCode: 'FR')
        commonService.saveEntity(location)

        Company.metaClass.getCompanyValidation = {new CompanyValidation()}
        Company company = new Company(code: "mogobiz", name: "Mogobiz", location: location, website: "http://www.ebiznext.com", aesPassword:"5c3f3da15cae1bf2bc736b95bda10c78", email:"contact@mogobiz.com")
        commonService.saveEntity(company)

        Role.metaClass.getRoleValidation = {new RoleValidation()}
        Role admin = commonService.createRole(RoleName.SALESAGENT)

        Profile.metaClass.getProfileValidation = {new ProfileValidation()}

        ProfilePermission.metaClass.getProfilePermissionValidation = {new ProfilePermissionValidation()}
    }

    def cleanup() {
        // Tear down logic here
        resetGrailsApplication()
    }

    void "when getWildcardPermission is called the corresponding permission should be returned"() {
        when:
        def permission = service.getWilcardPermission()
        then:
        assertNotNull(permission)
        assertEquals(WILDCARD_PERMISSION, permission.type)
        assertEquals(ALL, permission.possibleActions)
    }

    void "when getProfilePermission is called the corresponding profile permission should be returned"() {
        given:
        Company company = Company.findByCode("mogobiz")
        def idStore = company.id
        def profile = new Profile(name: "profile", code: "profile", company: company)
        commonService.saveEntity(profile)
        def target = computePermission(PermissionType.ADMIN_COMPANY, idStore as String)
        log.info(target)
        ProfilePermission pp = new ProfilePermission(
                target: target,
                profile: profile,
                permission: service.getWilcardPermission()
        )
        commonService.saveEntity(pp)
        when:
        def profilePermission = service.getProfilePermission(profile, PermissionType.ADMIN_COMPANY, idStore as String)
        then:
        assertNotNull(profilePermission)
        assertEquals(profilePermission.target, pp.target)
        assertEquals(profilePermission.profile, pp.profile)
        assertEquals(profilePermission.permission, pp.permission)
    }

    void "when saveProfilePermission is called the corresponding profile permission should be added"() {
        given:
        Company company = Company.findByCode("mogobiz")
        def idStore = company.id
        def profile = new Profile(name: "profile", code: "profile", company: company)
        commonService.saveEntity(profile)
        def target = computePermission(PermissionType.ADMIN_COMPANY, idStore as String)
        log.info(target)
        when:
        service.saveProfilePermission(profile, true, PermissionType.ADMIN_COMPANY, idStore as String)
        then:
        def profilePermission = service.getProfilePermission(profile, PermissionType.ADMIN_COMPANY, idStore as String)
        assertNotNull(profilePermission)
        assertEquals(profilePermission.profile, profile)
    }

    void "when applying a profile to a store, a child profile with the matching store permissions should have been created"() {
        given:
        Company company = Company.findByCode("mogobiz")
        def idStore = company.id
        def name = "parent"
        def parent = new Profile(name: name, code: name)
        commonService.saveEntity(parent)
        PermissionType.admin().each {pt ->
            service.saveProfilePermission(parent, true, pt)
        }
        when:
        service.applyProfile(parent, idStore)
        then:
        def child = Profile.findByCompanyAndParent(company, parent)
        assertNotNull(child)
        assertEquals(parent.name, child.name)
        PermissionType.admin().each {pt ->
            def pp = service.getProfilePermission(child, pt, idStore as String)
            assertNotNull(pp)
            assertEquals(computePermission(pt, idStore as String), pp.target)
        }
    }

    void "when upgrading a parent profile, its child profiles with the matching store permissions should have been updating accordingly"() {
        given:
        Company company = Company.findByCode("mogobiz")
        def idStore = company.id
        def name = "parent"
        def parent = new Profile(name: name, code: name)
        commonService.saveEntity(parent)
        PermissionType.admin().each {pt ->
            service.saveProfilePermission(parent, true, pt)
        }
        service.applyProfile(parent, idStore)
        service.saveProfilePermission(parent, false, PermissionType.ADMIN_COMPANY)
        when:
        service.upgradeChildProfiles(parent)
        then:
        def child = Profile.findByCompanyAndParent(company, parent)
        assertNotNull(child)
        assertEquals(parent.name, child.name)
        def pp = service.getProfilePermission(child, PermissionType.ADMIN_COMPANY, idStore as String)
        assertNull(pp)
        PermissionType.admin().each {pt ->
            if(!(pt in [PermissionType.ADMIN_COMPANY])){
                pp = service.getProfilePermission(child, pt, idStore as String)
                assertNotNull(pp)
                assertEquals(computePermission(pt, idStore as String), pp.target)
            }
        }
    }

    void "when copying a profile to a store, a child profile with the matching store permissions should have been created"() {
        given:
        Company company = Company.findByCode("mogobiz")
        def idStore = company.id
        def name = "copy"
        def parent = new Profile(name: "parent", code: "parent")
        commonService.saveEntity(parent)
        PermissionType.admin().each {pt ->
            service.saveProfilePermission(parent, true, pt)
        }
        when:
        service.copyProfile(parent, idStore, name)
        then:
        def child = Profile.findByCompanyAndName(company, name)
        assertNotNull(child)
        assertNull(child.parent)
        assertEquals(name, child.name)
        PermissionType.admin().each {pt ->
            def pp = service.getProfilePermission(child, pt, idStore as String)
            assertNotNull(pp)
            assertEquals(computePermission(pt, idStore as String), pp.target)
        }
    }

// FIXME   void "when removing a profile to a store, all child profiles with the matching parent profile should have been removed"() {
//        given:
//        Company company = Company.findByCode("mogobiz")
//        def idStore = company.id
//        def name = "child"
//        def parent = new Profile(name: "parent", code: "parent")
//        commonService.saveEntity(parent)
//        PermissionType.admin().each {pt ->
//            service.saveProfilePermission(parent, true, pt)
//        }
//        service.applyProfile(parent, idStore, name)
//        when:
//        service.removeProfile(parent)
//        then:
//        def child = Profile.findByCompanyAndName(company, name)
//        assertNull(child)
//    }

}
