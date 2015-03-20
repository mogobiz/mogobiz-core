package com.mogobiz.store.partner

import com.mogobiz.authentication.AuthenticationService
import com.mogobiz.service.CatalogService
import com.mogobiz.service.ExportService
import com.mogobiz.service.ImportService
import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Seller
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.transform.Synchronized
import org.hibernate.SessionFactory

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
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

    private final Lock lock = new ReentrantLock();

    @Transactional(readOnly = true)
    def export() {
        if (lock.tryLock()) {
            try {
                log.info("EXPORT STARTED")
                Date start = new Date()
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
                log.info("EXPORT FINISHED")
                Date end = new Date()
                println("EXPORT DURATION (in seconds) =" + (end.getTime() - start.getTime()) / 1000)
            }
            finally {
                lock.unlock()
            }
        } else {
            log.info("EXPORT COULD NOT START")
            render text:"Impex currently busy running", status:403
        }
    }

    @Transactional
    def ximport() {
        def seller = request.seller ? request.seller : authenticationService.retrieveAuthenticatedSeller()
        if (!seller) {
            response.sendError 401
            return
        }
        if (lock.tryLock()) {
            try {
                log.info("Uploading file ...")
                def file = request.getFile('file')
                if (file && !file.empty) {
                    log.info("IMPORT STARTED")
                    Date start = new Date()
                    File tmpFile = File.createTempFile("import", ".zip")
                    file.transferTo(tmpFile)
                    Company company = seller.company
                    def name = "impex"
                    int countSales = 0
                    Catalog catalog
                    Catalog.withNewTransaction {
                        catalog = Catalog.findByNameAndCompany(name, seller.company)
                        if (catalog) {
                            log.info("Purging catalog ...")
                            countSales = catalogService.purge(catalog.id)
                            log.info("Purge ended ...")
                        }
                    }
                    if (countSales == 0) {
                        catalog = new Catalog()
                        catalog.company = company
                        catalog.name = name
                        catalog.activationDate = new Date(2040 - 1900, 11, 31)
                        catalog.uuid = UUID.randomUUID().toString()
                        if (catalog.validate()) {
                            Catalog.withNewTransaction {
                                catalog.save(flush: true)
                            }
                        } else {
                            System.out.println(catalog.errors)
                            catalog = null
                        }
                    }

                    if (countSales == 0 && catalog) {
                        importService.ximport(catalog, new ZipFile(tmpFile))
                        tmpFile.delete()
                        withFormat {
                            json { render catalog as JSON }
                        }
                    } else {
                        tmpFile.delete()
                        render text:"$countSales", status:403
                    }
                    log.info("IMPORT FINISHED")
                    Date end = new Date()
                    log.info("IMPORT DURATION (in seconds) =" + (end.getTime() - start.getTime()) / 1000)
                } else {
                    render text:"Missing file", status:401
                    response.sendError(401, "Missing file")
                }
            }
            finally {
                lock.unlock()
            }
        } else {
            log.info("IMPORT COULD NOT START")
            render text:"Impex currently busy running", status:401
        }
    }
}
