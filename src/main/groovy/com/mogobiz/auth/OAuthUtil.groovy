/**
 * 
 */
package com.mogobiz.auth

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang.StringUtils
import org.apache.oltu.oauth2.common.OAuth
import grails.core.GrailsApplication

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
final class OAuthUtil {

	private static OAuthUtil instance

	private final Set<OAuthClient> clients = []

	private OAuthUtil(GrailsApplication grailsApplication){
		Map oauth = grailsApplication.config['oauth'] ? grailsApplication.config['oauth'] as Map : [:]
		oauth.keySet().each { key ->
			Map m = oauth[key] as Map
			String clientId = m[OAuth.OAUTH_CLIENT_ID]
			String redirectURI = m[OAuth.OAUTH_REDIRECT_URI]
			String clientSecret = m[OAuth.OAUTH_CLIENT_SECRET]
			if(!StringUtils.isEmpty(clientId)
				&& !StringUtils.isEmpty(redirectURI)
				&& !StringUtils.isEmpty(clientSecret)){
				clients << new OAuthClient(clientId, redirectURI, clientSecret)
			}
		}
	}

	def static OAuthUtil getInstance(GrailsApplication grailsApplication){
		if(instance == null){
			instance = new OAuthUtil(grailsApplication)
		}
		instance
	}

	boolean checkClient(String clientId){
		return findByClientId(clientId) != null
	}

	boolean validateClient(String clientId, String redirectURI){
		OAuthClient client = findByClientId(clientId)
		return client ? client.redirectURI.equals(redirectURI) : false
	}

    OAuthClient parseBasicAuthentication(String authorization){
        OAuthClient client = null
        String clientSecret = null
        if(authorization){
            def args = authorization.trim().split(' ')
            if(args.length == 2 && args[0].equalsIgnoreCase('Basic') ){
                def decoded = new String(Base64.decodeBase64(args[1])).split(':')
                if(decoded.length == 2){
                    client = findByClientId(decoded[0])
                    clientSecret = decoded[1]
                }
            }
        }
        return client && client.clientSecret.equals(clientSecret) ? client : null
    }

    boolean authenticateClient(String clientId, String clientSecret){
		OAuthClient client = findByClientId(clientId)
		return client ? client.clientSecret.equals(clientSecret) : false
	}

	private OAuthClient findByClientId(String clientId){
		clients.find {c ->
			c.clientId.equals(clientId)
		}
	}
}
