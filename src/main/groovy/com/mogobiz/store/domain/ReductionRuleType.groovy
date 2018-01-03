/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ReductionRuleType
{
DISCOUNT("DISCOUNT"), X_PURCHASED_Y_OFFERED("X_PURCHASED_Y_OFFERED"), CUSTOM("CUSTOM")
    private java.lang.String value
    private ReductionRuleType(java.lang.String value){
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