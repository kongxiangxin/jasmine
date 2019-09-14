package org.yidan.jasmine.settings;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class GenerateSetting {
    private JdbcProperty jdbcProperty;

    private String tablePrefix;
    private boolean truncatePrefix;
    private String tableName;
    private Set<String> excludeTableSet = new HashSet<>();
    private String excludeTables;
    private String excludeColumns;
    private Set<String> excludeColumnSet = new HashSet<>();

    private Map<String, String> typeMapping = new HashMap<>();

    /**
     * 如果我们把某个大表的数据，分割到多张表中，可利用tableNameMapping和classNameMapping
     * 例如，订单数据量太大，根据订单号hash到10张表里，表名可能是这个样子：order_01、order_02、...、order_10，这10张表的结构完全相同。
     * 我们可以指定tableName，只利用其中一张表作为模型来生成代码，但是如果在模板中不特殊处理的话，生成的实体类名是Order01，生成的sql中的表名是order_01。这显然不是我们想要的结果。
     */
    private Map<String, String> tableNameMapping = new HashMap<>();
    private Map<String, String> classNameMapping = new HashMap<>();

    private Map<String, String> properties = new HashMap<>();

    private String fileEncoding;

    public static GenerateSetting getInstance(Properties properties){
        if(properties == null){
            return null;
        }

        GenerateSetting setting = new GenerateSetting();
        setting.jdbcProperty = JdbcProperty.getInstance(properties);
        setting.tablePrefix = properties.getProperty("tablePrefix");
        setting.truncatePrefix = "true".equals(properties.getProperty("truncatePrefix"));
        setting.tableName = properties.getProperty("tableName");
        setting.excludeTables = properties.getProperty("excludeTables");
        setting.excludeColumns = properties.getProperty("excludeColumns");
        setting.fileEncoding = properties.getProperty("fileEncoding");

        Enumeration enums = properties.propertyNames();
        while (enums.hasMoreElements()){
            String key = (String)enums.nextElement();
            if(key != null){
                String value = properties.getProperty(key);
                setting.properties.put(key, value);
                if(key.startsWith("typeMapping.")){
//                    if(value != null){
//                        value = value.replace(" ", "_").toLowerCase();
//                    }
                    setting.typeMapping.put(key.substring("typeMapping.".length()), value);
                }
                if(key.startsWith("tableNameMapping.")){
                    setting.tableNameMapping.put(key.substring("tableNameMapping.".length()), value);
                }
                if(key.startsWith("classNameMapping.")){
                    setting.classNameMapping.put(key.substring("classNameMapping.".length()), value);
                }
            }

        }


        if(StringUtils.isNotBlank(setting.excludeTables)){
            String[] arr = setting.excludeTables.split(",");
            for(String s : arr){
                if(StringUtils.isNotBlank(s)){
                    setting.excludeTableSet.add(s);
                }
            }
        }
        if(StringUtils.isNotBlank(setting.excludeColumns)){
            String[] arr = setting.excludeColumns.split(",");
            for(String s : arr){
                if(StringUtils.isNotBlank(s)){
                    setting.excludeColumnSet.add(s);
                }
            }
        }
        return setting;
    }

    public JdbcProperty getJdbcProperty() {
        return jdbcProperty;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Set<String> getExcludeTables() {
        return excludeTableSet;
    }

    public Set<String> getExcludeColumns() {
        return excludeColumnSet;
    }


    public boolean isValidTable(String table){
        if(excludeTableSet.contains(table)){
            return false;
        }
        return true;
    }

    public boolean isValidColumn(String column){
        if(excludeColumnSet.contains(column)){
            return false;
        }
        return true;
    }

    public Map<String, String> getTypeMapping(){
        return typeMapping;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, String> getTableNameMapping() {
        return tableNameMapping;
    }

    public Map<String, String> getClassNameMapping() {
        return classNameMapping;
    }

    public boolean truncatePrefix() {
        return truncatePrefix;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFileEncoding() {
        return StringUtils.isBlank(fileEncoding) ? "UTF-8" : fileEncoding;
    }
}
