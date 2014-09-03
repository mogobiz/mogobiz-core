package com.mogobiz.twitter

import twitter4j.Status
import twitter4j.Twitter;
import twitter4j.TwitterFactory
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import twitter4j.media.ImageUpload
import twitter4j.media.ImageUploadFactory
import twitter4j.media.MediaProvider

class TwitterClient {
	Twitter twitter;
	private String token;
	private String tokenSecret;
	public TwitterClient(String token, String tokenSecret)
	throws Exception {
		this.token = token
		this.tokenSecret = tokenSecret
	}
	public String uploadImage(File imagePath) {
		Configuration conf = new ConfigurationBuilder()
				.setMediaProviderAPIKey( TwitterApi.TWITPIC_API_KEY)
				.setOAuthConsumerKey( TwitterApi.CONSUMER_KEY )
				.setOAuthConsumerSecret( TwitterApi.CONSUMER_SECRET )
				.setOAuthAccessToken(token)
				.setOAuthAccessTokenSecret(tokenSecret)
				.build();
		ImageUpload uploader = new ImageUploadFactory(conf).getInstance(MediaProvider.TWITPIC);
		String url = uploader.upload(imagePath);
		return url;
	}
	
	public Status updateStatus(String status) {
		Configuration conf = new ConfigurationBuilder()
				.setOAuthConsumerKey( TwitterApi.CONSUMER_KEY )
				.setOAuthConsumerSecret( TwitterApi.CONSUMER_SECRET )
				.setOAuthAccessToken(token)
				.setOAuthAccessTokenSecret(tokenSecret)
				.build();
		Twitter t = new TwitterFactory(conf).getInstance();
		return t.updateStatus(status);
	
	}
}
