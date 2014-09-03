package com.mogobiz.facebook

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.List

import com.restfb.BinaryAttachment
import com.restfb.Connection
import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import com.restfb.Parameter
import com.restfb.json.JsonObject
import com.restfb.types.Album
import com.restfb.types.Comment
import com.restfb.types.FacebookType
import com.restfb.types.NamedFacebookType
import com.restfb.types.Photo
import com.restfb.types.Post
import com.restfb.types.User

/**
 * @author Hayssam Saleh
 * 
 */
class FBClient extends DefaultFacebookClient {
	
	/**
	 * @param accessToken - token to access facebook graph api
	 */
	FBClient(String accessToken) {
		super(accessToken)
	}

	public User getUser() {
		return this.fetchObject("me", User.class);

	}

	public List<User> getFriends() {
		return this.fetchConnection("me/friends", User.class).getData();
	}

	/**
	 * @param message - message to publish
	 * @return message id
	 */
	public String publishMessage(String message) {
		
		FacebookType publishMessageResponse = this.publish("me/feed",
				FacebookType.class, Parameter.with("message", message))
		println("Published message ID: " + publishMessageResponse.getId())
		return publishMessageResponse.getId()
	}
	
	/**
	 * @param message - message to publish
	 * @param linkName - link name
	 * @param linkURL - link url
	 * @return message id
	 */
	public String publishMessageAndLink(String message, String linkName,
	String linkURL) {
		
		FacebookType publishMessageResponse = this.publish("me/feed",
				FacebookType.class, Parameter.with("message", message),
				Parameter.with("link", linkURL),
				Parameter.with("name", linkName))
		println("Published message ID: " + publishMessageResponse.getId())
		return publishMessageResponse.getId()
	}

	/**
	 * @param applicationId  
	 * @param message - message to publish
	 * @param linkName - link name
	 * @param linkURL - link url
	 * @return message id
	 */
	public String postProductToApplicationWall(String applicationId, String message, String linkName,
	String linkURL) {
		def application = new Expando()
		application.id = applicationId
		FacebookType publishPostResponse = this.publish(applicationId + "/feed",
				Post.class, Parameter.with("message", message),
				Parameter.with("link", linkURL),
				Parameter.with("name", linkName),
				Parameter.with("from", "me"),
				Parameter.with("application",application ))
		println("Published message ID: " + publishPostResponse.getId())
		return publishPostResponse.getId()
	}

	/**
	 * @param message - message to publish
	 * @param pictureURL - picture url
	 * @return message id
	 */
	public String publishMessageAndPicture(String message, String pictureURL) {
		FacebookType publishMessageResponse = this.publish("me/feed",
				FacebookType.class, Parameter.with("message", message),
				Parameter.with("picture", pictureURL))
		println("Published message ID: " + publishMessageResponse.getId())
		return publishMessageResponse.getId()
	}
	
	/**
	 * @param message - message to publish
	 * @param linkName - link name
	 * @param linkURL - link url
	 * @param pictureURL - picture url
	 * @return message id
	 */
	public String publishMessageAndLinkAndPicture(String message,
	String linkName, String linkURL, String pictureURL) {
		
		FacebookType publishMessageResponse = this.publish("me/feed",
				FacebookType.class, Parameter.with("message", message),
				Parameter.with("picture", pictureURL),
				Parameter.with("link", linkURL),
				Parameter.with("name", linkName))
		println("Published message ID: " + publishMessageResponse.getId())
		return publishMessageResponse.getId()
	}
	
	/**
	 * @param objectId - id object
	 * @param objectType - object type
	 * @return object fetched
	 */
	public <T> T fetchObjectById(String objectId, Class<T> objectType) {
		return this.fetchObject(objectId, objectType)
	}
	
	/**
	 * @param objectURL - object url
	 * @return {@link LinkCounter}
	 */

	public LinkCounter fetchCounters(String objectURL) {
		List<LinkCounter> counter = this
				.executeQuery(
						"SELECT url, share_count, like_count, comment_count, total_count FROM link_stat WHERE url=\""
								+ objectURL + "\"", LinkCounter.class);
		if (counter.size() > 0)
			return counter.get(0);
		return null;
	}

	public LinkCounter[] fetchCounters(String prefixe, def listeId) {
		String urls = "";
		listeId.each {
			if (!urls.isEmpty()) 
			{
				urls += ",";
			}
			urls += "\""+prefixe + it +"\"";
		}

		List<LinkCounter> counters = this
				.executeQuery(
						"SELECT url, share_count, like_count, comment_count, total_count FROM link_stat WHERE url IN ("
								+ urls + ")", LinkCounter.class);
		return counters.toArray(new LinkCounter[0]);
	}

	public LinkCounter[] fetchCounters(String[] objectURLs) {
		String urls = "";
		for (String url : objectURLs) {
			if (!url.isEmpty()) {
				url += ",";
			}
			urls += "\""+url+"\"";
		}
		List<LinkCounter> counters = this
				.executeQuery(
						"SELECT url, share_count, like_count, comment_count, total_count FROM link_stat WHERE url IN ("
								+ urls + ")", LinkCounter.class);
		return counters.toArray(new LinkCounter[0]);
	}


	/**
	 * @param name - album name
	 * @param description - album description
	 * @return album id
	 */
	public Album createAlbum(String name, String description) {
		Album album = fetchAlbum(name)
		if (album == null) {
			FacebookType publishMessageResponse = this.publish(
					"me/albums", FacebookType.class,
					Parameter.with("name", name),
					Parameter.with("description", description))
			album = fetchObjectById(publishMessageResponse.getId(), Album.class)
		}
		return album
	}
	
