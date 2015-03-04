package com.mogobiz.authentication

import bootstrap.CommonService
import com.mogobiz.geolocation.domain.Location
import com.mogobiz.geolocation.domain.LocationValidation
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.CompanyValidation
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.PermissionValidation
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.ProfilePermission
import com.mogobiz.store.domain.ProfilePermissionValidation
import com.mogobiz.store.domain.ProfileValidation
import com.mogobiz.utils.PermissionType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static com.mogobiz.utils.ProfileUtils.ALL
import static com.mogobiz.utils.ProfileUtils.WILDCARD_PERMISSION
import static com.mogobiz.utils.ProfileUtils.computePermission

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ProfileService)
@Mock([Company, Location, Permission, Profile, ProfilePermission])
class ProfileServiceSpec extends Specification {

    CommonService commonService

    def setup() {
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
        def profile = new Profile(name: "profile", company: company)
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
        def profile = new Profile(name: "profile", company: company)
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
}
