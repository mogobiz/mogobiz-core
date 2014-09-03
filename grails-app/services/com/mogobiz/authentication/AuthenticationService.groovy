package com.mogobiz.authentication

import com.mogobiz.store.domain.*
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.subject.Subject

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class AuthenticationService {

	Seller retrieveAuthenticatedSeller(){
		def seller = null
		Subject subject = SecurityUtils.getSubject()
		Object principal = subject?.getPrincipal()
		if (principal != null && subject.isAuthenticated()) {
			def login = principal.toString()
			seller = Seller.findByLogin(login)
		}
		return seller
	}

	User retrieveAuthenticatedUser(){
		def user = null
		Subject subject = SecurityUtils.getSubject()
		Object principal = subject?.getPrincipal()
		if (principal != null && subject.isAuthenticated()) {
			def login = principal.toString()
			user = User.findByLogin(login)
		}
		return user
	}

    ExternalAccount retrieveExternalAccount(long userId, AccountType accountType){
        def externalAccount
        def externalAccounts = ExternalAccount.findByUserAndAccountType(User.findById(userId), accountType)
        if(externalAccounts && externalAccounts.iterator().hasNext()){
            externalAccount = externalAccounts.iterator().next()
        }
        return externalAccount
    }
	String retrieveExternalAccountToken(AccountType accountType){
		ExternalAccount account = null
		def user = retrieveAuthenticatedUser()
		if(user){
			account = ExternalAccount.findByUserAndAccountType(user, accountType)
		}
		return account ? account.token : null
	}

	/**
	 * @deprecated
	 * @return true if authenticated user has role {@link RoleName#ADMINISTRATOR}
	 */
	boolean isAdministrator() {
		return SecurityUtils.getSubject().hasRole(RoleName.ADMINISTRATOR.key)
	}
	
	/**
	* @deprecated
	* @return true if authenticated user has role {@link RoleName#VALIDATOR}
	*/
   boolean isValidator() {
	   return SecurityUtils.getSubject().hasRole(RoleName.VALIDATOR.key)
   }

	/**
	 * @return true if authenticated user has admin permission for all stores
	 */
	boolean canAdminAllStores() {
		return SecurityUtils.getSubject().isPermitted('company:*:admin')
	}

	/**
	 * @param idStore - id store
	 * @return true if authenticated user has admin permission for this store
	 */
	boolean canAdminStore(long idStore) {
		return SecurityUtils.getSubject().isPermitted('company:'+idStore+':admin')
	}

	/**
	 * @param store - store
	 * @return true if authenticated user has admin permission for this store or is a seller for this store
	 */
	boolean canAccessStore(Company store) {
		def authorized = store && canAdminStore(store.id)
		if(store && !authorized){
			def seller = retrieveAuthenticatedSeller()
			authorized = store == seller?.company
		}
		return authorized
	}

    // begin OAuth 2

    /**
     *
     * @param token - the access token
     * @return User
     */
    User access(String token){
        Collection<Token> accessTokens = Token.findAllByValue(token).findAll { !hasExpired(it) }
        return accessTokens.isEmpty() ? null : accessTokens.iterator().next().user
    }

    /**
     *
     * @param token - the user access token
     * @return true if the user access token has not expired
     */
    boolean hasExpired(Token token){
        boolean ret = false
        if(token.expiresIn > 0){
            ret = (token.creationDate.time + token.expiresIn) < System.currentTimeMillis()
        }
        return ret
    }

    // end OAuth 2

    /**
     *
     * @param username - user name
     * @param password - password
     * @param rememberMe -
     * @return User
     * @throws AuthenticationException
     */
    User authenticate(String username, String password, boolean rememberMe) throws AuthenticationException
    {
        UsernamePasswordToken authToken = new UsernamePasswordToken(username, password)
        authToken.rememberMe = rememberMe
        try{
            SecurityUtils.getSubject().login(authToken)
            User.findByLogin(username)
        }
        catch (AuthenticationException ex){
            def error = ex.message//"Invalid username and/or password"
            def incorrectCredentials = ex instanceof IncorrectCredentialsException
            if(incorrectCredentials){
                def maxTries = grailsApplication.config.securisation?.essai?.auth ?
                        grailsApplication.config.securisation?.essai?.auth : 5
                User user = User.findByLogin(username)
                def nbFailures = user.nbFailures + 1
                user.nbFailures = nbFailures
                if(nbFailures > maxTries){
                    user.blocked = true
                }
                user.save(flush : true)
            }
            throw ex
        }
    }
}
