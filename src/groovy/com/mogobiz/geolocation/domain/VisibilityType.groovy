/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.geolocation.domain;
/**
 * 
 */
enum VisibilityType
{
LEVEL_1("LEVEL_1"), LEVEL_2("LEVEL_2"), LEVEL_3("LEVEL_3"), LEVEL_4("LEVEL_4")
    private java.lang.String value
    private VisibilityType(java.lang.String value){
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