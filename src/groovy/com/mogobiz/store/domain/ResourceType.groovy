/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ResourceType
{
PICTURE("PICTURE"), VIDEO("VIDEO"), AUDIO("AUDIO"), TEXT("TEXT")
    private java.lang.String value
    private ResourceType(java.lang.String value){
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