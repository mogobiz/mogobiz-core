/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: GrailsEntity.vsl in andromda-grails-cartridge.
//
package com.mogobiz.store.domain;
/**
 * 
 */
class ShippingCarriers implements java.io.Serializable
{
// If super inheritance is 'interface', render super properties etc
// render the properties and associations (if any)

    /**
     * 
     */
    boolean ups 
    /**
     * 
     */
    boolean fedex // END Entity 




    static mapping = {

        autoTimestamp true

    uuid column:"uuid",insertable:true,updateable:false,lazy:false,cache:false



        cache true

        version false



    }

    static constraints = {
        ups ( blank:false, nullable:false, unique:false)
        fedex ( blank:false, nullable:false, unique:false)
    }

    // GrailsEntity.vsl merge-point
}