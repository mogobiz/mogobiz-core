/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

/**
 *
 */
package com.mogobiz.store.admin

import org.apache.log4j.FileAppender
import org.apache.log4j.Logger

class AdminController {

    def index() {}

    def initCompany() {}

    def log() {
        String logvar = System.properties.getProperty("mogobiz.config.log")
        java.io.File logDir = new java.io.File(logvar)
        String name = params.name
        boolean inline = params.boolean("inline", true)
        String disposition = inline ? "inline" : "attachment"
        if (name) {
            java.io.File logfile = new java.io.File(logDir, name)
            if (logfile.exists())
            {
                response.setContentType("text/plain") // or or image/JPEG or text/xml or whatever type the file is
                response.setHeader("Content-disposition", "$disposition;filename=\"${logfile.name}\"")
                logfile.withInputStream { response.outputStream << it }
            }
            else
                render "file name $name not found"
        }
        else {
            String[] names = logDir.list(new FilenameFilter() {
                @Override
                boolean accept(File dir, String nam) {
                    return nam.endsWith(".log")
                }
            });
            render names.toString()
        }

    }

}
