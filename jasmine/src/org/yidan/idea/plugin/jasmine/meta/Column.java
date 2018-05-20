package org.yidan.idea.plugin.jasmine.meta;

import org.yidan.idea.plugin.jasmine.mapping.NamingStrategy;

import java.sql.JDBCType;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class Column {
    private String type;
    private JDBCType jdbcType;
    private String javaType;
    private String name;
    private String fieldName;
    private String propertyName;


    public Column(String name, String type, JDBCType jdbcType, String javaType) {
        this.name = name;
        this.type = type;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
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
}
