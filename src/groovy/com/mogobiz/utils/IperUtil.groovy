package com.mogobiz.utils

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import grails.util.Holders.*
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.NumberFormat
import java.text.ParseException;
import java.text.SimpleDateFormat

import com.mogobiz.store.domain.DatePeriod
import com.mogobiz.store.domain.IntraDayPeriod
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ProductCalendar
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.TicketType
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

    public static capitalize(String s) {
        if (s == null || s.length() == 0) {
            return s;
        } else {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    public static parseDateFromParam(String stringDate) throws Exception {
        return parseDateFromParamWithFormat(stringDate, IperConstant.DATE_FORMAT_WITHOUT_HOUR);
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


    public static Map getResourceVOSimple(Resource r) {
        def mapPicture = [:]
        mapPicture['id'] = r.id
        mapPicture['url'] = RenderUtil.extractResourceUrl(r)
        return mapPicture
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
    public static String normalizeName(String companyName) {
        return Normalizer.normalize(companyName, Normalizer.Form.NFD)
                .replaceAll("\\s", "-").replaceAll("\\p{IsM}+", "").replaceAll("[^a-zA-Z0-9-]", "");
    }

    public static def withAutoTimestampSuppression(entity, closure) {
        toggleAutoTimestamp(entity, false)
        def result = closure()
        toggleAutoTimestamp(entity, true)
        result
    }

    private static def toggleAutoTimestamp(target, enabled) {
        def applicationContext = (ServletContextHolder.getServletContext()
                .getAttribute(ApplicationAttributes.APPLICATION_CONTEXT))

        def closureInterceptor = applicationContext.getBean("eventTriggeringInterceptor")
        def datastore = closureInterceptor.datastores.values().iterator().next()
        def interceptor = datastore.getEventTriggeringInterceptor()

        def listener = interceptor.findEventListener(target)
        listener.shouldTimestamp = enabled
        null
    }


}
