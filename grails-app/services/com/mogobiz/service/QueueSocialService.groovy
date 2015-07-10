package com.mogobiz.service

import com.mogobiz.authentication.AuthenticationService

import static com.mogobiz.constant.IperConstant.*

import grails.events.Listener
import grails.util.Holders

import javax.activation.MimetypesFileTypeMap

import org.grails.plugin.platform.events.EventMessage

import com.mogobiz.store.domain.AccountType
import com.mogobiz.store.domain.ExternalAccount
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.store.domain.User
import com.mogobiz.facebook.FBClient
import com.mogobiz.google.PicasaClient
import com.mogobiz.google.YouTubeClient
import com.mogobiz.twitter.TwitterClient

class QueueSocialService {
	boolean transactional = false

    AuthenticationService authenticationService

	@Listener(topic=QUEUE_SOCIAL, namespace=QUEUE_NS)
	def onEvent(EventMessage ev) {
		def map = ev.data
		def action = map.action
		QueueSocialService.log.debug("Calling OnQueueSocialBroadcastService.onMessage with action " + action)
		if (action == "addResource") {
			return onAddResource(map)
		}
		if (action == "publishProduct") {
			return onPublishProduct(map)
		}
		
		return null
	}
	private def onAddResource(def map) {
		QueueSocialService.log.debug("adding resource")
		if (map.resourceId && map.userId) {
			QueueSocialService.log.debug("adding resource : map fields ok")
			long userId = map.userId
			long resourceId = map.resourceId
			long productId = Long.parseLong(map.productId)
			Resource resource = Resource.get(resourceId)
			Product product =Product.get(productId)
			QueueSocialService.log.debug("adding resource : loaded resource " + resource)
			QueueSocialService.log.debug("adding resource : loaded product " + product)
			if (resource) {
				QueueSocialService.log.debug("adding resource : got resource")
				def albumName = 'PASSNGUIDE'
				def albumDescription = 'PASSNGUIDE'
				File localFile
				// on upload sur chacun des r�seaux sociaux enregistr�s.
				def externalAccount = null
				// FACEBOOK
				if (resource.xtype == ResourceType.PICTURE) {
					QueueSocialService.log.debug("adding resource : got resource xtype")
					QueueSocialService.log.debug("Entering facebook publish")
					// on recherche le compte externe FACEBOOK
					externalAccount = ExternalAccount.findByUserAndAccountType(User.get(userId), AccountType.FACEBOOK)
					QueueSocialService.log.debug("adding resource : account retrieved "+externalAccount)
					if(externalAccount){
						FBClient client = new FBClient(externalAccount.token)
						def album = client.createAlbum (albumName, albumDescription)
						localFile = new File(resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator))
						FileInputStream fis = new FileInputStream(localFile)
						def photo = client.publishPhoto (fis, product.name + " (" + filePrefix(resource.name)+")", album)
						fis.close()
						if(photo){
							QueueSocialService.log.debug("facebook published photo "+ photo)
						}
					}
					// on recherche le compte externe PICASA
					externalAccount = authenticationService.retrieveExternalAccount(userId, AccountType.GOOGLE)
					if(externalAccount){
						PicasaClient client = new PicasaClient(externalAccount.token, externalAccount.tokenSecret)
						// TODO how to retrieve album by id
						def albumId = client.createAlbum(albumName, albumDescription).id
						if(albumId){
							localFile = new File(resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator))
							FileInputStream fis = new FileInputStream(localFile)
							def photo = client.publishPhoto (fis, getMimeType(localFile), resource.name, albumId)
							fis.close()
							if(photo){
							QueueSocialService.log.debug("PICASA published photo "+ photo)
							}
						}
					}
					// on recherche le compte externe TWITPIC
					externalAccount = authenticationService.retrieveExternalAccount(userId, AccountType.TWITTER)
					if(externalAccount){
						TwitterClient client = new TwitterClient(externalAccount.token, externalAccount.tokenSecret)
						localFile = new File(resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator))
						String url = client.uploadImage(localFile)
						client.updateStatus("Passnguide - " + product.name + "("+ url+")")
						QueueSocialService.log.debug("Twitter published photo "+ url)
						
					}
				}
				else if (resource.xtype == ResourceType.VIDEO) {
					// on recherche le compte externe YOUTUBE
					externalAccount = retrieveExternalAccount(userId, AccountType.GOOGLE)
					if(externalAccount){
						YouTubeClient client = new YouTubeClient(externalAccount.token, externalAccount.tokenSecret)
						localFile = new File(resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator))
						FileInputStream fis = new FileInputStream(localFile)
						def video = client.uploadVideo (fis, getMimeType(localFile), resource.name, albumName, resource.description, null)
						fis.close()
						if(video){
							QueueSocialService.log.debug("Youtube published Video "+ video)
						}
					}
					externalAccount = authenticationService.retrieveExternalAccount(userId, AccountType.FACEBOOK)
					if(externalAccount){
						FBClient client = new FBClient(externalAccount.token)
						def album = client.createAlbum (albumName, albumDescription)
						localFile = new File(resource.url.replaceAll("/", File.separator).replaceAll("\\\\", File.separator))
						FileInputStream fis = new FileInputStream(localFile)
						def video = client.publishVideo (fis, product.name + " (" + filePrefix(resource.name)+")", album)
						fis.close()
						if(video){
						QueueSocialService.log.debug("Facebook published Video "+ video)
						}
					}
				}
			}
		}
		return null
	}
	private def onPublishProduct(def map) {
		if (map.productId && map.userId) {
			long userId = map.userId
			long productId = Long.parseLong(map.productId)
			Product product =Product.get(productId)
			if (product) {
				String linkURL = Holders.config.grails.serverURL +"/event/getEvent?event.idEvent="+productId
				String message = product.descriptionAsText
				if (message?.length() > 140) {
					message = message.substring(0, 140)
				}
				String linkName = product.name
				String pictureURL = Holders.config.grails.serverURL + "/store/displayLogo?store=ebiznext"
				// FACEBOOK
				// on recherche le compte externe FACEBOOK
                ExternalAccount externalAccount = ExternalAccount.findByUserAndAccountType(User.get(userId), AccountType.FACEBOOK)
				if(externalAccount){
					FBClient client = new FBClient(externalAccount.token)
					client.publishMessageAndLinkAndPicture(message, linkName, linkURL, pictureURL)
				}
                externalAccount = ExternalAccount.findByUserAndAccountType(User.get(userId), AccountType.TWITTER)
				if(externalAccount){
					TwitterClient client = new TwitterClient(externalAccount.token, externalAccount.tokenSecret)
					client.updateStatus(linkName + linkURL)
				}
			}
		}
		return null
	}
	private String filePrefix(String name) {
		int i = name.lastIndexOf('.')
		String ret
		if (i > 0) {
			ret = name.substring(0, i)
		}
		else {
			ret = name
		}
		return ret
	}

	private String getMimeType(File photoFile) {
		String name = photoFile.getName()
		String mimetype = new MimetypesFileTypeMap().getContentType(photoFile)
		return mimetype
	}

}
