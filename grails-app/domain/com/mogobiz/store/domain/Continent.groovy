// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEnumeration.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;
/**
 * 
 */
enum Continent
{
AMERICA("America"), AFRICA("Africa"), ASIA("Asia"), EUROPE("Europe"), OCEANIA("Oceania")
    private java.lang.String value
    private Continent(java.lang.String value){
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