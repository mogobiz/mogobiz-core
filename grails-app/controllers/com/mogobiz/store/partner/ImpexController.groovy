package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.service.CatalogService
import com.mogobiz.service.ExportService
import com.mogobiz.service.ImportService
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Seller
import com.sun.corba.se.spi.orbutil.threadpool.Work
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory

import java.sql.Connection
import java.sql.SQLException
import java.util.zip.ZipFile

class ImpexController {

    ExportService exportService
    CatalogService catalogService
    AuthenticationService authenticationService
    ImportService importService
    SessionFactory sessionFactory

    @Transactional
    def purge() {
        try {
            long catalogId = params.long(["catalog.id"])
            int count = catalogService.purge(catalogId)
            render "$count"
        }
        catch (Exception e) {
            response.sendError(404)
        }
    }

    @Transactional(readOnly = true)
    def export() {
        Seller seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }

        long catalogId = params.long(["catalog.id"])
        if (Catalog.get(catalogId).company != seller.company) {
            response.sendError 401
            return
        }

        File zipFile = exportService.export(catalogId)
        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "Attachment;Filename=\"${zipFile.getName()}\"")
        response.outputStream << zipFile.newInputStream()
        zipFile.delete()
    }

    @Transactional
    def ximport() {
        Date start = new Date()
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        def file = request.getFile('file')
        if (file && !file.empty) {
            File tmpFile = File.createTempFile("import", ".zip")
            file.transferTo(tmpFile)
            Company company = seller.company
            def name = "impex"
            Catalog catalog = Catalog.findByNameAndCompany(name, seller.company)
            int countSales = 0
            if (catalog)
                countSales = catalogService.purge(catalog.id)

            if (countSales == 0) {
                catalog = new Catalog()
                catalog.company = company
                catalog.name = name
                catalog.activationDate = new Date(2040 - 1900, 11, 31)
                catalog.uuid = UUID.randomUUID().toString()
                if (catalog.validate()) {
                    catalog.save(flush: true)
                    importService.ximport(catalog, new ZipFile(tmpFile))
                } else {
                    System.out.println(catalog.errors)
                }
                tmpFile.delete()
                withFormat {
                    json { render catalog as JSON }
                }
            } else {
                tmpFile.delete()
                response.sendError(403, "$countSales")
            }
        } else {
            response.sendError(401, "Missing file")
        }
        Date end = new Date()
        println("IMPORT DURATION (in seconds) =" + (end.getTime() - start.getTime()) / 1000)
    }
}
