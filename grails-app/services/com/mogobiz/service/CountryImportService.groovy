package com.mogobiz.service

import com.mogobiz.store.domain.Country
import com.mogobiz.store.domain.CountryAdmin
import grails.util.Holders


import java.text.SimpleDateFormat


class CountryImportService {

    boolean transactional = true

    void importAll() {
        File countries = new File(Holders.config.importCountries.dir as String)
        File admins1 = new File(Holders.config.importCountries.dir as String)
        File admins2 = new File(Holders.config.importCountries.dir as String)
        File cities = new File(Holders.config.importCountries.dir as String)

        Date lastUpdated = Country.createCriteria().get {
            projections { max "lastUpdated" }
        } as Date
        if (lastUpdated && lastUpdated.getTime() >= countries.lastModified())
            return ;

        try {
            this.importCountries(countries)
            this.importAdmin1(admins1)
            this.importAdmin2(admins2)
            this.importCities(cities)
//            cities.renameTo(new File(cities.getAbsolutePath() + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())))
//            if (Environment.currentEnvironment == Environment.PRODUCTION) {
//                String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
//                countries.renameTo(new File(countries.getAbsolutePath() + "." + now))
//                admins1.renameTo(new File(admins1.getAbsolutePath() + "." + now))
//                admins2.renameTo(new File(admins2.getAbsolutePath() + "." + now))
//            }
        }
        catch (Exception e) {
            log.error(e.message)
        }

    }
    File importCountries(File countriesDir) {
        File countries = new File(countriesDir, "countries.txt")
        File currencies = new File(countriesDir, "currencies.txt")
        if (!countries.exists() || !currencies.exists())
            return null

        CountryAdmin.executeUpdate("delete from CountryAdmin")
        Map<String,String> currencyMap = [:]
        currencies.eachLine() { String line ->
            if (line.trim().length() > 0) {
                def field = line.trim().tokenize("\t")
                currencyMap[field[0]] = field[1]
            }
        }
        long lineCount = 0
        countries.eachLine() { String line ->
            def field = line.tokenize("\t")
            lineCount++
            String code = field[0]
            String name = field[4]
            String postalCodeRegex = field[14]
            String phoneCode = field[12]
            String currencyCode = field[10]
            String currencyName = field[11]
            if (Holders.config.importCountries.codes.contains(code) || Holders.config.importCountries.codes.empty) {
                Country country = Country.get(code)
                if (country == null) {
                    log.info("CountryService.importCountries: $lineCount Adding $code $name")
                    country = new Country(code:code,
                            name:name,
                            shipping:true,
                            billing:true,
                            phoneCode:phoneCode,
                            currencyCode:currencyCode,
                            currencyName:currencyName,
                            currencyNumericCode:currencyMap.get(currencyCode),
                            postalCodeRegex:postalCodeRegex
                    )
                    country.save(flush:true)
                }
                else {
                    log.info("CountryService.importCountries: $lineCount Not added $code $name (already exist)")
                }
            }
        }
        return countries
    }

    File importAdmin1(File countriesDir) {
        File admins1 = new File(countriesDir, "admins1.txt")
        if(!admins1.exists()){
            return null
        }
        Map<String, Boolean> map = [:]
        long lineCount = 0
        admins1.eachLine() { line ->
            def field = line.tokenize("\t")
            lineCount++
            String code = field[0]
            String name = field[2]
            String countryCode = code.tokenize(".").get(0)
            if (Holders.config.importCountries.codes.contains(countryCode) || Holders.config.importCountries.codes.empty) {
                File countryDir = new File(countriesDir, countryCode)
                File localAdmin1 = new File(countryDir, "admins1.txt")
                if (!map.get(countryCode)) {
                    if (localAdmin1.exists()) {
                        map.put(countryCode, true)
                        def instance = this.class.classLoader.loadClass( countryCode+".Import", true)?.newInstance()
                        instance.importAdmin1(localAdmin1)
                    }
                    else {
                        CountryAdmin admin = CountryAdmin.findByCodeAndLevel(code, 1)
                        if (admin == null) {
                            log.info("CountryService.importAdmin1:----> $code")
                            Country country = Country.findByCode(countryCode)
                            if (country) {
                                admin = new CountryAdmin(code:code, name:name, level:1, country:country)
                                admin.save(flush:true)
                                log.info("CountryService.importAdmin1: $lineCount Adding $code $name")
                            }
                            else {
                                log.error("CountryService.importAdmin1: $lineCount Not Added $code $name (Country not found)")
                            }
                        }
                        else {
                            log.info("CountryService.importAdmin1: $lineCount Not added $code $name (already exist)")
                        }
                    }
                }
            }
        }
        return admins1
    }

