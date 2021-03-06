/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.domain.Country
import com.mogobiz.store.domain.CountryAdmin
import com.mogobiz.tools.CsvLine
import grails.util.Holders
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import rx.Observable
import rx.functions.Action0
import rx.functions.Action1
import rx.functions.Func1
import rx.observables.BlockingObservable

import static com.mogobiz.tools.Reader.getTrim
import static com.mogobiz.tools.Reader.parseCsvFile

class CountryImportService {
    // http://download.geonames.org/export/dump/readme.txt
    boolean transactional = true

    def sessionFactory

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        ThreadLocal<Map> propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP as ThreadLocal<Map>
        propertyInstanceMap.get().clear()
    }


    void importAll(Collection<String> countryCodes = [], final File countriesDir, final String charset = "UTF-8") {
        if (countryCodes.size() == 0) {
            countryCodes = (Holders.config.importCountries.codes as String)?.split(',')
        } else {
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

    void importCountries(File countriesDir,
                         final Collection<String> countryCodes = [], final String charset = "UTF-8") {
        File countries = new File(countriesDir, "countries.txt")
        File currencies = new File(countriesDir, "currencies.txt")
        if (!countries.exists() || !currencies.exists())
            return

        //CountryAdmin.executeUpdate("delete from CountryAdmin")

        final Map<String, String> currencyMap = [:]

        BlockingObservable.from(parseCsvFile(currencies, charset, "\t", trim, trim, false)).forEach(
                new Action1<CsvLine>() {
                    void call(CsvLine csvLine) {
                        currencyMap << ["${csvLine.values[0]}": csvLine.values[1]]
                    }
                }
        )

        parseCsvFile(countries, charset, "\t", trim, trim, false).filter(
                new Func1<CsvLine, Boolean>() {
                    Boolean call(CsvLine csvLine) {
                        try {
                            final countryCode = csvLine.values[0].trim().toUpperCase()
                            def ret = countryCodes.empty || countryCodes.contains(countryCode)
                            if (!ret) {
                                log.debug("CountryService.importCountries: ${csvLine.number} Not added (Country $countryCode not found within $countryCodes)")
                            }
                            ret
                        }
                        catch (Throwable th) {
                            log.error(th.message)
                            false
                        }

                    }
                }
        ).subscribe(
                { CsvLine csvLine ->
                    final String code = csvLine.values[0].trim().toUpperCase()
                    final String isoCode3 = csvLine.values[1].trim().toUpperCase()
                    final String isoNumericCode = csvLine.values[2]
                    final String name = csvLine.values[4]
                    final String postalCodeRegex = csvLine.values[14]
                    final String phoneCode = csvLine.values[12]
                    final String currencyCode = csvLine.values[10]
                    final String currencyName = csvLine.values[11]
                    Country.withSession {
                        Country country = Country.findByCode(code)
                        if (country == null) {
                            log.info("CountryService.importCountries: ${csvLine.number} Adding $code $name")
                            country = new Country(
                                    code: code,
                                    isoCode3: isoCode3,
                                    isoNumericCode: isoNumericCode,
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
                            country.isoCode3 = isoCode3
                            country.isoNumericCode = isoNumericCode
                            country.lastUpdated = new Date()
                            log.info("CountryService.importCountries: ${csvLine.number} Not added $code $name (already exist)")
                        }
                        country.validate()
                        if (country.hasErrors()) {
                            country.errors.allErrors.each { log.error(it) }
                        } else {
                            country.save(flush: true)
                        }
                    }
                } as Action1<CsvLine>,
                { th ->
                    log.error(th.message)
                } as Action1<Throwable>,
                {
                    importAdmin1(countriesDir, countryCodes, charset)
                } as Action0
        )
    }

    void importAdmin1(File countriesDir, final Collection<String> countryCodes = [], final String charset = "UTF-8") {
        File admins1 = new File(countriesDir, "admins1.txt")
        if (!admins1.exists()) {
            return
        }

        final Set<String> local = []

        def importLocal = {
            Observable.from(local).subscribe(
                    { countryCode ->
                        File countryDir = new File(countriesDir, countryCode as String)
                        File localAdmin1 = new File(countryDir, "admins1.txt")
                        def instance = this.class.classLoader.loadClass("${countryCode}.Import", true)?.newInstance()
                        instance?.importAdmin1(localAdmin1)
                    } as Action1<String>,
                    { th ->
                        log.error(th.message)
                    } as Action1<Throwable>,
                    {
                        importAdmin2(countriesDir, countryCodes, charset)
                    } as Action0
            )
        }

        parseCsvFile(admins1, charset, "\t", trim, trim, false).filter(
                new Func1<CsvLine, Boolean>() {
                    Boolean call(CsvLine csvLine) {
                        try {
                            final String countryCode = csvLine.values[0].tokenize(".").get(0).trim().toUpperCase()
                            boolean ret = countryCodes.empty || countryCodes.contains(countryCode)
                            if (ret) {
                                File countryDir = new File(countriesDir, countryCode)
                                File localAdmin1 = new File(countryDir, "admins1.txt")
                                if (localAdmin1.exists()) {
                                    local.add(countryCode)
                                }
                            } else {
                                log.debug("CountryService.importAdmin1: ${csvLine.number} Not Added (Country $countryCode not found within $countryCodes)")
                            }
                            ret && !local.contains(countryCode)
                        }
                        catch (Throwable th) {
                            log.warn("${csvLine.number} -> ${th.message}")
                            false
                        }

                    }
                }
        ).subscribe(
                { CsvLine csvLine ->
                    final String code = csvLine.values[0]
                    final String name = csvLine.values[2]
                    final String countryCode = code.tokenize(".").get(0)
                    CountryAdmin.withSession {
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
                } as Action1<CsvLine>,
                { th ->
                    log.error(th.message)
                } as Action1<Throwable>,
                {
                    importLocal()
                } as Action0
        )

    }

    void importAdmin2(File countriesDir, final Collection<String> countryCodes = [], final String charset = "UTF-8") {
        File admins2 = new File(countriesDir, "admins2.txt")
        if (!admins2.exists()) {
            return
        }

        final Set<String> local = []

        def importLocal = {
            Observable.from(local).subscribe(
                    { String countryCode ->
                        File countryDir = new File(countriesDir, countryCode)
                        File localAdmin2 = new File(countryDir, "admins2.txt")
                        def instance = this.class.classLoader.loadClass("${countryCode}.Import", true)?.newInstance()
                        instance?.importAdmin2(localAdmin2)
                    } as Action1<String>,
                    { th ->
                        log.error(th.message)
                    } as Action1<Throwable>,
                    {
                        importCities(countriesDir, countryCodes, charset)
                    } as Action0
            )
        }

        parseCsvFile(admins2, charset, "\t", trim, trim, false).filter(
                new Func1<CsvLine, Boolean>() {
                    Boolean call(CsvLine csvLine) {
                        try {
                            final countryCode = csvLine.values[0].tokenize(".").get(0).trim().toUpperCase()
                            def ret = countryCodes.empty || countryCodes.contains(countryCode)
                            if (ret) {
                                File countryDir = new File(countriesDir, countryCode)
                                File localAdmin2 = new File(countryDir, "admins2.txt")
                                if (localAdmin2.exists()) {
                                    local.add(countryCode)
                                }
                            } else {
                                log.debug("CountryService.importAdmin2: ${csvLine.number} Not Added (Country $countryCode not found within $countryCodes)")
                            }
                            ret && !local.contains(countryCode)
                        }
                        catch (Throwable th) {
                            log.warn("${csvLine.number} -> ${th.message}")
                            false
                        }
                    }
                }).subscribe(
                { CsvLine csvLine ->
                    final String code = csvLine.values[0]
                    String name = csvLine.values[2]
                    final String countryCode = code.tokenize(".").get(0).trim().toUpperCase()
                    CountryAdmin.withSession {
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
                } as Action1<CsvLine>,
                { th ->
                    log.error(th.message)
                } as Action1<Throwable>,
                {
                    importLocal()
                } as Action0
        )

    }

    void importCities(File countriesDir, final Collection<String> countryCodes = [], final String charset = "UTF-8") {
        File cities = new File(countriesDir, "cities.txt")
        if (!cities.exists()) {
            return
        }

        final Set<String> local = []

        def importLocal = {
            Observable.from(local).subscribe(
                    new Action1<String>() {
                        void call(String countryCode) {
                            File countryDir = new File(countriesDir, countryCode)
                            File localCities = new File(countryDir, "cities.txt")
                            def instance = this.class.classLoader.loadClass("${countryCode}.Import", true)?.newInstance()
                            instance?.importCities(localCities)
                        }
                    }
            )
        }
        int count = 0
        parseCsvFile(cities, charset, "\t", trim, trim, false).filter(
                new Func1<CsvLine, Boolean>() {
                    Boolean call(CsvLine csvLine) {
                        try {
                            final countryCode = csvLine.values[8]
                            def ret = countryCodes.empty || countryCodes.contains(countryCode)
                            if (ret) {
                                File countryDir = new File(countriesDir, countryCode)
                                File localCities = new File(countryDir, "cities.txt")
                                if (localCities.exists()) {
                                    local.add(countryCode)
                                }
                            } else {
                                log.debug("CountryService.importCities: ${csvLine.number} Not Added (Country $countryCode not found within $countryCodes)")
                            }
                            ret && !local.contains(countryCode)
                        }
                        catch (Throwable th) {
                            log.warn("${csvLine.number} -> ${th.message}")
                            false
                        }
                    }
                }).subscribe(
                { CsvLine csvLine ->
                    final String cityCode = csvLine.values[2]
                    final String countryCode = csvLine.values[8]
                    String a1code = csvLine.values[10]
                    String a2code = csvLine.values[11]

                    if (a1code.length() == 1) a1code = "0" + a1code
                    if (a2code.length() == 1) a2code = "0" + a2code
                    if (count == 1000) {
                        count = 0
                        this.cleanUpGorm()
                    }
                    CountryAdmin.withSession {
                        count++
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
                } as Action1<CsvLine>,
                { th ->
                    log.error(th.message)
                } as Action1<Throwable>,
                {
                    importLocal()
                } as Action0
        )

    }
}
