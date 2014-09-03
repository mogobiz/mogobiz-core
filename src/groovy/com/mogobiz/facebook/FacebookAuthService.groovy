package com.mogobiz.facebook;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FacebookAuthService {

	private static final String appSecret = "APP_SECRET";


	//	public static String getAPIKey() {
	//		return apiKey;
	//	}

	public static String getSecret() {
		return appSecret;
	}

	public static String getLoginRedirectURL(client_id, redirect_uri, String[] perms) {
		return "https://graph.facebook.com/oauth/authorize?client_id=" + client_id
		+ "&display=page&redirect_uri=" + redirect_uri + "&scope="
		+ perms.join(",");
	}

	public static String getAuthCodeURL(client_id, redirect_uri, String authCode) {
		return "https://graph.facebook.com/oauth/access_token?client_id="
		+ client_id + "&redirect_uri=" + redirect_uri + "&client_secret="
		+ appSecret + "&code=" + authCode;
	}

	public static String getAuthURL(client_id, redirect_uri, String[] perms) {
		return "https://www.facebook.com/dialog/oauth?client_id=" + client_id + "&redirect_uri=" + redirect_uri + "&scope="
		+ perms.join(",");
	}

	public static FacebookSignedRequest getFacebookSignedRequest(String signedRequest) throws Exception{
		if (signedRequest == null)
			return new FacebookSignedRequest();
		String payLoad = signedRequest.split("[.]", 2)[1];
		payLoad = payLoad.replace("-", "+").replace("_", "/").trim();

		String jsonString = new String(payLoad.decodeBase64());
		println(jsonString)
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, FacebookSignedRequest.class);
	}

}
