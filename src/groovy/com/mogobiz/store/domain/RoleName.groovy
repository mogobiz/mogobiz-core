/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum RoleName
{
ANONYMOUS("Anonymous"), CLIENT("Client"), PARTNER("Partner"), ADMINISTRATOR("Administrator"), VALIDATOR("Validator"), SALESAGENT("SalesAgent")
    private java.lang.String value
    private RoleName(java.lang.String value){
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