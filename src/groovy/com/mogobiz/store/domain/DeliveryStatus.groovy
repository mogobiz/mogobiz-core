/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum DeliveryStatus
{
NOT_STARTED("NOT_STARTED"), IN_PROGRESS("IN_PROGRESS"), DELIVERED("DELIVERED"), RETURNED("RETURNED"), CANCELED("CANCELED")
    private java.lang.String value
    private DeliveryStatus(java.lang.String value){
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