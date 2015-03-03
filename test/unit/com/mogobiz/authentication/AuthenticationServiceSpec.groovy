package com.mogobiz.authentication

import bootstrap.CommonService
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.PermissionValidation
import com.mogobiz.store.domain.Profile
import com.mogobiz.store.domain.ProfilePermission
import com.mogobiz.store.domain.Role
import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.RolePermission
import com.mogobiz.store.domain.RolePermissionValidation
import com.mogobiz.store.domain.RoleValidation
import com.mogobiz.store.domain.User
import com.mogobiz.store.domain.UserPermission
import com.mogobiz.store.domain.UserValidation
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.codehaus.groovy.grails.plugins.codecs.HexCodec
import org.codehaus.groovy.grails.plugins.codecs.SHA256BytesCodec
import spock.lang.Specification

/**
 *
 * Created by smanciot on 03/03/15.
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(AuthenticationService)
@Mock([Company, Role, RolePermission, User, UserPermission, Permission, Profile, ProfilePermission])
class AuthenticationServiceSpec extends Specification{

    public static final String CLEAR_PASSWORD = "changeit"

    CommonService commonService

    def setupSpec() {
        Holders.config.superadmin = [
                login: "login",
                email: "admin@mogobiz.com",
                password: HexCodec.encode(SHA256BytesCodec.encode(CLEAR_PASSWORD))
        ]
    }

    def cleanupSpec() {
        // Tear down logic here
        resetGrailsApplication()
    }

    def setup(){
        SecurityUtils.metaClass.'static'.getSubject = { ->
            MockSubject.getInstance()
        }
        SecurityUtils.getSubject().logout()
        commonService = new CommonService()

        Role.metaClass.getRoleValidation = {new RoleValidation()}
        Role admin = commonService.createRole(RoleName.ADMINISTRATOR)

        Permission.metaClass.getPermissionValidation = {new PermissionValidation()}
        Permission permission = new Permission(type: service.WILDCARD_PERMISSION, possibleActions: service.ALL)
        commonService.saveEntity(permission)

        RolePermission.metaClass.getRolePermissionValidation = {new RolePermissionValidation()}
        RolePermission adminCompanyRolePermission = new RolePermission(permission : permission, role : admin, target : 'company:*:admin')
        commonService.saveEntity(adminCompanyRolePermission)

        // création de l'admin
        User.metaClass.getUserValidation = {new UserValidation()}
        User userAdmin = new User(login:Holders.config.superadmin.login, email:Holders.config.superadmin.email, password:Holders.config.superadmin.password, active:true)
        userAdmin.addToRoles(admin)
        commonService.saveEntity(userAdmin)
    }

    def "authentication should fail when no user matches the login provided" (){
        when:
        service.authenticate("fake", "fake", false)
        then:
        def e = thrown(UnknownAccountException)
        e.message == "No account found for user fake"
    }

    def "authentication should fail when bad credentials have been provided" (){
        when:
        service.authenticate(Holders.config.superadmin.login, "fake", false)
        then:
        def e = thrown(IncorrectCredentialsException)
        e.message == "Invalid password for ${Holders.config.superadmin.login}"
    }

    def "authentication should fail when account has been disabled" (){
        given:
        User admin = User.findByLogin(Holders.config.superadmin.login)
        admin.active = false
        admin.save()
        when:
        service.authenticate(Holders.config.superadmin.login, Holders.config.superadmin.password, false)
        then:
        def e = thrown(DisabledAccountException)
        e.message == "Account for user ${Holders.config.superadmin.login} has been disabled"
    }

    def "authentication should succeed when expected credentials have been provided" (){
        when:
        service.authenticate(Holders.config.superadmin.login, CLEAR_PASSWORD, false)
        then:
        assertTrue(SecurityUtils.getSubject().isAuthenticated())
        assertEquals(Holders.config.superadmin.login, SecurityUtils.getSubject().principal as String)
        assertTrue(service.isAdministrator())
        assertTrue(service.isPermitted("company:*:admin"))
        def user = service.retrieveAuthenticatedUser()
        assertNotNull(user)
        assertEquals(Holders.config.superadmin.login, user.login)
    }

}
