/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum AccountType
{
FACEBOOK("FACEBOOK"), TWITTER("TWITTER"), GOOGLE("GOOGLE"), YAHOO("YAHOO"), STANDARD("STANDARD")
    private java.lang.String value
    private AccountType(java.lang.String value){
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