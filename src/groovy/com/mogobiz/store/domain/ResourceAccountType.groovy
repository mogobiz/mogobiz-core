/*
* Copyright (C) 2015 Mogobiz SARL. All rights reserved.
*/

package com.mogobiz.store.domain;
/**
 * 
 */
enum ResourceAccountType
{
STANDARD("STANDARD"), FACEBOOK("FACEBOOK"), PICASA("PICASA"), FLICKR("FLICKR"), YOUTUBE("YOUTUBE"), DAILYMOTION("DAILYMOTION"), VIMEO("VIMEO")
    private java.lang.String value
    private ResourceAccountType(java.lang.String value){
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