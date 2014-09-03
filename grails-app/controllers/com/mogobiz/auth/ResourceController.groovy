package com.mogobiz.auth

import com.mogobiz.store.domain.Token
import com.mogobiz.store.domain.User
import org.apache.commons.lang.StringUtils
import org.apache.oltu.oauth2.as.issuer.MD5Generator
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl
import org.apache.oltu.oauth2.common.OAuth
import org.apache.oltu.oauth2.common.error.OAuthError
import org.apache.oltu.oauth2.common.exception.OAuthProblemException
import org.apache.oltu.oauth2.common.message.OAuthResponse
import org.apache.oltu.oauth2.common.message.types.ParameterStyle
import org.apache.oltu.oauth2.common.message.types.TokenType
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest

import javax.servlet.http.HttpServletResponse

class ResourceController {

    def authenticationService

    def sessionToken(){
        String body

        int status = HttpServletResponse.SC_OK

        try{
            String redirectURI = request.getParameter(OAuth.OAUTH_REDIRECT_URI)
            String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID)
            String clientSecret = request.getParameter(OAuth.OAUTH_CLIENT_SECRET)

            // the redirection URI is missing
            if(!redirectURI) {
                OAuthProblemException e = OAuthProblemException
                        .error(OAuthError.CodeResponse.INVALID_REQUEST, 'Missing redirect_uri parameter value')
                        .responseStatus(HttpServletResponse.SC_BAD_REQUEST)
                throw e
            }

            redirectURI = URLDecoder.decode(redirectURI, 'UTF-8')

            // the client identifier is missing
            if(!clientId) {
                OAuthProblemException e = OAuthProblemException
                        .error(OAuthError.CodeResponse.INVALID_REQUEST, 'Missing client_id parameter value')
                        .responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
                throw e
            }

            // the client secret is missing
            if(!clientSecret) {
                OAuthProblemException e = OAuthProblemException
                        .error(OAuthError.CodeResponse.INVALID_REQUEST, 'Missing client_secret parameter value')
                        .responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
                throw e
            }

            OAuthUtil oauthUtil = OAuthUtil.getInstance(grailsApplication)

            // the client identifier is invalid or the client authentication failed
            if(!oauthUtil.checkClient(clientId) || !oauthUtil.authenticateClient(clientId, clientSecret)) {
                OAuthProblemException e = OAuthProblemException
                        .error(OAuthError.TokenResponse.INVALID_CLIENT, 'Client authentication failed')
                        .responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
                throw e
            }

            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request,
                    [TokenType.BEARER] as TokenType[],
                    [ParameterStyle.HEADER] as ParameterStyle[])

            User user = authenticationService.access(oauthRequest.accessToken)
            if(user == null){
                OAuthProblemException e = OAuthProblemException
                        .error(OAuthError.ResourceResponse.INVALID_TOKEN, 'Invalid token')
                        .responseStatus(HttpServletResponse.SC_UNAUTHORIZED)
                throw e
            }

            OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new MD5Generator())

            String sessionToken = oauthIssuer.authorizationCode()

            Token token = new Token(
                    value : sessionToken,
                    clientId : clientId,
                    user : user,
                    creationDate : new Date(),
                    // The authorization code MUST expire
                    // shortly after it is issued to mitigate the risk of leaks.  A
                    // maximum authorization code lifetime of 10 minutes is
                    // RECOMMENDED.
                    expiresIn : 10*60*1000,
                    redirectURI : redirectURI)
            token.validate()
            if(token.hasErrors()){
                token.errors.allErrors.each {
                    log.error(it)
                }
                OAuthProblemException e = OAuthProblemException.error('Internal server error')
                        .responseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                throw e
            }
            else{
                token.save(flush:true)
                final OAuthResponse resp = org.apache.oltu.oauth2.as.response.OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(sessionToken)
                        .setExpiresIn('' + token.expiresIn)
                        .buildJSONMessage()
                body = resp.body
            }
        }
        catch(OAuthProblemException ex){
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
        render(status : status,
                contentType : 'application/json',
                text : body,
                encoding: 'UTF-8')
    }

    def sso(String sessionToken, String resource){
        def redirections = grailsApplication.config.sso as Map
        redirectToPage(sessionToken, resource, redirections ? redirections[resource] as String : null)
    }

    private void redirectToPage(final String token, final String action, final String pageURI){
        if(StringUtils.isEmpty(token)){
            log.warn('SSO - missing token')
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 'missing token')
        }
        else{
            StringBuffer buffer = new StringBuffer(grailsApplication.config.grails.serverURL as String)
            String redirectURI = buffer.append('/').append(controllerName).append('/').append(action).toString()
            log.info('SSO - redirectURI :' + redirectURI)
            Token authzToken = Token.findByValueAndRedirectURI(token, redirectURI)
            if(!authzToken || authenticationService.hasExpired(authzToken)){
                log.warn('SSO - invalid token')
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 'invalid token')
            }
            else{
                User user = authzToken.user
                authenticationService.authenticate(user.login, user.password, true)
                def url = new StringBuffer(grailsApplication.config.grails.serverURL as String)
                if(pageURI){
                    url.append(pageURI)
                }
                else{
                    log.warn('no redirection has been defined for sso.' + action)
                }
                log.info('SSO - redirect to ' + url)
                redirect(url : url.toString())
            }
            if(authzToken){
                authzToken.delete(flush:true)
            }
        }
    }
}
