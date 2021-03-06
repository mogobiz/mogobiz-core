/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.hibernate;

import java.sql.Types;

/**
 */
public class OracleDialect extends org.hibernate.dialect.Oracle10gDialect {
    public OracleDialect() {
        super();
        registerColumnType( Types.VARBINARY, 2000, "raw($l)" );
        registerColumnType( Types.VARBINARY, "blob" );
        registerColumnType( Types.LONGVARCHAR, "clob" );
        registerColumnType( Types.LONGVARBINARY, "blob" );
        registerColumnType( Types.VARCHAR, 4000, "varchar2($l char)" );
        registerColumnType( Types.VARCHAR, "clob" );
    }
}
