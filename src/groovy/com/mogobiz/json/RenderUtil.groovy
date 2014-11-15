package com.mogobiz.json

import grails.util.Holders
import org.hibernate.collection.AbstractPersistentCollection

import java.text.SimpleDateFormat

import org.apache.commons.beanutils.PropertyUtilsBean
import com.mogobiz.store.domain.Resource
import com.mogobiz.store.domain.ResourceType
import com.mogobiz.constant.IperConstant

class RenderUtil {
	public static String asMapForJSON(Calendar c) {
		if (c != null) {
			return format(c.getTime());
		}
		else {
			return null;
		}
	}
	
	public static String asMapForJSON(Calendar c, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern)
		if (c != null) {
			return sdf.format(c.getTime());
		}
		else {
			return null;
		}
	}
	
	public static String asMapForJSON(Calendar c, SimpleDateFormat sdf) {
		if (c != null) {
			return sdf.format(c.getTime());
		}
		else {
			return null;
		}
	}

	public static Map primitifAsMapForJSON(objectVO) {
		return primitifAsMapForJSON("value", objectVO)
	}
	
	public static Map primitifAsMapForJSON(String propriete, def objectVO) {
		def map = new HashMap()
		map.put(propriete, objectVO);
		return map;
	}

	/**
	 *  convert VO into map for json renderer
	 *
	 * @param object the object to render
	 * @return  map for json renderer
	 */
	public static Map voAsMapForJSON(objectVO) {
		return asMapForJSON(null, [], ["**.class", "**.methods", "**.serialVersionUID"], objectVO)
	}

	/**
	 *  convert VO into map for json renderer
	 *
	 * @param object the object to render
	 * @return  map for json renderer
	 */
	public static List listeVoAsMapForJSON(listeVO) {
		def liste = new ArrayList()
		if (listeVO != null)
		{
			for (vo in listeVO) 
			{
				liste.add(RenderUtil.voAsMapForJSON(vo))
			}
		}
		return liste;
	}

	/**
	 *  convert object into map for json renderer
	 *
	 * @param object the object to render
	 * @return  map for json renderer
	 */
	public static Map asMapForJSON(object) {
		return asMapForJSON(null, [], [], object, false, true)
	}
	
	/**
	 * convert object into map for json renderer
	 *
	 * @param included the included properties list
	 * @param object the object to render
	 * @return  map for json renderer
	 */
	public static Map asMapForJSON(included, object) {
		return asMapForJSON(null, included, [], object, false, true)
	}

    /**
     * convert object into map for json renderer using iso format for date
     *
     * @param included the included properties list
     * @param object the object to render
     * @return  map for json renderer
     */
    public static Map asIsoMapForJSON(included, object) {
        return asMapForJSON(null, included, [], object, true, false)
    }

    /**
	 * convert object into map for json renderer
	 *
	 * @param property the current property
	 * @param included the included properties list
	 * @param excluded the exclued properties list
	 * @param object the object to render
     * @param iso whether or not to use ISO format for date
	 * @return  map for json renderer
	 */
	public static Map asMapForJSON(property, included, excluded, object, boolean iso = false, boolean nullable = true) {
		Map map = null;
		if (object != null)
		{
			map = new HashMap()
			def propertyUtilsBean = PropertyUtilsBean.getInstance()
			propertyUtilsBean.describe(object).collect { key, value ->
				
				if(!(key.equals("metaClass") || key.equals("hibernateLazyInitializer"))){
					def currentProperty
					if(property == null){
						currentProperty = key
					}
					else{
						currentProperty = property+'.'+key
					}
					
					if(included == null || included.size() == 0 || included.contains(currentProperty)){
						if(excluded == null || excluded.size() == 0 || 
							(!excluded.contains(currentProperty) && !excluded.contains("**." + key))){
							if(value instanceof AbstractPersistentCollection) {
								def valueList  = new ArrayList()
								value.each { val ->
									valueList.add (asMapForJSON(currentProperty, included, excluded, val))
								}
								map.put(key,valueList)
							}else if(value == null || value instanceof java.lang.Enum || isPrimitiveType(value.class)) {
                                if(value == null && !nullable && String.class.equals(propertyUtilsBean.getPropertyDescriptor(object, key).propertyType)){
                                    map.put(key, "")
                                }
                                else{
                                    map.put(key, value)
                                }
							}else if(value!= null && value.class.isArray()) {
								def valueList  = new ArrayList()
								value.each { val ->
									valueList.add (asMapForJSON(currentProperty, included, excluded, val))
								}
								map.put(key,valueList)					
							}
							else if(value instanceof Calendar ) {
								map.put(key, iso ? formatToIso8601(value.getTime()) : format(value.getTime()))
							}
							else if(value instanceof Date ) {
								map.put(key, iso ? formatToIso8601(value) : format(value))
							}
							else if (value!= null){
								def map2 = asMapForJSON(currentProperty, included, excluded, value)
								map.put(key,map2)
							}
						}
					}
				}
			}
		}
		return map
	}
	
	/**
	 * test if the object type is primitive
	 * @param type
	 * @return
	 */
	public static boolean isPrimitiveType(Class<?> type) {
		if (type.isArray()) {
			return isPrimitiveType(type.getComponentType());
		}
		else {
			return Boolean.class.equals(type) ||
			Integer.class.equals(type) ||
			Character.class.equals(type) ||
			Byte.class.equals(type) ||
			Short.class.equals(type) ||
			Double.class.equals(type) ||
			Long.class.equals(type) ||
			Float.class.equals(type) ||
			String.class.equals(type)
		}
	}
	
	public static String extractResourceUrl(Resource resource) {
		String url = resource?.url;
		if (resource?.uploaded) {
			url = '/resource/display/'+resource.id
		}
		return completerUrl(url);
	}
	
	public static String completerUrl(String url) {
		if (url != null && !url.startsWith("http://")) {
			url = Holders.config.grails.serverURL + url;
		}
		return url;
	}
	
	public static String extractResourceSmallPicture(Resource resource) {
		def smallPicture = null
		if(ResourceType.PICTURE.equals (resource?.xtype)){
			smallPicture = resource?.smallPicture
			if (!smallPicture && resource?.uploaded) {
				smallPicture = '/resource/display/'+resource.id
			}
			smallPicture = completerUrl(smallPicture);
		}
		return smallPicture;
	}
	
	def static Calendar translateDateTimeToCalendar(def param, def pattern){
		def c = null
		def date = translateDateTimeToDate(param, pattern)
		if(date){
			c = Calendar.getInstance()
			c.setTime (date)
		}
		return c
	}
	
	def static Date translateDateTimeToDate(def param, def pattern){
		def date = null
		if(param){
			date = new SimpleDateFormat(pattern).parse (param)
		}
		return date
	}

    def static String format(Date d, String format = IperConstant.DATE_FORMAT){
        new SimpleDateFormat(format).format(d)
    }

    def static String formatToIso8601(Date d){
        new SimpleDateFormat('yyyy-MM-dd\'T\'HH:mm:ss\'Z\'').format(d)
    }

    def static Date parseFromIso8601(String s){
        new SimpleDateFormat('yyyy-MM-dd\'T\'HH:mm:ss\'Z\'').parse(s)
    }
}
