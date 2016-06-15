/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum MiraklSyncStatus
{
WAITING("WAITING"), RUNNING("RUNNING"), COMPLETE("COMPLETE"), FAILED("FAILED"), CANCELLED("CANCELLED"), QUEUED("QUEUED")
    private java.lang.String value
    private MiraklSyncStatus(java.lang.String value){
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