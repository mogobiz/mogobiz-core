/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum MiraklSyncType
{
CATEGORIES("CATEGORIES"), HIERARCHIES("HIERARCHIES"), VALUES("VALUES"), ATTRIBUTES("ATTRIBUTES"), PRODUCTS("PRODUCTS"), OFFERS("OFFERS"), PRODUCTS_SYNCHRO("PRODUCTS_SYNCHRO")
    private java.lang.String value
    private MiraklSyncType(java.lang.String value){
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