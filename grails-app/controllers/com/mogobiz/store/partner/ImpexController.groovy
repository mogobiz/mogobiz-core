package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.service.CatalogService
import com.mogobiz.service.ExportService
import com.mogobiz.service.ImportService
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Company
import grails.converters.JSON

import java.util.zip.ZipOutputStream

class ImpexController {

    ExportService exportService
    ImportService importService
    CatalogService catalogService
    AuthenticationService authenticationService

    def purge() {
        try {
            int count = catalogService.purge(31063L)
            render "$count"
        }
        catch (Exception e) {
            response.sendError(404)
        }
    }

    def export() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        File zipFile = exportService.export(31262L)
        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "Attachment;Filename=\"${zipFile.getName()}\"")
        response.outputStream << zipFile.newInputStream()
        zipFile.delete()
    }

    def ximport() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
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
                importService.ximport(catalog, null)
            } else {
                System.out.println(catalog.errors)
            }
            withFormat {
                json { render catalog as JSON }
            }
        } else {
            response.sendError(403, "$countSales")
        }
    }
}
