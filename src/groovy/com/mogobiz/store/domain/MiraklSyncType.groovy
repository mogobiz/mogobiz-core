/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum MiraklSyncType
{
CATEGORIES("CATEGORIES"), HIERARCHIES("HIERARCHIES"), VALUES("VALUES"), ATTRIBUTES("ATTRIBUTES"), PRODCUCTS("PRODCUCTS"), OFFERS("OFFERS")
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