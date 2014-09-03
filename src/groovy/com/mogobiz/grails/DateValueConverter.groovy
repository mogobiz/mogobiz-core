package com.mogobiz.grails

import grails.util.Holders;

import java.text.ParseException;
import java.text.SimpleDateFormat

import org.grails.databinding.converters.ValueConverter;

class DateValueConverter implements ValueConverter {

	private String expectedFormats;
	private final List<String> formats;

	public DateValueConverter()
	{
		def formats = Holders.config.grails.date.formats
		expectedFormats = "";
		List<String> formatList = new ArrayList<String>(formats.size());
		for (Object format : formats) {
			formatList.add(format.toString()); // Force String values (eg. for GStrings)
			expectedFormats += ((expectedFormats.length() > 0) ? ", " : "") + format.toString()
		}
		this.formats = Collections.unmodifiableList(formatList);
	}
	
	boolean canConvert(value) {
		value == null || (value instanceof String)
	}

	def convert(value) {
		if (value == null)
		{
			return null;
		}
		String text = (String)value
		if (text.trim().length() > 0)
		{
			for (String format : formats)
			{
				try
				{
					SimpleDateFormat dateFormat = new SimpleDateFormat(format);
					return dateFormat.parse(text);
				}
				catch (ParseException ex)
				{
				}
			}
			throw new IllegalArgumentException("Could not parse date " + text + ": expected format = " + expectedFormats);
		}
		return null;
	}

	Class<?> getTargetType() { 
		Date
	} 
}
