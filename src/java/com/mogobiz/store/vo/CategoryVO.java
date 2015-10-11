/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: ValueObject.vsl in andromda-java-cartridge.
//
package com.mogobiz.store.vo;

/**
 * 
 */
public class CategoryVO
    implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5967971007033862444L;

    public CategoryVO()
    {
        this.code = null;
        this.libelle = null;
    }

    public CategoryVO(java.lang.String code, java.lang.String libelle)
    {
        this.code = code;
        this.libelle = libelle;
    }

    /**
     * Copies constructor from other CategoryVO
     *
     * @param otherBean, cannot be <code>null</code>
     * @throws java.lang.NullPointerException if the argument is <code>null</code>
     */
    public CategoryVO(CategoryVO otherBean)
    {
        this(otherBean.getCode(), otherBean.getLibelle());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(CategoryVO otherBean)
    {
        if (otherBean != null)
        {
            this.setCode(otherBean.getCode());
            this.setLibelle(otherBean.getLibelle());
        }
    }

    private java.lang.String code;

    /**
     * 
     */
    public java.lang.String getCode()
    {
        return this.code;
    }

    public void setCode(java.lang.String code)
    {
        this.code = code;
    }

    private java.lang.String libelle;

    /**
     * 
     */
    public java.lang.String getLibelle()
    {
        return this.libelle;
    }

    public void setLibelle(java.lang.String libelle)
    {
        this.libelle = libelle;
    }

}