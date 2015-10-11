/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.domain.Country
import com.mogobiz.store.domain.CountryAdmin
import com.mogobiz.tools.CsvLine
import grails.util.Holders
import rx.Observable
import rx.functions.Action0
import rx.functions.Action1
import rx.observables.BlockingObservable

import static com.mogobiz.tools.Reader.*

class CountryImportService {

    boolean transactional = true

    void importAll(Collection<String> countryCodes = [], final File countriesDir, final String charset= "UTF-8"){
        if(countryCodes.size() == 0){
            countryCodes = (Holders.config.importCountries.codes as String)?.split(',')
        }
        else{
            // the client may want to import other countries
            countriesDir.setLastModified(new Date().time)
        }

        Date lastUpdated = Country.createCriteria().get {
            projections { max "lastUpdated" }
        } as Date
        if (lastUpdated && lastUpdated.getTime() >= countriesDir.lastModified())
            return

        try {
            importCountries(countriesDir, countryCodes, charset)
        }
        catch (Exception e) {
            log.error(e.message)
        }

    }

    void importCountries(File countriesDir, final Collection<String> countryCodes = [], final String charset= "UTF-8") {
        File countries = new File(countriesDir, "countries.txt")
        File currencies = new File(countriesDir, "currencies.txt")
        if (!countries.exists() || !currencies.exists())
            return

        //CountryAdmin.executeUpdate("delete from CountryAdmin")

        final Map<String, String> currencyMap = [:]
        BlockingObservable.from(parseCsvFile(currencies, charset, "\t", trim)).forEach{ csvLine ->
            currencyMap << ["${csvLine.fields[0]}": csvLine.fields[1]]
        }as Action1<CsvLine>

        parseCsvFile(countries, charset, "\t", trim).filter{csvLine ->
            try{
                final countryCode = csvLine.fields[0].trim().toUpperCase()
                def ret = countryCodes.empty || countryCodes.contains(countryCode)
                if(!ret){
                    log.debug("CountryService.importCountries: ${csvLine.number} Not added (Country $countryCode not found within $countryCodes)")
                }
                ret
            }
            catch(Throwable th){
                log.error(th.message)
                false
            }
        }.subscribe(
            { csvLine ->
                final code = csvLine.fields[0].trim().toUpperCase()
                final name = csvLine.fields[4]
                final postalCodeRegex = csvLine.fields[14]
                final phoneCode = csvLine.fields[12]
                final currencyCode = csvLine.fields[10]
                final currencyName = csvLine.fields[11]
                Country.withTransaction {
                    Country country = Country.findByCode(code)
                    if (country == null) {
                        log.info("CountryService.importCountries: ${csvLine.number} Adding $code $name")
                        country = new Country(
                                code: code,
                                name: name,
                                shipping: true,
                                billing: true,
                                phoneCode: phoneCode,
                                currencyCode: currencyCode,
                                currencyName: currencyName,
                                currencyNumericCode: currencyMap.get(currencyCode),
                                postalCodeRegex: postalCodeRegex
                        )
                    } else {
                        country.lastUpdated = new Date()
                        log.info("CountryService.importCountries: ${csvLine.number} Not added $code $name (already exist)")
                    }
                    country.validate()
                    if(country.hasErrors()){
                        country.errors.allErrors.each {log.error(it)}
                    }
                    else{
                        country.save(flush: true)
                    }
                }
            }as Action1<CsvLine>,
            { th ->
                log.error(th.message)
            }as Action1<Throwable>,
            {
                importAdmin1(countriesDir, countryCodes, charset)
            }as Action0
        )
    }

