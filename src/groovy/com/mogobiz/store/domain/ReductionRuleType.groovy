// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEnumeration.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;
/**
 * 
 */
enum ReductionRuleType
{
DISCOUNT("DISCOUNT"), X_PURCHASED_Y_OFFERED("X_PURCHASED_Y_OFFERED")
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