/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum EventType
{
CREATE("CREATE"), MODIFY("MODIFY"), DELETE("DELETE"), PUBLISH("PUBLISH")
    private java.lang.String value
    private EventType(java.lang.String value){
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