    void importAdmin1(File countriesDir, final Collection<String> countryCodes = [], final String charset= "UTF-8") {
        File admins1 = new File(countriesDir, "admins1.txt")
        if (!admins1.exists()) {
            return
        }

        final Set<String> local = []

        def importLocal = {
            Observable.from(local).subscribe(
                {countryCode ->
                    File countryDir = new File(countriesDir, countryCode)
                    File localAdmin1 = new File(countryDir, "admins1.txt")
                    def instance = this.class.classLoader.loadClass(countryCode + ".Import", true)?.newInstance()
                    instance?.importAdmin1(localAdmin1)
                }as Action1<String>,
                {th ->
                    log.error(th.message)
                }as Action1<Throwable>,
                {
                    importAdmin2(countriesDir, countryCodes, charset)
                }as Action0
            )
        }

        parseCsvFile(admins1, charset, "\t", trim).filter{csvLine ->
            try{
                final countryCode = csvLine.fields[0].tokenize(".").get(0).trim().toUpperCase()
                def ret = countryCodes.empty || countryCodes.contains(countryCode)
                if(ret) {
                    File countryDir = new File(countriesDir, countryCode)
                    File localAdmin1 = new File(countryDir, "admins1.txt")
                    if (localAdmin1.exists()) {
                        local.add(countryCode)
                    }
                }
                else{
                    log.debug("CountryService.importAdmin1: ${csvLine.number} Not Added (Country $countryCode not found within $countryCodes)")
                }
                ret && !local.contains(countryCode)
            }
            catch(Throwable th){
                log.warn("${csvLine.number} -> ${th.message}")
                false
            }
        }.subscribe(
            {csvLine ->
                final code = csvLine.fields[0]
                final name = csvLine.fields[2]
                final countryCode = code.tokenize(".").get(0)
                CountryAdmin.withTransaction {
                    CountryAdmin admin = CountryAdmin.findByCodeAndLevel(code, 1)
                    if (admin == null) {
                        log.info("CountryService.importAdmin1:----> $code")
                        Country country = Country.findByCode(countryCode)
                        if (country) {
                            admin = new CountryAdmin(code: code, name: name, level: 1, country: country)
                            admin.save(flush: true)
                            log.info("CountryService.importAdmin1: ${csvLine.number} Adding $code $name")
                        } else {
                            log.error("CountryService.importAdmin1: ${csvLine.number} Not Added $code $name (Country $countryCode not found)")
                        }
                    } else {
                        log.info("CountryService.importAdmin1: ${csvLine.number} Not added $code $name (already exist)")
                    }
                }
            }as Action1<CsvLine>,
            { th ->
                log.error(th.message)
            }as Action1<Throwable>,
            {
                importLocal()
            }as Action0
        )

    }

    void importAdmin2(File countriesDir, final Collection<String> countryCodes = [], final String charset= "UTF-8") {
        File admins2 = new File(countriesDir, "admins2.txt")
        if (!admins2.exists()) {
            return
        }

        final Set<String> local = []

        def importLocal = {
            Observable.from(local).subscribe(
                {countryCode ->
                    File countryDir = new File(countriesDir, countryCode)
                    File localAdmin2 = new File(countryDir, "admins2.txt")
                    def instance = this.class.classLoader.loadClass(countryCode + ".Import", true)?.newInstance()
                    instance?.importAdmin2(localAdmin2)
                }as Action1<String>,
                {th ->
                    log.error(th.message)
                }as Action1<Throwable>,
                {
                    importCities(countriesDir, countryCodes, charset)
                }as Action0
            )
        }

        parseCsvFile(admins2, charset, "\t", trim).filter{csvLine ->
            try{
                final countryCode = csvLine.fields[0].tokenize(".").get(0).trim().toUpperCase()
                def ret = countryCodes.empty || countryCodes.contains(countryCode)
                if(ret) {
                    File countryDir = new File(countriesDir, countryCode)
                    File localAdmin2 = new File(countryDir, "admins2.txt")
                    if (localAdmin2.exists()) {
                        local.add(countryCode)
                    }
                }
                else{
                    log.debug("CountryService.importAdmin2: ${csvLine.number} Not Added (Country $countryCode not found within $countryCodes)")
                }
                ret && !local.contains(countryCode)
            }
            catch(Throwable th){
                log.warn("${csvLine.number} -> ${th.message}")
                false
            }
        }.subscribe(
            { csvLine ->
                final code = csvLine.fields[0]
                def name = csvLine.fields[2]
                final countryCode = code.tokenize(".").get(0).trim().toUpperCase()
                CountryAdmin.withTransaction {
                    CountryAdmin admin2 = CountryAdmin.findByCodeAndLevel(code, 2)
                    if (admin2 == null) {
                        final admin1Code = countryCode + "." + code.tokenize(".").get(1)
                        CountryAdmin admin1 = CountryAdmin.findByCodeAndLevel(admin1Code, 1)
                        if (admin1) {
                            name = name.replace("Departement ", "")
                            if (name.startsWith("de ") || name.startsWith("du "))
                                name = name.substring(3);
                            if (name.startsWith("des "))
                                name = name.substring(4);
                            if (name.startsWith("la ") || name.startsWith("le "))
                                name = name.substring(3);
                            if (name.startsWith("l'") || name.startsWith("d'"))
                                name = name.substring(2);

                            admin2 = new CountryAdmin(code: code, name: name, level: 2, country: admin1.country, parent: admin1)
                            admin2.save(flush: true)
                            log.info("CountryService.importAdmin2: ${csvLine.number} Adding $code $name")
                        } else {
                            log.error("CountryService.importAdmin2: ${csvLine.number} Not Added $code $name (Admin1 $admin1Code not found)")
                        }
                    } else {
                        log.info("CountryService.importAdmin2: ${csvLine.number} Not added $code $name (already exist)")
                    }
                }
            }as Action1<CsvLine>,
            { th ->
                log.error(th.message)
            }as Action1<Throwable>,
            {
                importLocal()
            }as Action0
        )

    }

