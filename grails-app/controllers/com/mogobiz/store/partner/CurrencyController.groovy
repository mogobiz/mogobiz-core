package com.mogobiz.store.partner

import grails.converters.JSON
import grails.transaction.Transactional
import grails.util.Holders

class CurrencyController {

    File countriesDir = new File(Holders.config.importCountries.dir as String)
    File rateFile = new File(countriesDir, "rates.txt")
    Map defaultCurrency = [:]
    /**
     * First currency in the rate file
     * @return
     */
    @Transactional(readOnly = true)
    def defaultCurrency() {
        if (defaultCurrency.size() == 0) {
            String line
            rateFile.withReader { line = it.readLine() }
            String[] fields = line.split('\t')
            String currencyCode = fields[0].trim()
            int fractionDigits = fields[2].toInteger()
            defaultCurrency = ["currencyCode": currencyCode, "fractionDigits": fractionDigits]
        }
        render defaultCurrency as JSON;
    }

}
