/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum RefundPolicy
{
MONEYBACK("Money back"), EXCHANGE("Exchange"), NO_REFUND("No refund")
    private java.lang.String value
    private RefundPolicy(java.lang.String value){
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