package com.mogobiz.google

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters
import com.google.gdata.client.authn.oauth.OAuthException
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer
import com.google.gdata.client.youtube.YouTubeQuery
import com.google.gdata.client.youtube.YouTubeService
import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.data.media.mediarss.MediaCategory
import com.google.gdata.data.media.mediarss.MediaDescription
import com.google.gdata.data.media.mediarss.MediaKeywords
import com.google.gdata.data.media.mediarss.MediaPlayer
import com.google.gdata.data.media.mediarss.MediaTitle
import com.google.gdata.data.youtube.CommentEntry
import com.google.gdata.data.youtube.VideoEntry
import com.google.gdata.data.youtube.VideoFeed
import com.google.gdata.data.youtube.YouTubeMediaGroup
import com.google.gdata.data.youtube.YouTubeNamespace
import com.google.gdata.data.youtube.YtPublicationState
import com.google.gdata.util.ServiceException

/**
 * @author Hayssam Saleh
 * 
 * @see http://code.google.com/apis/youtube/2.0/developers_guide_java.html
 */
public class YouTubeClient {
	/**
	 * The name of the server hosting the YouTube GDATA feeds.
	 */
	public static final String YOUTUBE_GDATA_SERVER = 'http://gdata.youtube.com'
	
	/**
	 * The URL of the videos feed
	 */
	public static final String VIDEOS_FEED = YOUTUBE_GDATA_SERVER + '/feeds/api/videos'
	
	/**
	 * The prefix of the user feeds
	 */
	public static final String USER_FEED_PREFIX = YOUTUBE_GDATA_SERVER + '/feeds/api/users/'
	
	/**
	 * The prefix of recent activity feeds
	 */
	public static final String ACTIVITY_FEED_PREFIX = YOUTUBE_GDATA_SERVER + '/feeds/api/events'
	
	/**
	 * The URL suffix of the test user's uploads feed
	 */
	public static final String UPLOADS_FEED_SUFFIX = '/uploads'
	
	/**
	 * The URL suffix of the test user's favorites feed
	 */
	public static final String FAVORITES_FEED_SUFFIX = '/favorites'
	
	/**
	 * The URL suffix of the test user's playlists feed
	 */
	public static final String PLAYLISTS_FEED_SUFFIX = '/playlists'
	
	/**
	 * The URL suffix of the friends activity feed
	 */
	public static final String FRIENDS_ACTIVITY_FEED_SUFFIX = '/friendsactivity'
	
	/**
	 * The default username.
	 */
	public static final String DEFAULT_USER = 'default'
	
	/**
	 * The default video to use for examples.
	 */
	public static final String DEFAULT_VIDEO_ID = 'scoMN8DYkCw'
	
	/**
	 * The URL used to upload video
	 */
	public static final String VIDEO_UPLOAD_FEED = 'http://uploads.gdata.youtube.com/feeds/api/users/' + DEFAULT_USER + '/uploads'
	
	private URL newOAuthURL(String url, String requestorId)
	throws MalformedURLException {
		return new URL(url + '?xoauth_requestor_id=' + requestorId)
	}

	
	private final YouTubeService service
	
	/**
	 * Constructs a new client with the given username and password.
	 */
	public YouTubeClient(String token, String tokenSecret)
	throws Exception {
		this(GoogleApi.SERVICE_NAME, GoogleApi.CONSUMER_KEY, GoogleApi.CONSUMER_SECRET, token, tokenSecret)
	}
	
	/**
	 * Constructs a new client with the given username and password.
	 */
	public YouTubeClient(String serviceName, String consumerKey,
	String consumerSecret, String token, String tokenSecret)
	throws Exception {
		service = new YouTubeService(serviceName)
		if (token != null && tokenSecret != null) {
			try {
				GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters()
				oauthParameters.setOAuthConsumerKey(consumerKey)
				oauthParameters.setOAuthConsumerSecret(consumerSecret)
				oauthParameters.setOAuthToken(token)
				oauthParameters.setOAuthTokenSecret(tokenSecret)
				service.setOAuthCredentials(oauthParameters,
						new OAuthHmacSha1Signer())
			} catch (OAuthException e) {
				e.printStackTrace()
				throw new Exception('Unable to initialize calendar service', e)
			}
		}
	}
	
