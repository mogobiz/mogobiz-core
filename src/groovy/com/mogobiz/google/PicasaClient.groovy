/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mogobiz.google

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters
import com.google.gdata.client.authn.oauth.OAuthException
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer
import com.google.gdata.client.photos.PicasawebService
import com.google.gdata.data.Link
import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.data.media.MediaStreamSource
import com.google.gdata.data.media.mediarss.MediaContent
import com.google.gdata.data.photos.*
import com.google.gdata.util.ServiceException

/**
 * This is a simple client that provides high-level operations on the Picasa Web
 * Albums GData API. It can also be used as a command-line application to test
 * out some of the features of the API.
 * 
 * 
 */
// http://code.google.com/apis/picasaweb/docs/2.0/developers_guide_java.html

public class PicasaClient {
	
	private static final String API_PREFIX = 'https://picasaweb.google.com/data/feed/api/user/'
	
	private final PicasawebService service

	private final String serviceName

	/**
	 * Constructs a new client with the given username and password.
	 */
	public PicasaClient(String token, String tokenSecret)
	throws Exception {
		this(GoogleApi.SERVICE_NAME, GoogleApi.CONSUMER_KEY, GoogleApi.CONSUMER_SECRET, token, tokenSecret)
	}
	/**
	 * Constructs a new client with the given username and password.
	 */
	public PicasaClient(String serviceName, String consumerKey,
	String consumerSecret, String token, String tokenSecret)
	throws Exception {
		this.serviceName = serviceName
		service = new PicasawebService(serviceName)
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
				throw new Exception("Unable to initialize picasa service", e)

			}
		}
	}
	
	/**
	 * Retrieves the albums for the currently logged-in user. This is equivalent
	 * to calling {@link #getAlbums(String)} with "default" as the username.
	 */
	public AlbumEntry[] fetchAlbums() throws IOException,
			ServiceException {
		String albumUrl = API_PREFIX + "default"
		albumUrl = addParameter(albumUrl, "kind", "album")
		UserFeed userFeed = getFeed(albumUrl, UserFeed.class)
		List<AlbumEntry> albums = new ArrayList<AlbumEntry>()
		for (AlbumEntry myAlbum : userFeed.getAlbumEntries()) {
			albums.add((AlbumEntry) myAlbum)
		}
		return albums.toArray(new AlbumEntry[albums.size()])
	}

	/**
	 * Retrieves the tags for the currently logged-in user. This is equivalent
	 * to calling {@link #getTags(String)} with "default" as the username.
	 */
	public TagEntry[] getTags() throws IOException,
			ServiceException {
		String tagUrl = API_PREFIX + "default"
		tagUrl = addParameter(tagUrl, "kind", "tag")
		UserFeed userFeed = getFeed(tagUrl, UserFeed.class)

		List<GphotoEntry> entries = userFeed.getEntries()
		List<TagEntry> tags = new ArrayList<TagEntry>()
		for (GphotoEntry entry : entries) {
			GphotoEntry adapted = entry.getAdaptedEntry()
			if (adapted instanceof TagEntry) {
				tags.add((TagEntry) adapted)
			}
		}
		return tags.toArray(new TagEntry[tags.size()])
	}

	/**
	 * Retrieves the photos for the given album.
	 */
	public PhotoEntry[] fetchPhotos(String albumId, int offset, int limit)
			throws IOException, ServiceException {
		albumId = albumId.substring(albumId.lastIndexOf('/')+1)
		String feedUrl = API_PREFIX + "default/albumid/"+albumId
		feedUrl = addParameter(feedUrl, "start-index", (offset+1)+"")
		feedUrl = addParameter(feedUrl, "max-results", limit+"")
		
		AlbumFeed albumFeed = getFeed(feedUrl, AlbumFeed.class)
		List<PhotoEntry> entries = albumFeed.getPhotoEntries()
		List<PhotoEntry> photos = new ArrayList<PhotoEntry>()
		for (PhotoEntry photo : entries) {
		    photos.add(photo)
		}
		return photos.toArray(new PhotoEntry[photos.size()])
	}

	/**
	 * Retrieves the comments for the given photo.
	 */
	public CommentEntry[] fetchComments(PhotoEntry photo)
			throws IOException, ServiceException {

		String feedHref = getLinkByRel(photo.getLinks(), Link.Rel.FEED)
		AlbumFeed albumFeed = getFeed(feedHref, AlbumFeed.class)

		List<GphotoEntry> entries = albumFeed.getEntries()
		List<CommentEntry> comments = new ArrayList<CommentEntry>()
		for (GphotoEntry entry : entries) {
			GphotoEntry adapted = entry.getAdaptedEntry()
			if (adapted instanceof CommentEntry) {
				comments.add((CommentEntry) adapted)
			}
		}
		return comments.toArray(new CommentEntry[comments.size()])
	}

			
	/**
	 * Retrieves the tags for the given taggable entry. This is valid on user,
	 * album, and photo entries only.
	public TagEntry[] fetchTags(GphotoEntry<? super GphotoEntry> parent)
			throws IOException, ServiceException {

		String feedHref = getLinkByRel(parent.getLinks(), Link.Rel.FEED)
		feedHref = addParameter(feedHref, "kind", "tag")
		AlbumFeed albumFeed = getFeed(feedHref, AlbumFeed.class)

		List<GphotoEntry> entries = albumFeed.getEntries()
		List<TagEntry> tags = new ArrayList<TagEntry>()
		for (GphotoEntry entry : entries) {
			GphotoEntry adapted = entry.getAdaptedEntry()
			if (adapted instanceof TagEntry) {
				tags.add((TagEntry) adapted)
			}
		}
		return tags.toArray(new TagEntry[tags.size()])
	}
	 */

	/**
	 * Album-specific insert method to insert into the gallery of the current
	 * user, this bypasses the need to have a top-level entry object for parent.
	 */
	public AlbumEntry createAlbum(String title, String description)
			throws IOException, ServiceException {
		title = title.trim()
		AlbumEntry album = fetchAlbum(title)
		if (album != null) {
			return album
		}
		 AlbumEntry myAlbum = new AlbumEntry()
		 myAlbum.setTitle(new PlainTextConstruct(title))
		 myAlbum.setDescription(new PlainTextConstruct(description))
		String feedUrl = API_PREFIX + "default"
		return service.insert(new URL(feedUrl), myAlbum)
	}

	/**
	 * Album-specific insert method to insert into the gallery of the current
	 * user, this bypasses the need to have a top-level entry object for parent.
	 */
	public AlbumEntry fetchAlbum(String title)
			throws IOException, ServiceException {
		AlbumEntry[] albums = fetchAlbums()
		for (AlbumEntry album : albums) {
			if (album.getTitle() != null && album.getTitle().getPlainText().equals(title)) {
				return album
			}
		}
		return null
	}
	public PhotoEntry publishPhoto(InputStream photoStream, String mimeType, String name, String albumId) {
		albumId = albumId.substring(albumId.lastIndexOf('/')+1)
		String feedUrl = API_PREFIX + "default/albumid/"+albumId
		PhotoEntry myPhoto = new PhotoEntry()
		myPhoto.setTitle(new PlainTextConstruct(name))
		myPhoto.setClient(serviceName)

		MediaStreamSource myMedia = new MediaStreamSource(photoStream, mimeType)
		myPhoto.setMediaSource(myMedia)

		try {
			PhotoEntry returnedPhoto = service.insert(new URL(feedUrl), myPhoto)
			return returnedPhoto
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace()
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace()
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace()
		}
		return null
	}
	
	private String getMimeType(File photoFile) {
		String name = photoFile.getName()
		if (name.toLowerCase().endsWith(".jpg"))
			return "image/jpeg"
		if (name.toLowerCase().endsWith(".gif"))
			return "image/gif"
		if (name.toLowerCase().endsWith(".png"))
			return "image/png"
		if (name.toLowerCase().endsWith(".bmp"))
			return "image/bmp"
		return null
	}

	/**
	 * Insert an entry into another entry. Because our entries are a hierarchy,
	 * this lets you insert a photo into an album even if you only have the
	 * album entry and not the album feed, making it quicker to traverse the
	 * hierarchy.
	 * image/bmp
	 * image/gif
	 * image/jpeg
	 * image/png
	public <T extends GphotoEntry> T insert(GphotoEntry<?> parent, T entry)
			throws IOException, ServiceException {

		String feedUrl = getLinkByRel(parent.getLinks(), Link.Rel.FEED)
		return service.insert(new URL(feedUrl), entry)
	}
	 */

	/**
	 * Helper function to allow retrieval of a feed by string url, which will
	 * create the URL object for you. Most of the Link objects have a string
	 * href which must be converted into a URL by hand, this does the
	 * conversion.
	 */
	public <T extends GphotoFeed> T getFeed(String feedHref, Class<T> feedClass)
			throws IOException, ServiceException {
		println("Get Feed URL: " + feedHref)
		return service.getFeed(new URL(feedHref), feedClass)
	}

	/**
	 * Helper function to add parameter to a url.
	 */
	private String addParameter(String url, String param, String value) {
		if (url.contains("?")) {
			return url + "&" + param + "=" + value
		} else {
			return url + "?" + param + "=" + value
		}
	}

	/**
	 * Helper function to get a link by a rel value.
	 */
	public String getLinkByRel(List<Link> links, String relValue) {
		for (Link link : links) {
			if (relValue.equals(link.getRel())) {
				return link.getHref()
			}
		}
		throw new IllegalArgumentException("Missing " + relValue + " link.")
	}
	
	public String getPhotoURL(PhotoEntry photo) {
		List<MediaContent> contents = photo.getMediaContents()
		for (MediaContent content : contents) {
			if (content.getMedium().equals("image"))
			return content.getUrl()
		}
		return null
	}
}
