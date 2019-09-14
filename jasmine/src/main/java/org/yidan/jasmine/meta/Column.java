package org.yidan.jasmine.meta;

import org.yidan.jasmine.mapping.NamingStrategy;

import java.sql.JDBCType;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class Column {
    /**
     * jdbc TYPE_NAME
     */
    private String type;

    private JDBCType jdbcType;
    /**
     * pojo type
     */
    private String javaType;
    /**
     * jdbc COLUMN_NAME
     */
    private String name;

    /**
     * pojo field name
     */
    private String fieldName;

    /**
     * pojo propertyName
     */
    private String propertyName;

    /**
     * 字段注释
     */
    private String remarks;

    /**
     * 是否主键
     */
    private boolean isPK;


    public Column(String name, String type, JDBCType jdbcType, String javaType, String remarks) {
        this.name = name;
        this.type = type;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
        this.remarks = remarks;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getFieldName() {
        if(fieldName == null){
            fieldName = NamingStrategy.columnToFieldName(name);
        }
        return fieldName;
    }

    public String getPropertyName() {
        if(propertyName == null){
            propertyName = NamingStrategy.columnToPropertyName(name);
        }
        return propertyName;
    }

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(JDBCType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isPK() {
        return isPK;
    }

    public void setPK(boolean PK) {
        isPK = PK;
    }
}
