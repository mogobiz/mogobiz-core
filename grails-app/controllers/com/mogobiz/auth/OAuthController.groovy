package com.mogobiz.auth

import com.mogobiz.store.domain.Token
import com.mogobiz.store.domain.User
import grails.transaction.Transactional
import org.apache.oltu.oauth2.as.issuer.MD5Generator
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest
import org.apache.oltu.oauth2.common.OAuth
import org.apache.oltu.oauth2.common.error.OAuthError
import org.apache.oltu.oauth2.common.exception.OAuthProblemException
import org.apache.oltu.oauth2.common.message.OAuthResponse
import org.apache.oltu.oauth2.common.message.types.TokenType
import org.apache.shiro.authc.AuthenticationException

import javax.servlet.http.HttpServletResponse

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class OAuthController {

	def authenticationService

	@Transactional
	def authorize() {
		try {
			String redirectURI = request.getParameter(OAuth.OAUTH_REDIRECT_URI)
			String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID)
			String responseType = request.getParameter(OAuth.OAUTH_RESPONSE_TYPE)

			// If the request fails due to a missing, invalid, or mismatching
			// redirection URI, or if the client identifier is missing or invalid,
			// the authorization server SHOULD inform the resource owner of the
			// error and MUST NOT automatically redirect the user-agent to the
			// invalid redirection URI.
	
			// the client identifier is missing
			if(!clientId) {
				OAuthProblemException e = OAuthProblemException
				.error(OAuthError.CodeResponse.INVALID_REQUEST, 'Missing client_id parameter value')
				.responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
				throw e
			}

            OAuthUtil oauthUtil = OAuthUtil.getInstance(grailsApplication)

			// the client identifier is invalid
			if(!oauthUtil.checkClient(clientId)) {
				OAuthProblemException e = OAuthProblemException
				.error(OAuthError.CodeResponse.INVALID_REQUEST, 'Invalid client_id parameter value')
				.responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
				throw e
			}

            // the redirection URI is missing
            if(!redirectURI) {
                OAuthProblemException e = OAuthProblemException
                .error(OAuthError.CodeResponse.INVALID_REQUEST, 'Missing redirect_uri parameter value')
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST)
                throw e
            }

            redirectURI = URLDecoder.decode(redirectURI, 'UTF-8')

            // the redirection URI is mismatching
			if(!oauthUtil.validateClient(clientId, redirectURI)) {
				OAuthProblemException e = OAuthProblemException
				.error(OAuthError.CodeResponse.INVALID_REQUEST, 'Invalid redirect_uri parameter value')
				.responseStatus(HttpServletResponse.SC_BAD_REQUEST)
				throw e
			}
	
			//dynamically recognize an OAuth profile based on request characteristic (params,
			// method, content type etc.), perform validation
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request)

			//build response
			OAuthResponse resp

            String scope = oauthRequest.getParam(OAuth.OAUTH_SCOPE)
            // TODO check scope

            String state = oauthRequest.getParam(OAuth.OAUTH_STATE)

			User user = authenticationService.retrieveAuthenticatedUser()
	
			if(user == null){
				String username = oauthRequest.getParam(OAuth.OAUTH_USERNAME)
				String password = oauthRequest.getParam(OAuth.OAUTH_PASSWORD)

				if(username && password && request.method == 'POST'){
					boolean rememberMe = request.getParameter('rememberMe') ? request.getParameter('rememberMe').asBoolean() : false
					try{
						user = authenticationService.authenticate(username, password, rememberMe)
					}
					catch (AuthenticationException ex){
						flash.message = ex.message
						render(view: 'login', model: [client_id:clientId, redirect_uri:redirectURI, response_type:responseType, state:state, scope:scope])
						return
					}
				}
				else{
					render(view: 'login', model: [client_id:clientId, redirect_uri:redirectURI, response_type:responseType, state:state, scope:scope])
					return
				}
			}

			OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new MD5Generator())
			String code = oauthIssuer.authorizationCode()

			Token authzToken = new Token(
				value : code,
				clientId : clientId,
				redirectURI : redirectURI,
				user : user,
				scope : scope,
				state : state,
				// The authorization code MUST expire
				// shortly after it is issued to mitigate the risk of leaks.  A
				// maximum authorization code lifetime of 10 minutes is
				// RECOMMENDED.
				expiresIn : 10*60*1000)
			authzToken.validate()
			if(authzToken.hasErrors()){
				authzToken.errors.allErrors.each {
					log.error(it)
				}
				resp = org.apache.oltu.oauth2.as.response.OAuthASResponse
					.errorResponse(HttpServletResponse.SC_FOUND)
					.setError(OAuthError.CodeResponse.SERVER_ERROR)
					.setErrorDescription("unable to save the authorization code")
					.location(redirectURI)
					.buildQueryMessage()
			}
			else{
				authzToken.save(flush:true)
				resp = org.apache.oltu.oauth2.as.response.OAuthASResponse
					.authorizationResponse(request, HttpServletResponse.SC_FOUND)
					.setCode(code)
					.location(redirectURI)
					.buildQueryMessage()
			}
	
			redirect(url : resp.locationUri)

		} catch (OAuthProblemException e) {

		    int status = e.responseStatus
			if(status == 0){
				status = HttpServletResponse.SC_FOUND
			}
			switch(status){
				case(HttpServletResponse.SC_FOUND) :
					String redirectUri = e.getRedirectUri()
					final OAuthResponse resp = org.apache.oltu.oauth2.as.response.OAuthASResponse
							.errorResponse(HttpServletResponse.SC_FOUND)
							.error(e)
							.location(redirectUri)
							.buildQueryMessage()
					redirect(url : resp.locationUri)
					break
				default :
					render(status : status,
						contentType : 'text/plain',
						text : e.description,
						encoding: 'UTF-8')
				break
			}
		}
	}

	@Transactional
	def token() {
		OAuthTokenRequest oauthRequest

		String body = null

		int status = HttpServletResponse.SC_OK

        Token authzToken = null

		try {
			oauthRequest = new OAuthTokenRequest(request)

			OAuthResponse resp = null

			String grantType = oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
			if(!'authorization_code'.equals(grantType)){
                OAuthProblemException e = OAuthProblemException
                .error(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE, 'Unsupported grant_type value')
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST)
                throw e
			}

			String clientId = oauthRequest.clientId
            String clientSecret = oauthRequest.clientSecret
            
            OAuthUtil oauthUtil = OAuthUtil.getInstance(grailsApplication)
            
            // the client identifier is invalid or the client authentication failed
            if(!oauthUtil.checkClient(clientId) || !oauthUtil.authenticateClient(clientId, clientSecret)) {
                OAuthProblemException e = OAuthProblemException
                .error(OAuthError.TokenResponse.INVALID_CLIENT, 'Client authentication failed')
                .responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
                throw e
            }
            
            String redirectURI = oauthRequest.redirectURI

            // the redirection URI is mismatching
            if(!oauthUtil.validateClient(clientId, redirectURI)) {
                OAuthProblemException e = OAuthProblemException
                .error(OAuthError.TokenResponse.INVALID_GRANT, 'Invalid authorization grant')
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST)
                throw e
            }
    
			String code = oauthRequest.code
			
			authzToken = Token.findByValueAndClientId(code, clientId)
			if(authzToken == null || authenticationService.hasExpired(authzToken)){
                OAuthProblemException e = OAuthProblemException
                .error(OAuthError.TokenResponse.INVALID_GRANT, 'Invalid authorization grant')
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST)
                throw e
			}
            // The authorization code is bound to the client identifier and redirection URI.
			else if(!redirectURI.equals(authzToken.redirectURI)){
                OAuthProblemException e = OAuthProblemException
                .error(OAuthError.TokenResponse.INVALID_GRANT, 'Invalid authorization grant')
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST)
                throw e
			}
			else{
				OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new MD5Generator())
		
				String accessToken = oauthIssuer.accessToken()
				User user = authzToken.user
	
				Token token = new Token(
					value : accessToken,
					clientId : clientId,
					user : user,
					expiresIn : 0,
                    redirectURI : redirectURI)
				token.validate()
				if(token.hasErrors()){
                    token.errors.allErrors.each {
                        log.error(it)
                    }
                    // TODO
				}
				else{
					token.save(flush:true)
					resp = org.apache.oltu.oauth2.as.response.OAuthASResponse
						.tokenResponse(HttpServletResponse.SC_OK)
						.setAccessToken(accessToken)
                        .setTokenType(TokenType.BEARER.name())
						.setExpiresIn("0")
						.buildJSONMessage()
				}
			}

			status = HttpServletResponse.SC_OK
			body = resp.body

		} catch(OAuthProblemException ex) {

            status = ex.responseStatus
            if(status == 0){
                status = HttpServletResponse.SC_BAD_REQUEST
            }
			final OAuthResponse r = OAuthResponse
					.errorResponse(status)
					.error(ex)
					.buildJSONMessage()

			body = r.body
		}
        finally{
            if(authzToken){
                // The client MUST NOT use the authorization code more than once.
                authzToken.delete(flush:true)
            }
        }
		response.setHeader('Cache-Control', 'no-store')
		response.setHeader('Pragma', 'no-cache')
		render(status : status, 
			contentType : 'application/json', 
			text : body, 
			encoding: 'UTF-8')
	}
}
