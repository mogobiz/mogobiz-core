/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.store.partner

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.TicketType
import com.mogobiz.tools.MimeTypeTools
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import javax.servlet.http.HttpServletResponse

class DownloadableController {

    def grailsApplication

    def authenticationService

    /**
     *
     */
    static final int BUFFER_SIZE = 2048

    def save(Long id){
        final ticketType = TicketType.load(id)
        if(!ticketType){
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        else{
            Company store = ticketType.product?.company
            if(authenticationService.canAccessStore(store)){
                MultipartFile file = (request as MultipartHttpServletRequest).getFile('file')
                String resourcesPath = grailsApplication.config.resources.path
                String dir = "$resourcesPath${File.separator}resources${File.separator}${store.code}${File.separator}sku"
                def parent = new File(dir)
                parent.mkdirs()
                file.transferTo(new File(parent, id as String))
                ticketType.filename = file.getOriginalFilename()
                ticketType.save(flush:true)
                response.status = HttpServletResponse.SC_OK
                render "success"
            }
            else{
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
        }
    }

    def delete(Long id){
        final ticketType = TicketType.load(id)
        if(!ticketType){
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        else{
            Company store = ticketType.product?.company
            if(authenticationService.canAccessStore(store)){
                String resourcesPath = grailsApplication.config.resources.path
                String dir = "$resourcesPath${File.separator}resources${File.separator}${store.code}${File.separator}sku"
                def file = new File(dir, id as String)
                if(!file.exists()){
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                }
                else{
                    file.delete()
                    ticketType.filename = ""
                    ticketType.save(flush:true)
                    response.status = HttpServletResponse.SC_OK
                    render "success"
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
        }
    }

    def display(Long id){
        final ticketType = TicketType.load(id)
        if(!ticketType){
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        else{
            Company store = ticketType.product?.company
            if(authenticationService.canAccessStore(store)){
                String resourcesPath = grailsApplication.config.resources.path
                String dir = "$resourcesPath${File.separator}resources${File.separator}${store.code}${File.separator}sku"
                def file = new File(dir, id as String)
                if(!file.exists()){
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                }
                else{
                    response.contentType = MimeTypeTools.detectMimeType(file);
                    response.setHeader("Content-disposition", "attachment;filename=${ticketType.filename}")
                    def out = response.outputStream
                    def bytes = new byte[BUFFER_SIZE]
                    file.withInputStream { inp ->
                        while( inp.read(bytes) != -1) {
                            out.write(bytes)
                            out.flush()
                        }
                    }
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
        }
    }

    def hasResource(Long id){
        final ticketType = TicketType.load(id)
        if(!ticketType){
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        else{
            Company store = ticketType.product?.company
            if(authenticationService.canAccessStore(store)){
                String resourcesPath = grailsApplication.config.resources.path
                String dir = "$resourcesPath${File.separator}resources${File.separator}${store.code}${File.separator}sku"
                def file = new File(dir, id as String)
                response.status = HttpServletResponse.SC_OK
                if(file.exists()){
                    render "true"
                }
                else{
                    render "false"
                }
            }
            else{
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
        }
    }
}
