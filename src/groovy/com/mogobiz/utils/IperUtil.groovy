package com.mogobiz.utils

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException;
import java.text.SimpleDateFormat

import com.mogobiz.store.domain.DatePeriod
import com.mogobiz.store.domain.Event
import com.mogobiz.store.domain.IntraDayPeriod
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.domain.User
import com.mogobiz.constant.IperConstant
import com.mogobiz.geolocation.domain.Poi
import com.mogobiz.geolocation.domain.PoiType
import com.mogobiz.geolocation.domain.VisibilityType
import com.mogobiz.json.RenderUtil
import com.restfb.types.Post

/**
 * classe utilitaire
 *
 * @author Hayssam Saleh
 *
 */
class IperUtil {
    private static final NumberFormat DEFAULT_DECIMAL_FORMAT;

    static {
        DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
    }

    public static String escapeQuote(String s) {
        s = s.replaceAll("\"", "\\\\\"");
        s = s.replaceAll("'", "\\\\'");
        return s;
    }

    public static capitalize(String s) {
        if (s == null || s.length() == 0) {
            return s;
        } else {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    public static def getInfosForPoiMarker(Poi poi, langue) {
        def infos = [:];
        infos["type"] = poi.pictureType;
        infos["icon"] = Holders.config.grails.serverURL + "/images/markers/" + poi.pictureType + "/" + poi.picture;
        infos["libelleType"] = poi.pictureType;

        return infos;
    }

    public static String genererNomVariableUnique(String valeurParDefaut, String prefixe) {
        if (valeurParDefaut != null) {
            return valeurParDefaut;
        } else {
            genererNomVariableUnique(prefixe);
        }
    }

    public static String genererNomVariableUnique(String prefixe) {
        return prefixe + "_" + (new Date()).getTime();
    }

    public static parseDateFromParam(String stringDate) throws Exception {
        return parseDateFromParamWithFormat(stringDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR);
    }

    public static parseDateTimeFromParam(String stringDate) throws Exception {
        return parseDateFromParamWithFormat(stringDate, IperConstant.DATE_FORMAT);
    }

    public static parseDateFromParamWithFormat(String stringDate, String format) throws Exception {
        if (stringDate != null && stringDate.length() > 0) {
            DateFormat formatter = new SimpleDateFormat(format)
            Date date = (Date) formatter.parse(stringDate)
            Calendar cal = Calendar.getInstance()
            cal.setTime(date)
            return cal
        } else {
            return null;
        }
    }

    /**
     * save or update a poi
     * @param params the request params
     * @return the new poi or the updated poi
     */
    public static Poi saveOrUpdatePoi(params) {
        def poi
        def poiId = params['poi']?.id
        if (poiId) {
            poi = Poi.get(poiId)
        } else {
            poi = new Poi()
        }

        poi.countryCode = params['poi']?.country?.code;
        poi.latitude = params['poi']?.latitude?.toDouble()
        poi.longitude = params['poi']?.longitude?.toDouble()
        poi.city = params['poi']?.city
        poi.road1 = params['poi']?.road1
        poi.road2 = params['poi']?.road2
        poi.road3 = params['poi']?.road3
        poi.roadNum = params['poi']?.roadNum
        poi.postalCode = params['poi']?.postalCode
        poi.description = params['poi']?.description
        poi.name = params['poi']?.name
        poi.video = params['poi']?.video
        poi.pictureType = params['poi']?.pictureType
        poi.picture = params['poi']?.picture
        poi.isMain = (params['poi']?.isMain ? true : false);

        //		if (poi.isMain) {
        //			poi.pictureType ="tourism";
        //			poi.picture = "passnguide.png"
        //		}

        if (params['poi']?.visibility) {
            poi.visibility = VisibilityType.valueOf(params['poi']?.visibility)
        }
        def codePoiType = params['poiType']?.code
        if (codePoiType) {
            def poiType = PoiType.findByCode(codePoiType)
            if (poiType) {
                poi.poiType = poiType
            } else {
                poiType = new PoiType()
                poiType.xtype = params['poiType']?.xtype
                poiType.code = params['poiType']?.code
                poiType.icon = params['poiType']?.icon
                if (poiType.validate()) {
                    poiType.save(flush: true)
                    poi.poiType = poiType
                } else {
                    poi.errors = poiType.errors
                }
            }
        }
        poi.name = params['poi']?.name

        if (poi.validate()) {
            poi.save(flush: true)
            //associer le poi au produit dont l'id est pass� en params
            def productId = params['product']?.id
            if (productId) {
                def product = Product.get(productId)
                if (product) {
                    def productPoi = product.poi
                    if (poi.isMain) {
                        product.poi = poi
                        if (productPoi && productPoi.id != poi.id) {
                            productPoi.isMain = false;
                            productPoi.save(flush: true)
                        }
                    }
                    product.save(flush: true)
                }
            }
        }
        return poi
    }

    /**
     * cr�er l'evenement apres creer or modifier un Twitable
     * @param seller the seller
     * @param twitable the twitable object
     * @param eventType the event type
     * @return the created event
     */
    public static Event saveEvent(User user, Object twitable, eventType) {
        def event = new Event()
        if (twitable instanceof Resource) {
            event.resource = twitable
        } else if (twitable instanceof Product) {
            event.product = twitable
        }
        event.date = Calendar.getInstance()
        event.user = user
        event.xtype = eventType
        event.date = Calendar.getInstance()
        if (event.validate()) {
            event.save(flush: true)
        }
        return event
    }

    public static long computeDiscount(String regle, long prixDeBase) {
        long nouveauPrix = prixDeBase;
        if (!StringUtils.isEmpty(regle)) {
            if (regle.endsWith('%')) {
                float pourcentage = Float.parseFloat(regle.substring(0, regle.length() - 1))
                nouveauPrix = prixDeBase * pourcentage / 100;
            } else if (regle.startsWith('+')) {

                long increment = Long.parseLong(regle.substring(1))
                nouveauPrix = prixDeBase + increment;
            } else if (regle.startsWith('-')) {
                long decrement = Long.parseLong(regle.substring(1))
                nouveauPrix = prixDeBase - decrement;
            } else {
                nouveauPrix = Long.parseLong(regle)
            }
        }
        return nouveauPrix;
    }

    public static Number parseAmount(String amount) {
        return DEFAULT_DECIMAL_FORMAT.parse(amount)
    }

    /**
     * @param amount1
     *            - montant 1
     * @param amount2
     *            - montant 2
     * @return amount1 + amount2
     */
    public static BigDecimal addAmount(float amount1, float amount2) {
        String bdString1 = DEFAULT_DECIMAL_FORMAT.format(amount1);
        String bdString2 = DEFAULT_DECIMAL_FORMAT.format(amount2);
        BigDecimal bd1 = new BigDecimal(bdString1);
        BigDecimal bd2 = new BigDecimal(bdString2);
        bd1 = bd1.add(bd2);
        return bd1;
    }

    /**
     * @param amount1
     *            - montant 1
     * @param amount2
     *            - montant 2
     * @return amount1 + amount2
     */
    public static float addAmountAsFloat(float amount1, float amount2) {
        return addAmount(amount1, amount2).floatValue();
    }

    /**
     * @param amount
     *            - montant unitaire
     * @param quantity
     *            - quantite
     * @return montant*quantite
     */
    public static BigDecimal computeAmount(float amount, long quantity) {
        String bdString = DEFAULT_DECIMAL_FORMAT.format(amount);
        BigDecimal bd = new BigDecimal(bdString);
        bd = bd.multiply(new BigDecimal(quantity));
        bd = bd.multiply(new BigDecimal('100'));
        return bd;
    }

    /**
     * @param amount
     *            - montant unitaire
     * @param quantity
     *            - quantite
     * @return montant*quantite
     */
    public static long computeAmountAsLong(float amount, long quantity) {
        return computeAmount(amount, quantity).longValue()
    }

    public static Map getResourceVOSimple(Resource r) {
        def mapPicture = [:]
        mapPicture['id'] = r.id
        mapPicture['url'] = RenderUtil.extractResourceUrl(r)
        return mapPicture
    }

    public static Map getProductVO(Product p) {
        def map = getProductVOSimple(p)

        def tabPictures = [];
        p.getPictures().each { Resource r ->
            tabPictures << getResourceVOSimple(r);
        }
        map.put("pictures", tabPictures)

        return map;
    }

    public static Map getProductDetailVO(Product p, Post[] posts) {
        def map = getProductVO(p);

        if (posts) {
            def tabPosts = []
            posts.each {
                def mapPosts = [:]
                def idPost = it.id
                mapPosts.put("id", idPost)
                mapPosts.put("idProduct", p.id)
                mapPosts.put("from", it.from?.name)
                mapPosts.put("message", it.message)
                mapPosts.put("date", it.createdTime)
                mapPosts.put("likesCount", it.likesCount)

                /*
                 def tabLikes = []
                 if (it.likes) {
                 it.likes.data.each {like ->
                 def mapLikes = [:]
                 mapLikes.put("idPost", idPost)
                 mapLikes.put("idProduct", p.id)
                 mapLikes.put("idUser", like.id)
                 mapLikes.put("from", like.name)
                 tabLikes << mapLikes
                 }
                 }
                 mapPosts.put("likes", tabLikes)
                 */
                tabPosts << mapPosts
            }

            map.put("posts", tabPosts);
        }
        return map;
    }

    /**
     * This method create a ListePagine with the parameters
     * @param liste
     * @param totalCount
     * @param maxItemsPerPage
     * @param pageOffset
     * @return
     */
    public static Page createListePagine(List liste, long totalCount, long maxItemsPerPage, long pageOffset) {
        Page myList = new Page()
        myList.list = liste
        myList.pageSize = myList.list.size()
        myList.totalCount = totalCount
        myList.maxItemsPerPage = maxItemsPerPage
        myList.pageOffset = pageOffset
        myList.pageCount = (int) (myList.totalCount / maxItemsPerPage) + ((myList.totalCount % maxItemsPerPage) > 0 ? 1 : 0)
        myList.hasPrevious = (pageOffset > 0)
        myList.hasNext = (pageOffset != (myList.pageCount - 1))
        return myList
    }

    /**
     * Reset time of the given calendar and returns it
     * @param calendar
     * @return
     */
    public static Calendar resetCalendarTime(Calendar calendar) {
        if (calendar) {
            calendar.set(Calendar.AM_PM, 0)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
        return calendar
    }

    /**
     * Returns current date without time
     * @return
     */
    public static Calendar today() {
        def today = Calendar.getInstance()
        today = resetCalendarTime(today)
        return today
    }

    /**
     * Format the given date with the given format
     * @param date
     * @param format
     * @return
     */
    public static String formatCalendar(Calendar date, String format = IperConstant.DATE_FORMAT) {
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.format(date.getTime());
        }
        return null;
    }

    /**
     * Parse the given date with the given format and return a Calendar.
     * If "resetTime" is true, this method reset calendar time before returns calendar
     * @param date
     * @param format
     * @return
     */
    public static Calendar parseCalendar(String date, String format, boolean resetTime = true) throws ParseException {
        if (date != null && date.length() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parseDate(date, format))
            if (resetTime) {
                calendar = resetCalendarTime(calendar)
            }
            return calendar
        }
        return null;
    }

    /**
     * Parse the given string date with the given format and return a Date
     * @param date
     * @return
     */
    public
    static Date parseDate(String date, String format = IperConstant.DATE_FORMAT_WITHOUT_HOUR) throws ParseException {
        if (date != null && date.length() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.parse(date);
        }
        return null;
    }

    public static Calendar getStartPeriodeDate(Product product, Calendar date) {
        def result

        switch (product.calendarType) {
            case ProductCalendar.NO_DATE:
                break
            case ProductCalendar.DATE_ONLY:
                result = IperUtil.resetCalendarTime(date)
                break
            case ProductCalendar.DATE_TIME:
                def listIncluded = IntraDayPeriod.createCriteria().list {
                    eq('product', product)
                    le('startDate', date)
                    ge('endDate', date)
                }
                for (item in listIncluded) {
                    if (item.startDate.get(Calendar.HOUR) == date.get(Calendar.HOUR) &&
                            item.startDate.get(Calendar.MINUTE) == date.get(Calendar.MINUTE)) {
                        result = item.startDate
                    }
                    if (result) {
                        break
                    }
                }
                break
        }
        return result
    }

    /**
     * Cette méthode permet de récupérer la date de début et de fin d'utilisation d'un ticket à partir de la date
     * choisie par le client et de la configuration du produit et du TicketType.
     * Si la date n'est pas valide, la méthode renvoie null sinon elle renvoie un tableau de 2 dates, la première étant
     * la date de début et la seconde étant la date de fin
     * @param product
     * @param ticketType : Facultatif (si non fourni, la date n'est pas controllée au niveau du ticketType)
     * @param date
     * @return
     */
    public static Calendar[] verifyAndExtractStartEndDate(TicketType ticketType, Calendar date) {
        Product product = ticketType.product;
        // Si le ticketType est renseigné, la date doit être valide par rapport au dates du ticketType
        boolean dateValidForTicketType = false;
        if (ticketType && date) {
            dateValidForTicketType = (date.compareTo(ticketType.startDate) >= 0 && date.compareTo(ticketType.stopDate) <= 0)
        }

        // On controle maintenant la date avec le calendrier du produit
        switch (product.calendarType) {
            case ProductCalendar.NO_DATE:
                // Pas de calendrier donc pas de date
                return [null, null];
            case ProductCalendar.DATE_ONLY:
                if (dateValidForTicketType) {
                    // Calendrier jour seulement, la date doit être comprise entres les dates du produits
                    Calendar startDate = IperUtil.resetCalendarTime(date);
                    if (startDate.compareTo(product.startDate) < 0 || startDate.compareTo(product.stopDate) > 0) {
                        return null;
                    }
                    return [startDate, startDate];
                }
                break;
            case ProductCalendar.DATE_TIME:
                if (dateValidForTicketType) {
                    def listIncluded = IntraDayPeriod.createCriteria().list {
                        eq('product', product)
                        le('startDate', date)
                        ge('endDate', date)
                    }
                    for (IntraDayPeriod intraDayPeriod in listIncluded) {
                        // On vérifie que l'heure demandé correspond à la place horaire du calendrier
                        String patternComparaisonHeure = "HHmm";
                        if (DateUtilitaire.isBeforeOrEqual(intraDayPeriod.startDate, date, patternComparaisonHeure)
                                && DateUtilitaire.isAfterOrEqual(intraDayPeriod.endDate, date, patternComparaisonHeure)) {
                            Calendar endDate = DateUtilitaire.copy(intraDayPeriod.startDate);
                            endDate.set(Calendar.HOUR, intraDayPeriod.endDate.get(Calendar.HOUR))
                            endDate.set(Calendar.MINUTE, intraDayPeriod.endDate.get(Calendar.MINUTE))
                            return [
                                    intraDayPeriod.startDate,
                                    endDate
                            ]
                        }
                    }
                }
                break
        }
        return null
    }

    public static Map getFromToEvent(Product product, Calendar date) {
        def result = [:]

        switch (product.calendarType) {
            case ProductCalendar.NO_DATE:
                result.put("startDate", null)
                result.put("stopDate", null)
                break
            case ProductCalendar.DATE_ONLY:
                result.put("startDate", date)
                result.put("stopDate", date)
                break
            case ProductCalendar.DATE_TIME:
                def listIncluded = IntraDayPeriod.createCriteria().list {
                    eq('product', product)
                    le('startDate', date)
                    ge('endDate', date)
                }
                for (item in listIncluded) {
                    if (item.startDate.get(Calendar.HOUR) == date.get(Calendar.HOUR) &&
                            item.startDate.get(Calendar.MINUTE) == date.get(Calendar.MINUTE)) {
                        result.put("startDate", item.startDate)
                        result.put("stopDate", item.endDate)
                    }
                    if (result) {
                        break
                    }
                }
                break
        }
        return result
    }


    static public boolean isDateIncluded(List<IntraDayPeriod> periodesList, Calendar day) {
        def included = false
        for (item in periodesList) {
            if (day.compareTo(item.startDate) >= 0 && day.compareTo(item.endDate) <= 0) {
                def dow = day.get(Calendar.DAY_OF_WEEK)
                switch (dow) {
                    case Calendar.MONDAY: included = item.weekday1; break
                    case Calendar.TUESDAY: included = item.weekday2; break
                    case Calendar.WEDNESDAY: included = item.weekday3; break
                    case Calendar.THURSDAY: included = item.weekday4; break
                    case Calendar.FRIDAY: included = item.weekday5; break
                    case Calendar.SATURDAY: included = item.weekday6; break
                    case Calendar.SUNDAY: included = item.weekday7; break
                }
            }
            if (included == true) {
                break
            }
        }
        return included
    }

    static public boolean isDateExcluded(List<DatePeriod> periodesList, Calendar day) {
        def excluded = false
        for (item in periodesList) {
            if (day.compareTo(item.startDate) >= 0 && day.compareTo(item.endDate) <= 0) {
                excluded = true
                break
            }
        }
        return excluded
    }
    static DatabaseReader geoService

    static DatabaseReader getGeoService() {
        if (geoService == null) {
            File layoutFolder = ApplicationHolder.application.parentContext.getResource("WEB-INF/geoip").file
            File ipdbfile = new File(layoutFolder, "GeoLite2-City.mmdb")
            geoService = new DatabaseReader.Builder(ipdbfile).build()
        }
        return geoService
    }

    public static String getCountryFromIP(String ip) {
        CityResponse city = getGeoService().city(InetAddress.getByName(ip))
        String c = null
        if (city) {
            c = city.getCountry().getIsoCode()
        } else {
            c = "N/A"
        }
        return c;
    }
}
