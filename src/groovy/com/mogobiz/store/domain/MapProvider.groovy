/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum MapProvider
{
GOOGLE_MAP("GOOGLE_MAP"), OPEN_STREET_MAP("OPEN_STREET_MAP")
    private java.lang.String value
    private MapProvider(java.lang.String value){
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