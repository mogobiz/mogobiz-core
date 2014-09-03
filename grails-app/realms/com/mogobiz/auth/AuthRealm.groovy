/**
 * 
 */
package com.mogobiz.auth

import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.RolePermission
import com.mogobiz.store.domain.User
import com.mogobiz.store.domain.UserPermission
import org.apache.shiro.authc.*

/**
 * @version $Id $
 *
 */
class AuthRealm {
	/**
	 * The authentication token supported by this realm. This is
	 * often UsernamePasswordToken, in which authentication is
	 * performed via a given username and password. An alternative
	 * might be a GpgAuthToken that encodes the user's GPG key
	 * information.
	 *
	 * If this property does not exist, the realm does not take
	 * part in authentication, even if {{authenticate()}} exists.
	 */
	static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken

	/**
	 * The credentialMatcher property is injected by Spring at runtime.
	 * The default implementation used is the
	 * org.apache.shiro.authc.credential.Sha1CredentialsMatcher, which expects that credentials
	 * that are stored are SHA hashed.    
	 */
	org.apache.shiro.authc.credential.CredentialsMatcher credentialMatcher

	/**
	 * Authenticates a user via the given authentication token. The
	 * token will be an instance of 'authTokenClass'. Should return
	 * a principal or {{org.apache.shiro.authc.Account}} instance if
	 * the authentication succeeds, or throw an
	 * {{org.apache.shiro.authc.AuthenticationException}} if it does not.
	 *
	 * If this method is not present, the realm does not take part
	 * in authentication.
	 */
	def authenticate(authToken){
		def username = authToken.username
		// Null username is invalid
		if (username == null) {
			throw new AccountException('Null usernames are not allowed by this realm.')
		}
		// Get the user with the given username. If the user is not
		// found, then they don't have an account and we throw an
		// exception.
		def user = User.findByLogin(username)
		if (!user) {
			throw new UnknownAccountException("No account found for user $username")
		}
		// check if the account has been disabled
		if(!user.active){
			throw new DisabledAccountException("Account for user $username has been disabled")
		}
		// Now check the user's password against the hashed value stored
		// in the database.
		def account = new SimpleAccount(username, user.password, "AuthRealm")
		if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
			throw new IncorrectCredentialsException("Invalid password for $username")
		}
		return account
	}

	/**
	 * Determines whether a user has a particular role or not. It
	 * should return {{true}} if the user has the role, or {{false}}
	 * otherwise. {{principal}} is the principal returned by the
	 * {{authenticate()}} method, while {{roleName}} is simply a
	 * string.
	 */
	def hasRole(principal, roleName) {
		def user = User.findByLogin(principal, [fetch:[roles:'join']])
		return user.roles.any { it.name == RoleName.valueOf(roleName) }
	}

	/**
	 * Determines whether a user has a set of particular roles or not. It
	 * should return {{true}} if the user has been granted the roles, or {{false}}
	 * otherwise. {{principal}} is the principal returned by the
	 * {{authenticate()}} method, while {{roles}} is simply an array of
	 * string.
	 */
	def hasAllRoles(principal, roles) {
		def user = User.findByLogin(principal, [fetch:[roles:'join']])
		return roles.every { roleName ->  user.roles.any { it.name == RoleName.valueOf(roleName) }};
	}

	/**
	 * Determines whether a user has a particular permission or not.
	 */
	def isPermitted(principal, requiredPermission){
		// Does the user have the given permission directly associated
		// with himself?
		//
		// First find all the permissions that the user has that match
		// the required permission's type and project code.
		def permissions = UserPermission.createCriteria().list {
			user { eq('login', principal) }
			permission {
				eq('type', requiredPermission.class.name)
			}
		}

		// Try each of the permissions found and see whether any of
		// them confer the required permission.
		def retval = permissions?.find { rel ->
			// Create a real permission instance from the database
			// permission.
			def perm = null
			def constructor = findConstructor(rel.permission.type)
			if (constructor.parameterTypes.size() == 2) {
				perm = constructor.newInstance(rel.target, rel.actions)
			}
			else if (constructor.parameterTypes.size() == 1) {
				perm = constructor.newInstance(rel.target)
			}
			else {
				log.error "Unusable permission: ${rel.permission.type}"
				return false
			}

			// Now check whether this permission implies the required
			// one.
			if (perm.implies(requiredPermission)) {
				// User has the permission!
				return true
			}
			else {
				return false
			}
		}

		if (retval != null) {
			// Found a matching permission!
			return true
		}

		// If not, does he gain it through a role?
		//
		// First, find the roles that the user has.
		def user = User.findByLogin(principal, [fetch:[roles:'join']])
		def roles = user.roles

		// If the user has no roles, then he obviously has no permissions
		// via roles.
		if (roles.isEmpty()) return false

		// Get the permissions from the roles that the user does have.
		def results = RolePermission.createCriteria().list {
			'in'('role', roles)
			permission {
				eq('type', requiredPermission.class.name)
			}
		}

		// There may be some duplicate entries in the results, but
		// at this stage it is not worth trying to remove them. Now,
		// create a real permission from each result and check it
		// against the required one.
		retval = results.find { rel ->
			def perm = null
			def constructor = findConstructor(rel.permission.type)
			if (constructor.parameterTypes.size() == 2) {
				perm = constructor.newInstance(rel.target, rel.actions)
			}
			else if (constructor.parameterTypes.size() == 1) {
				perm = constructor.newInstance(rel.target)
			}
			else {
				log.error "Unusable permission: ${rel.permission.type}"
				return false
			}

			// Now check whether this permission implies the required
			// one.
			if (perm.implies(requiredPermission)) {
				// User has the permission!
				return true
			}
			else {
				return false
			}
		}

		if (retval != null) {
			// Found a matching permission!
			return true
		}
		else {
			return false
		}
	}

	def findConstructor(className) {
		// Load the required permission class.
		def clazz = this.class.classLoader.loadClass(className)

		// Check the available constructors. If any take two
		// string parameters, we use that one and pass in the
		// target and actions string. Otherwise we try a single
		// parameter constructor and pass in just the target.
		def preferredConstructor = null
		def fallbackConstructor = null
		clazz.declaredConstructors.each { constructor ->
			def numParams = constructor.parameterTypes.size()
			if (numParams == 2) {
				if (constructor.parameterTypes[0].equals(String) &&
				constructor.parameterTypes[1].equals(String)) {
					preferredConstructor = constructor
				}
			}
			else if (numParams == 1) {
				if (constructor.parameterTypes[0].equals(String)) {
					fallbackConstructor = constructor
				}
			}
		}

		return (preferredConstructor != null ? preferredConstructor : fallbackConstructor)
	}
}

