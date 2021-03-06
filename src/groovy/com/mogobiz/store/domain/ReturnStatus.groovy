/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ReturnStatus
{
RETURN_SUBMITTED("RETURN_SUBMITTED"), RETURN_TO_BE_RECEIVED("RETURN_TO_BE_RECEIVED"), RETURN_RECEIVED("RETURN_RECEIVED"), RETURN_REFUSED("RETURN_REFUSED"), RETURN_ACCEPTED("RETURN_ACCEPTED")
    private java.lang.String value
    private ReturnStatus(java.lang.String value){
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