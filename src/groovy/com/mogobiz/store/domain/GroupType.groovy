/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum GroupType
{
ALL("ALL"), FRIENDS("FRIENDS"), FRIENDS_OF_FRIENDS("FRIENDS_OF_FRIENDS"), GROUP("GROUP")
    private java.lang.String value
    private GroupType(java.lang.String value){
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