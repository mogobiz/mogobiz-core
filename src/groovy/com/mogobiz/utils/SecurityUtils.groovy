package com.mogobiz.utils


import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.crypto.hash.Sha256Hash

import com.mogobiz.store.domain.AccountType
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.ExternalAccount
import com.mogobiz.store.domain.Permission
import com.mogobiz.store.domain.Role
import com.mogobiz.store.domain.RoleName
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.UserPermission

class SecurityUtils {
	public static Seller extractSeller(String login, String password, AccountType accountType, String externalId, String token, String tokenSecret,
			String firstName, String lastName, String email, boolean innerAuth, String storeName) {
		Seller seller = null
		def externalAccount = ExternalAccount.findByLoginAndAccountType(login, accountType)
		if (!externalAccount) {
			externalAccount = new ExternalAccount()
			seller = Seller.findByLogin(login)
		}
		if (!seller) {
			Company company = Company.findByCode(storeName)
			if(!company){
				company = new Company(code:storeName, name:storeName, aesPassword:SecureCodec.genKey())
				if(company.validate()) {
					company.save(flush:true)
				}
			}
			def partner = Role.findByName(RoleName.PARTNER)
			seller = new Seller(
					login:login,
					email:email,
					password:new Sha256Hash(password).toHex(),
					active:true,
					accountType:accountType,
					externalId:externalId,
					firstName:firstName,
					lastName:lastName,
					company:company)
			seller.addToRoles(partner)
			if(seller.validate()) {
				seller.save(flush:true)
			}
		}
		else{
			seller.password = new Sha256Hash(password).toHex()
		}
		externalAccount.login = login
		externalAccount.accountType = accountType
		externalAccount.externalId = externalId
		externalAccount.token = token
		externalAccount.tokenSecret = tokenSecret
		externalAccount.user = seller
		if (externalAccount.validate()) {
			externalAccount.save(flush:true)
		}
		seller.admin = true
		seller.save(flush:true)

		def permission = Permission.findByTypeAndPossibleActions(
				'org.apache.shiro.authz.permission.WildcardPermission',
				'*')
		if(permission){
			UserPermission userPermission = UserPermission.createCriteria().get{
				eq('permission.id', permission?.id)
				eq('user.id', seller.id)
				eq('target', 'company:'+seller.company?.id+':admin')
				eq('actions', '*')
			}
			if(!userPermission){
				userPermission = new UserPermission(
						permission:permission,
						user:seller,
						target:'company:'+seller.company?.id+':admin',
						actions:'*'
						)
				userPermission.save(flush:true)
			}
		}
		if(innerAuth){
			try{
				def authToken = new UsernamePasswordToken(login, password)
				authToken.rememberMe = true
				org.apache.shiro.SecurityUtils.subject.login(authToken)
			}
			catch(Throwable th){
				th.printStackTrace()
			}
		}
		return seller
	}
}
