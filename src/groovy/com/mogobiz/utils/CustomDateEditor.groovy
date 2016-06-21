package com.mogobiz.utils

import org.apache.commons.lang.time.DateUtils

import java.beans.PropertyEditorSupport

public class CustomDateEditor extends PropertyEditorSupport {
    boolean allowEmpty
    String[] formats

    /**
     * Parse the Date from the given text
     */
    void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !text) {
            // Treat empty String as null value.
            setValue(null)
        }
        else {
            try {
                setValue(DateUtils.parseDate(text, formats))
            }
            catch (Throwable ex) {
                throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex)
            }
        }
    }

    /**
     * Format the Date as String, using the first specified format
     */
    String getAsText() {
        def val = getValue()
        val?.respondsTo('format') ? val.format(formats[0]) : ''
    }
}