	/**
	 * @param name - album name
	 * @return {@link Album}
	 */
	public Album fetchAlbum(String name) {
		Connection<Album> connection = this.fetchConnection(
				"me/albums", Album.class)
		for (Album album : connection.getData()) {
			if (album.getName().equals(name)) {
				return album
			}
		}
		return null
	}
	
	/**
	 * @param photoFile - photo as file
	 * @param name - photo name
	 * @param album - album
	 * @return {@link Photo}
	 */
	public Photo publishPhoto(File photoFile, String name, Album album)
	throws FileNotFoundException {
		InputStream photoStream = new FileInputStream(photoFile)
		Photo photo = publishPhoto(photoStream, name, album)
		photoStream.close()
		return photo
	}
	
	/**
	 * @param photoStream - photo as stream
	 * @param name - photo name
	 * @param album - album
	 * @return {@link Photo}
	 */
	public Photo publishPhoto(InputStream photoStream, String name, Album album) {
		String root = "me"
		if (album) {
			root = album.getId()
		}
		FacebookType publishPhotoResponse = this.publish(
				root + "/photos", FacebookType.class, BinaryAttachment.with(name, photoStream),
				Parameter.with("message", name))
		println("Published photo ID: " + publishPhotoResponse.getId())
		return fetchObjectById(publishPhotoResponse.getId(), Photo.class)
	}

	public Photo publishVideo(File videoFile, String name, Album album)
	throws FileNotFoundException {
		InputStream videoStream = new FileInputStream(videoFile)
		Photo photo = publishVideo(videoStream, name, album)
		videoStream.close()
		return photo
	}
	public Photo publishVideo(InputStream photoStream, String name, Album album) {
		String root = "me"
		if (album) {
			root = album.getId()
		}
		FacebookType publishPhotoResponse = this.publish(
				root + "/videos", FacebookType.class, BinaryAttachment.with(name, photoStream),
				Parameter.with("message", name))
		println("Published photo ID: " + publishPhotoResponse.getId())
		return fetchObjectById(publishPhotoResponse.getId(), Photo.class)
	}



	/**
	 * @param albumId - id album
	 * @param offset - offset
	 * @param limit - limit
	 * @return array of {@link Photo}
	 */
	public Photo[] fetchPhotos(String albumId, int offset, int limit) {
		String root = "me"
		if (albumId) {
			root = albumId
		}
		Connection<Photo> connection = this.fetchConnection(root
				+ "/photos", Photo.class, Parameter.with("limit", limit),
				Parameter.with("offset", offset))
		return connection.getData().toArray(new Photo[0])
	}
	
	/**
	 * @param offset - offset
	 * @param limit - limit
	 * @return array of {@link Album}
	 */
	public Album[] fetchAlbums(int offset, int limit) {
		Connection<Album> connection = this.fetchConnection(
				"me/albums", Album.class, Parameter.with("limit", limit),
				Parameter.with("offset", offset))
		return connection.getData().toArray(new Album[0])
	}
	
	/**
	 * @param objectId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public NamedFacebookType[] fetchLikes(String objectId, int offset, int limit) {
		Connection<NamedFacebookType> connection = this
				.fetchConnection(objectId + "/likes", NamedFacebookType.class,
				Parameter.with("limit", limit),
				Parameter.with("offset", offset))
		return connection.getData().toArray(new NamedFacebookType[0])
	}
	
	/**
	 * @param objectId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public Comment[] fetchComments(String objectId, int offset, int limit) {
		Connection<Comment> connection = this.fetchConnection(
				objectId + "/comments", Comment.class,
				Parameter.with("limit", limit),
				Parameter.with("offset", offset))
		return connection.getData().toArray(new Comment[0])
	}
	
	/**
	 * @param id
	 * @return
	 */
	public List<JsonObject> queryTest(String id) {
		return this.executeQuery(
		"SELECT url, id, type, site FROM object_url where id = " + id,
		JsonObject.class)
	}

	/**
	 * @param objectId
	 * @param message
	 * @return Post Id
	 */
	public String addProductPost(String objectId, String message) {
		FacebookType postCommentResponse = this.publish(objectId + "/feed", Post.class, Parameter.with("message", message))
		println("Post Id: " + postCommentResponse.getId())
		return postCommentResponse.getId()
	}

	/**
	 * @param url - id object
	 * @return id of the fetched object
	 */
	public ObjectUrl getObjectIdFromUrl(String url) {
		List<ObjectUrl> result = this.executeQuery("SELECT id, url, type, site FROM object_url WHERE url = \"" + url + "\"",ObjectUrl.class)
		if (result.size() > 0)
			return result.get(0);
		return null;
	}

	/**
	 * @param post Id
	 * @return array of {@link Post}
	 */
	public Post []  getProductPosts(String postId){
		Connection<Post> productFeed = this.fetchConnection(postId+ "/feed", Post.class);
		return productFeed.getData().toArray()
	}
	
	
	/**
	* @param objectId : object must have /likes connection 
	* @param offset
	* @param limit
	* @return
	*/
   public Boolean likeObject(String objectId) {
	   Boolean likeResponse = this.publish(objectId + "/likes", Boolean.class)
	   println("Like Object : " + objectId + " = " + likeResponse)
	   return likeResponse
   }
}