	/*
	 * check debugPrintVideoEntry(videoEntry) for url & title info
	 */
	public VideoEntry uploadVideo(InputStream videoStream, String mimeType,
	String videoTitle, String category, String description, String[] keywords) throws Exception {
		println('First, type in the path to the movie file:')
		
		VideoEntry newEntry = new VideoEntry()
		
		YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup()
		
		mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME,
				category))
		mg.setTitle(new MediaTitle())
		mg.getTitle().setPlainTextContent(videoTitle)
		mg.setKeywords(new MediaKeywords())
		if (keywords != null) {
			mg.setKeywords(new MediaKeywords())
			for (String keyword : keywords) {
				mg.getKeywords().addKeyword(keyword)
			}
		}
		mg.setDescription(new MediaDescription())
		mg.getDescription().setPlainTextContent(description)
		MediaStreamSource ms = new MediaStreamSource(videoStream, mimeType)
		newEntry.setMediaSource(ms)
		return service.insert(new URL(VIDEO_UPLOAD_FEED), newEntry)
	}
	
	public CommentEntry addComment(String videoID, String comment,
	String requestorId) throws Exception {
		println('Okay, adding a comment to: ' + videoID)
		
		URL entryUrl = newOAuthURL('http://gdata.youtube.com/feeds/api/videos/'
				+ videoID, requestorId)
		
		VideoEntry videoEntry = null
		try {
			videoEntry = service.getEntry(entryUrl, VideoEntry.class)
		} catch (ServiceException se) {
			// an invalid video ID was used.
		}
		
		if (videoEntry == null) {
			throw new Exception(
			'Sorry, the video ID you entered was not valid.\n')
		}
		
		println('Enter your comment: ')
		
		String commentUrl = videoEntry.getComments().getFeedLink().getHref()
		
		CommentEntry newComment = new CommentEntry()
		newComment.setContent(new PlainTextConstruct(comment))
		
		return service.insert(newOAuthURL(commentUrl, requestorId), newComment)
	}
	
	public VideoEntry[] searchVideos(String searchExpression)
	throws IOException, ServiceException {
		return searchFeed(USER_FEED_PREFIX + DEFAULT_USER, searchExpression)
	}
	
	private VideoEntry[] searchFeed(String feed, String searchExpression)
	throws IOException, ServiceException {
		YouTubeQuery query = new YouTubeQuery(new URL(feed))
		// order results by the number of views (most viewed first)
		query.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT)
		
		// do not exclude restricted content from the search results
		// (by default, it is excluded)
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE)
		
		query.setFullTextQuery(searchExpression)
		VideoFeed videoFeed = service.query(query, VideoFeed.class)
		return videoFeed.getEntries().toArray(new VideoEntry[0])
	}
	
	public VideoEntry[] getUploads(String requestorId) throws IOException,
	ServiceException {
		return getVideoFeed(USER_FEED_PREFIX + DEFAULT_USER
		+ UPLOADS_FEED_SUFFIX, requestorId)
	}
	
	public String getVideoURL(VideoEntry video) {
		return video?.mediaGroup?.player?.url
	}

	private VideoEntry[] getVideoFeed(String feedUrl, String requestorId)
	throws IOException, ServiceException {
		VideoFeed videoFeed = service.getFeed(
				newOAuthURL(feedUrl, requestorId), VideoFeed.class)
		String title = videoFeed.getTitle().getPlainText()
		
		List<VideoEntry> videoEntries = videoFeed.getEntries()
		int count = 1
		for (VideoEntry entry : videoEntries) {
			println('(Video #' + String.valueOf(count) + ')')
			debugPrintVideoEntry(entry)
			count++
		}
		return videoEntries.toArray(new VideoEntry[videoEntries.size()])
	}
	
	private void debugPrintVideoEntry(VideoEntry entry) throws IOException,
	ServiceException {
		println('Title:' + entry.getTitle().getPlainText())
		println('Id:' + entry.getId())
		YouTubeMediaGroup mediaGroup = entry.getMediaGroup()
		if (mediaGroup != null) {
			if (mediaGroup.isPrivate()) {
				println('Video is private')
			}
			MediaPlayer player = mediaGroup.getPlayer()
			if (player != null) {
				println('Video URL: ' + player.getUrl())
			}
		}
		if (entry.isDraft()) {
			println('Video is not live')
			YtPublicationState pubState = entry.getPublicationState()
			if (pubState.getState() == YtPublicationState.State.PROCESSING) {
				println('Video is still being processed.')
			} else if (pubState.getState() == YtPublicationState.State.REJECTED) {
				System.out.print('Video has been rejected because: ')
				println(pubState.getDescription())
				System.out.print('For help visit: ')
				println(pubState.getHelpUrl())
			} else if (pubState.getState() == YtPublicationState.State.FAILED) {
				System.out.print('Video failed uploading because: ')
				println(pubState.getDescription())
				System.out.print('For help visit: ')
				println(pubState.getHelpUrl())
			}
		}
	}
}
