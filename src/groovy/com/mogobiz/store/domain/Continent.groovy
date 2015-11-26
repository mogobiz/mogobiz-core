/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum Continent
{
AMERICA("America"), AFRICA("Africa"), ASIA("Asia"), EUROPE("Europe"), OCEANIA("Oceania")
    private java.lang.String value
    private Continent(java.lang.String value){
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