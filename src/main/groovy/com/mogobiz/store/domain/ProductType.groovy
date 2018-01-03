/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ProductType
{
SERVICE("SERVICE"), PRODUCT("PRODUCT"), DOWNLOADABLE("DOWNLOADABLE"), OTHER("OTHER"), PACKAGE("PACKAGE")
    private java.lang.String value
    private ProductType(java.lang.String value){
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