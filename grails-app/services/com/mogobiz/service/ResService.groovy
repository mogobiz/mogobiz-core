package com.mogobiz.service

import com.mogobiz.store.domain.Album
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.utils.ImageSize
import com.mogobiz.utils.ImageUtil


class ResService {
    def grailsApplication
    SanitizeUrlService sanitizeUrlService

    def uploadResource(Company company, Resource resource, File file, String contentType) {
        String dir = grailsApplication.config.rootPath + File.separator + 'resources' + File.separator
        resource.company = company
        dir += company.code + '/'

        log.debug("Processing Upload resource name = " + resource.name)
        resource.sanitizedName = sanitizeUrlService.sanitizeWithDashes(resource.name)
        resource.contentType = contentType
        if (contentType != null) {
            if (contentType.toLowerCase().contains('audio')) {
                resource.xtype = ResourceType.AUDIO
                dir += 'audio/'
            } else if (contentType.toLowerCase().contains('video')) {
                resource.xtype = ResourceType.VIDEO
                dir += 'video/'
            } else if (contentType.toLowerCase().contains('image')) {
                resource.xtype = ResourceType.PICTURE
                dir += 'image/'
            } else if (contentType.toLowerCase().contains('text')) {
                resource.xtype = ResourceType.TEXT
                dir += 'text/'
            }
        }
        resource.url = dir + resource.name
        def albumName = 'Mogobiz'
        def albumDescription = 'Mogobiz'
        Album album
        log.debug("Processing Upload resource rul = " + resource.url)
        if (company != null) {
            album = Album.findByNameAndCompany(albumName, company)
            if (!album) {
                album = new Album(
                        name: albumName,
                        description: albumDescription,
                        company: company
                )
                if (album.validate()) {
                    album.save()
                }
            }
        }
        resource.album = album
        if (resource.validate()) {
            resource.save()
            log.debug("Processing Upload resource validated = " + resource)
        }
        def d = new File(dir)
        d.mkdirs()
        resource.url = dir + resource.id
        file.renameTo(new File(resource.url))
        if (resource.xtype == ResourceType.PICTURE) {
            resource.smallPicture = ImageUtil.getFile(new File(resource.url), ImageSize.SMALL, true)
        }
        resource.uploaded = true
        if (resource.validate()) {
            resource.save(flush: true)
            log.debug("Processing Upload resource validated 2= " + resource)
        } else {
            log.debug("Processing Upload resource not validated 2= " + resource)
            resource.errors.each { log.debug it }
            return null
        }
        return resource
    }
}
