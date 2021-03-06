/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum TransactionStatus
{
PENDING("PENDING"), PAYMENT_NOT_INITIATED("PAYMENT_NOT_INITIATED"), FAILED("FAILED"), COMPLETE("COMPLETE")
    private java.lang.String value
    private TransactionStatus(java.lang.String value){
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