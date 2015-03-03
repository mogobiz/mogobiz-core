/**
 * 
 */
package com.mogobiz.authentication

import com.mogobiz.auth.AuthRealm
import groovy.util.logging.Log4j
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.Sha256CredentialsMatcher
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.session.Session
import org.apache.shiro.subject.ExecutionException
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.Subject

import java.util.concurrent.Callable

/**
 * @version $Id $
 *
 */
@Log4j
final class MockSubject implements Subject{

    private SimpleAccount account = null
    private Object principal

    private static AuthRealm realm

    private static ThreadLocal<Subject> TLS_SUBJECT = new ThreadLocal < Subject >(){
        protected Subject initialValue(){
            return new MockSubject()
        }
    } 

    static {
        realm = new AuthRealm()
        realm.credentialMatcher = new Sha256CredentialsMatcher()        
    }

    private MockSubject(){}

    public static Subject getInstance(){
        return TLS_SUBJECT.get()
    }

    @Override
    public void checkPermission(Permission permission)
    throws AuthorizationException {
        if(!isPermitted(permission)){
            throw new AuthorizationException()
        }
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {
        if(!isPermitted(permission)){
            throw new AuthorizationException()
        }
    }

    @Override
    public void checkPermissions(Collection<Permission> permissions)
    throws AuthorizationException {
        if(!isPermittedAll(permissions)){
            throw new AuthorizationException()
        }
    }

    @Override
    public void checkPermissions(String... permissions)
    throws AuthorizationException {
        if(!isPermittedAll(permissions)){
            throw new AuthorizationException()
        }
    }

    @Override
    public void checkRole(String role) throws AuthorizationException {
        if(!hasRole(role)){
            throw new AuthorizationException()
        }
    }

    @Override
    public void checkRoles(Collection<String> roles)
    throws AuthorizationException {
        if(!hasAllRoles(roles)){
            throw new AuthorizationException()
        }
    }

    @Override
    void checkRoles(String... roles) throws AuthorizationException {
        roles.each {role ->
            checkRole(role)
        }
    }

    @Override
    public Object getPrincipal() {
        return principal
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return account ? account.getPrincipals() : null
    }

    @Override
    public Session getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Session getSession(boolean arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasAllRoles(Collection<String> roles) {
        return roles.every{role -> hasRole(role) }
    }

    @Override
    public boolean hasRole(String role) {
        return realm.hasRole(principal as String, role)
    }

    @Override
    public boolean[] hasRoles(List<String> roles) {
        return realm.hasAllRoles(principal as String, roles.toArray(new String[roles?.size() ?:0]))
    }

    @Override
    public boolean isAuthenticated() {
        return account != null
    }

    @Override
    public boolean[] isPermitted(List<Permission> permissions) {
        List<Boolean> ret = []
        permissions.each {permission -> ret << isPermitted(permission) }
        ret.toArray(new boolean[ret.size()])
    }

    @Override
    public boolean isPermitted(Permission permission) {
        return realm.isPermitted(principal as String, permission)
    }

    @Override
    public boolean[] isPermitted(String... permissions) {
        boolean[] ret = new boolean[permissions.length]
        permissions.eachWithIndex {permission, index ->
            ret[index] = isPermitted(permission)
        }
        ret
    }

    @Override
    public boolean isPermitted(String permission) {
        log.info("$principal isPermitted($permission)")
        return realm.isPermitted(principal as String, new WildcardPermission(permission))
    }

    @Override
    public boolean isPermittedAll(Collection<Permission> permissions) {
        return permissions.every {permission -> isPermitted(permission) }
    }

    @Override
    public boolean isPermittedAll(String... permissions) {
        return permissions.every {permission -> isPermitted(permission) }
    }

    @Override
    public void login(AuthenticationToken token)
    throws AuthenticationException {
        account = realm.authenticate(token as UsernamePasswordToken)
        principal = token.principal
    }

    @Override
    public void logout() {
        account = null
        principal = null
    }

    @Override
    def <V> V execute(Callable<V> callable) throws ExecutionException {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    void execute(Runnable runnable) {
        // TODO Auto-generated method stub
    }

    @Override
    def <V> Callable<V> associateWith(Callable<V> callable) {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    Runnable associateWith(Runnable runnable) {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    void runAs(PrincipalCollection principalCollection) throws NullPointerException, IllegalStateException {
        // TODO Auto-generated method stub
    }

    @Override
    boolean isRunAs() {
        // TODO Auto-generated method stub
        return false
    }

    @Override
    PrincipalCollection getPreviousPrincipals() {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    PrincipalCollection releaseRunAs() {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    public boolean isRemembered(){
        // TODO Auto-generated method stub
        return false
    }
}