    void importCities(File countriesDir, final Collection<String> countryCodes = [], final String charset= "UTF-8") {
        File cities = new File(countriesDir, "cities.txt")
        if (!cities.exists()) {
            return
        }

        final Set<String> local = []

        def importLocal = {
            Observable.from(local).subscribe{countryCode ->
                File countryDir = new File(countriesDir, countryCode)
                File localCities = new File(countryDir, "cities.txt")
                def instance = this.class.classLoader.loadClass(countryCode + ".Import", true)?.newInstance()
                instance?.importCities(localCities)
            }as Action1<String>
        }

        parseCsvFile(cities, charset, "\t", trim).filter{csvLine ->
            try{
                final countryCode = csvLine.fields[8]
                def ret = countryCodes.empty || countryCodes.contains(countryCode)
                if(ret) {
                    File countryDir = new File(countriesDir, countryCode)
                    File localCities = new File(countryDir, "cities.txt")
                    if (localCities.exists()) {
                        local.add(countryCode)
                    }
                }
                else{
                    log.debug("CountryService.importCities: ${csvLine.number} Not Added (Country $countryCode not found within $countryCodes)")
                }
                ret && !local.contains(countryCode)
            }
            catch(Throwable th){
                log.warn("${csvLine.number} -> ${th.message}")
                false
            }
        }.subscribe (
            { csvLine ->
                final cityCode = csvLine.fields[2]
                final countryCode = csvLine.fields[8]
                def a1code = csvLine.fields[10]
                def a2code = csvLine.fields[11]

                if (a1code.length() == 1) a1code = "0" + a1code
                if (a2code.length() == 1) a2code = "0" + a2code

                CountryAdmin.withTransaction {
                    CountryAdmin city = CountryAdmin.findByCodeAndLevel(cityCode, 3)
                    if (city == null) {
                        String admin2Code = countryCode + "." + a1code + "." + a2code
                        CountryAdmin admin2 = CountryAdmin.findByCodeAndLevel(admin2Code, 2)
                        if (admin2) {
                            if (null == CountryAdmin.findByCodeAndLevel(cityCode, 3)) {
                                city = new CountryAdmin(code: cityCode, name: cityCode, level: 3, country: admin2.country, parent: admin2)
                                city.save(flush: true);
                            }
                            log.info("CountryService.importCities: ${csvLine.number} Adding $cityCode")
                        } else {
                            log.warn("CountryService.importCities: ${csvLine.number} Not Added $cityCode (Admin2 $admin2Code not found)")
                        }
                    } else {
                        log.warn("CountryService.importAdmin2: ${csvLine.number} Not added $cityCode (already exist)")
                    }
                }
            }as Action1<CsvLine>,
            {th ->
                log.error(th.message)
            }as Action1<Throwable>,
            {
                importLocal()
            }as Action0
        )

    }
}
