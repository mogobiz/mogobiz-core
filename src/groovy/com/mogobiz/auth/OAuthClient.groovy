/**
 * 
 */
package com.mogobiz.auth

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class OAuthClient {

	final String clientId
	final String redirectURI
	final String clientSecret


	public OAuthClient(String clientId, String redirectURI, String clientSecret) {
		this.clientId = clientId
		this.redirectURI = redirectURI
		this.clientSecret = clientSecret
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true
		}
		if(!obj instanceof OAuthClient){
			return false
		}
		else{
			return clientId.equals(((OAuthClient)obj).clientId)
		}
	}

}
