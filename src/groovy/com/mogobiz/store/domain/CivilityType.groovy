/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum CivilityType
{
MR("Monsieur"), MRS("Madame"), MISS("Mademoiselle")
    private java.lang.String value
    private CivilityType(java.lang.String value){
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