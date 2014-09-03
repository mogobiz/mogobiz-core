/**
 * 
 */
package com.mogobiz.facebook

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.restfb.Connection
import com.restfb.types.Album
import com.restfb.types.Photo
import com.restfb.types.Comment
import com.restfb.types.Post
import com.restfb.json.JsonObject;

/**
 * @author stephane.manciot@ebiznext.com
 *
 */
class FBClientTest {
	
	/**
	 * 
	 */
	private static final String accessToken = "102076459880504|d55ece6cfce660a516d743ef.1-100002038103534|eakhpfXUkkgkWa9QYYVGC4l4StI"
	private static final String myToken = "AAABc1ochkDgBAKWhOa77W8msDNTrAW3BlTnbSJiPErAmdwZBZCnY5fQ58iHuCSB5l2CaYhMdw37L6SGH64G35IauPloaUBo27xuBbqXQZDZD"
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testClient(){
		FBClient client = new FBClient(accessToken)
		Album album = client.fetchAlbum("IPER 2010 Photos")
		Photo[] photos = client.fetchPhotos(album.getId(), 0, 100)
		def photo = photos[0]
		assertNotNull(photo)
		def picture = photo.picture
		assertNotNull(picture)
		println(picture)
		def source = photo.source
		assertNotNull(source)
		println(source)
		List<JsonObject> list = client.queryTest(photo.id)
		assertNotNull(list)
		assertFalse(list.isEmpty())
		list.each { json -> println(json)  }
	}

	@Test
	public void testFetchAlbums(){
		FBClient client = new FBClient(accessToken)
		def albums = client.fetchAlbums (0, 0)
		assertNotNull(albums)
		assertFalse(albums.length == 0)
		albums.each { album -> println(album.name)  }
	}
	
	@Test
	public void testCommentAndFetchAllComments(){
		FBClient client = new FBClient(myToken)
		String msgId = client.publishMessage("test message at : " + Calendar.getInstance().getTimeInMillis())
		client.postComment(msgId, "test Comment")
		Comment [] commentaires = client.fetchComments(msgId, 0, 1);
		assertTrue(commentaires.length == 1)
		commentaires.each { com -> println(com.message)}
	}
	
	
	
	@Test
	public void testCommentingMyLastFeeds(){
		FBClient client = new FBClient(myToken)
		Connection<Post> myFeed = client.fetchConnection("me/feed", Post.class);		
		println("First item in my feed: " + myFeed.getData().get(0));
		Post post = myFeed.getData().get(0)
		
		client.postComment(post.id, "test Comment at " + Calendar.getInstance().getTimeInMillis())
		Comment [] commentaires = client.fetchComments(post.id, 0, post.getComments().getCount().toInteger());
		assertTrue(commentaires.length == post.getComments().getCount())
	}
	
	
	@Test
	public void testGetAppFeed(){
		//147789211961700
		FBClient client = new FBClient(myToken)
		Connection<Post> myFeed = client.fetchConnection("147789211961700/posts", Post.class);		
		println("First item in my feed: " + myFeed.getData().get(0));
		Post post = myFeed.getData().get(0)
		
		client.postComment(post.id, "test Comment at " + Calendar.getInstance().getTimeInMillis())
		Comment [] commentaires = client.fetchComments(post.id, 0, post.getComments().getCount().toInteger());
		assertTrue(commentaires.length == post.getComments().getCount())
	}

	
}
