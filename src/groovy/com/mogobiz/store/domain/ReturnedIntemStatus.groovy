/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ReturnedIntemStatus
{
NOT_AVAILABLE("NOT_AVAILABLE"), BACK_TO_STOCK("BACK_TO_STOCK"), DISCARDED("DISCARDED"), UNDEFINED("UNDEFINED")
    private java.lang.String value
    private ReturnedIntemStatus(java.lang.String value){
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