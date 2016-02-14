/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.domain.Album
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.tools.ImageSize
import com.mogobiz.tools.ImageTools
import com.mogobiz.tools.MimeTypeTools
import com.mogobiz.utils.IperUtil


class ResService {
    def grailsApplication
    SanitizeUrlService sanitizeUrlService

    def uploadResource(Company company, Resource resource, File file, String contentType) {
        String resourcesPath = grailsApplication.config.resources.path
        String dir = File.separator + 'resources' + File.separator
        resource.company = company
        dir += company.code + '/'

        log.debug("Processing Upload resource name = " + resource.name)
        resource.sanitizedName = sanitizeUrlService.sanitizeWithDashes(resource.name)
        resource.contentType = contentType ?: MimeTypeTools.detectMimeType(file)
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
        Album album = null
        log.debug("Processing Upload resource url = " + resource.url)
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
        // FIXME JAHIAC-208 resource.content = ImageTools.encodeFileBase64(file)
        if (resource.validate()) {
            resource.save()
            log.debug("Processing Upload resource validated = " + resource)
        }
        def d = new File(resourcesPath + dir)
        d.mkdirs()
        resource.url = dir + resource.id
        final resourceFile = new File(resourcesPath + (IperUtil.normalizeSeparator(resource.url) - resourcesPath))
        file.renameTo(resourceFile)
        if (resource.xtype == ResourceType.PICTURE) {
            resource.smallPicture = ImageTools.getFile(resourceFile, ImageSize.SMALL, true).path - resourcesPath
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
