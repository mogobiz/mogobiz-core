package com.mogobiz.hibernate;

import java.sql.Types;

/**
 * Created by hayssams on 18/09/14.
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