    File importAdmin2(File countriesDir) {
        File admins2 = new File(countriesDir, "admins2.txt")
        if(!admins2.exists()){
            return null
        }
        long lineCount = 0
        Map<String, Boolean> map = [:]
        admins2.eachLine() { line ->
            def field = line.tokenize("\t")
            lineCount++
            String code = field[0]
            String name = field[2]
            String countryCode = code.tokenize(".").get(0)
            if (Holders.config.importCountries.codes.contains(countryCode) || Holders.config.importCountries.codes.length() == 0) {
                File countryDir = new File(countriesDir, countryCode)
                File localAdmin2 = new File(countryDir, "admins2.txt")
                if (!map.get(countryCode)) {
                    if (localAdmin2.exists()) {
                        map.put(countryCode, true)
                        def instance = this.class.classLoader.loadClass( countryCode+".Import", true)?.newInstance()
                        instance.importAdmin2(localAdmin2)
                    }
                    else {
                        CountryAdmin admin2 = CountryAdmin.findByCodeAndLevel(code, 2)
                        if (admin2 == null) {
                            String admin1Code = countryCode + "." + code.tokenize(".").get(1)
                            CountryAdmin admin1 = CountryAdmin.findByCodeAndLevel(admin1Code, 1)
                            if (admin1) {
                                name = name.replace("Departement ","")
                                if (name.startsWith("de ") || name.startsWith("du "))
                                    name = name.substring(3);
                                if (name.startsWith("des "))
                                    name = name.substring(4);
                                if (name.startsWith("la ") || name.startsWith("le "))
                                    name = name.substring(3);
                                if (name.startsWith("l'") || name.startsWith("d'"))
                                    name = name.substring(2);

                                admin2 = new CountryAdmin(code:code, name:name, level:2, country:admin1.country, parent:admin1)
                                admin2.save(flush:true)
                                log.info("CountryService.importAdmin2: $lineCount Adding $code $name")
                            }
                            else {
                                log.error("CountryService.importAdmin2: $lineCount Not Added $code $name (Admin1 $admin1Code not found)")
                            }
                        }
                        else {
                            log.info("CountryService.importAdmin2: $lineCount Not added $code $name (already exist)")
                        }
                    }
                }
            }
        }
        return admins2
    }

    File importCities(File countriesDir) {
        File cities = new File(countriesDir, "cities.txt")
        if(!cities.exists()){
            return null
        }
        long lineCount = 0
        Map<String, Boolean> map = [:]
        cities.eachLine() { line ->
            def field = line.tokenize("\t")
            lineCount++
            String cityCode = field[2]
            String countryCode = field[8]
            String a1code = field[10]
            String a2code = field[11]

            if (a1code.length() == 1) a1code = "0"+a1code;
            if (a2code.length() == 1) a2code = "0"+a2code;

            if (Holders.config.importCountries.codes.contains(countryCode) || Holders.config.importCountries.codes.empty) {
                File countryDir = new File(countriesDir, countryCode)
                File localCities = new File(countryDir, "cities.txt")
                if (!map.get(countryCode)) {
                    if (localCities.exists()) {
                        map.put(countryCode, true)
                        def instance = this.class.classLoader.loadClass( countryCode+".Import", true)?.newInstance()
                        instance?.importCities(localCities)
                    }
                    else {
                        CountryAdmin city = CountryAdmin.findByCodeAndLevel(cityCode, 3)
                        if (city == null) {
                            String admin2Code = countryCode + "." + a1code+ "." + a2code
                            CountryAdmin admin2 = CountryAdmin.findByCodeAndLevel(admin2Code, 2)
                            if (admin2) {
                                if (null == CountryAdmin.findByCodeAndLevel(cityCode,3)) {
                                    city = new CountryAdmin(code:cityCode, name:cityCode, level:3, country:admin2.country, parent:admin2)
                                    city.save(flush:true);
                                }
                                log.info("CountryService.importCities: $lineCount Adding $cityCode")
                            }
                            else {
                                //log.error("CountryService.importCities: $lineCount Not Added $cityCode (Admin2 $admin2Code not found)")
                            }
                        }
                        else {
                            //log.info("CountryService.importAdmin2: $lineCount Not added $cityCode (already exist)")
                        }
                    }
                }
            }
        }
        return cities
    }
}
