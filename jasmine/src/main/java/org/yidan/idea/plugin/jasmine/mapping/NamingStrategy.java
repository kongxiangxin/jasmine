package org.yidan.idea.plugin.jasmine.mapping;

import io.netty.util.internal.StringUtil;

/**
 * Created by kongxiangxin on 2017/8/2.
 */
public class NamingStrategy {

    private static String clearUnderscores(String name){
        if(StringUtil.isNullOrEmpty(name)){
            return name;
        }
        String[] arr = name.split("_");
        StringBuilder sb = new StringBuilder();
        for(String a : arr){
            sb.append(a.substring(0, 1).toUpperCase());
            sb.append(a.substring(1));
        }
        return sb.toString();
    }

    public static String tableToClassName(String tableName) {
        return clearUnderscores(tableName);
    }

    public static String columnToPropertyName(String columnName) {
        return clearUnderscores(columnName);
    }

    public static String columnToFieldName(String columnName) {
        String propertyName = clearUnderscores(columnName);
        if(StringUtil.isNullOrEmpty(propertyName)){
            return propertyName;
        }
        return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
    }
}
