/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ProductCalendar
{
NO_DATE("NO_DATE"), DATE_ONLY("DATE_ONLY"), DATE_TIME("DATE_TIME")
    private java.lang.String value
    private ProductCalendar(java.lang.String value){
        this.value = value
    }

	String getKey(){
		name()
	}

    /**
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return java.lang.String.valueOf(value)
    }
}