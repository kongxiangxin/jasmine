package org.yidan.idea.plugin.jasmine.mapping;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kongxiangxin on 2017/8/2.
 */
public class TypeMapping {


    private static Map<JDBCType, String> typeMap = new HashMap<>();
    static {
        typeMap.put(JDBCType.BIT, "boolean");
        typeMap.put(JDBCType.BOOLEAN, "boolean");

        typeMap.put(JDBCType.TINYINT, "Short");
        typeMap.put(JDBCType.SMALLINT, "Short");
        typeMap.put(JDBCType.INTEGER, "Integer");
        typeMap.put(JDBCType.BIGINT, "Long");

        typeMap.put(JDBCType.FLOAT, "Float");
        typeMap.put(JDBCType.REAL, "Double");
        typeMap.put(JDBCType.DOUBLE, "Double");

        typeMap.put(JDBCType.FLOAT, "Float");
        typeMap.put(JDBCType.FLOAT, "Float");
        typeMap.put(JDBCType.FLOAT, "Float");

        typeMap.put(JDBCType.NUMERIC, "java.math.BigDecimal");
        typeMap.put(JDBCType.DECIMAL, "java.math.BigDecimal");

        typeMap.put(JDBCType.CHAR, "String");
        typeMap.put(JDBCType.VARCHAR, "String");
        typeMap.put(JDBCType.LONGVARCHAR, "String");
        typeMap.put(JDBCType.NVARCHAR, "String");
        typeMap.put(JDBCType.LONGNVARCHAR, "String");

        typeMap.put(JDBCType.DATE, "java.util.Date");
        typeMap.put(JDBCType.TIME, "java.util.Date");
        typeMap.put(JDBCType.TIMESTAMP, "java.util.Date");
        typeMap.put(JDBCType.TIME_WITH_TIMEZONE, "java.util.Date");
        typeMap.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, "java.util.Date");

        typeMap.put(JDBCType.BINARY, "Byte[]");
        typeMap.put(JDBCType.VARBINARY, "Byte[]");
        typeMap.put(JDBCType.LONGVARBINARY, "Byte[]");

        typeMap.put(JDBCType.CLOB, "java.sql.Clob");
        typeMap.put(JDBCType.BLOB, "java.sql.Blob");
        typeMap.put(JDBCType.ARRAY, "java.sql.Array");
    }
    public static String dbToJavaType(JDBCType dbType){
        return typeMap.getOrDefault(dbType, "String");
    }
}
