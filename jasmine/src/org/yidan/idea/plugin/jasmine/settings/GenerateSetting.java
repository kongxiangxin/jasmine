package org.yidan.idea.plugin.jasmine.settings;

import io.netty.util.internal.StringUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class GenerateSetting {
    private JdbcProperty jdbcProperty;

    private String tablePrefix;
    private boolean truncatePrefix;
    private Set<String> excludeTableSet = new HashSet<>();
    private String excludeTables;
    private String excludeColumns;
    private Set<String> excludeColumnSet = new HashSet<>();

    private Map<String, String> typeMapping = new HashMap<>();

    private Map<String, String> properties = new HashMap<>();


    public static GenerateSetting getInstance(Properties properties){
        if(properties == null){
            return null;
        }

        GenerateSetting setting = new GenerateSetting();
        setting.jdbcProperty = JdbcProperty.getInstance(properties);
        setting.tablePrefix = properties.getProperty("tablePrefix");
        setting.truncatePrefix = "true".equals(properties.getProperty("truncatePrefix"));
        setting.excludeTables = properties.getProperty("excludeTables");
        setting.excludeColumns = properties.getProperty("excludeColumns");

        Enumeration enums = properties.propertyNames();
        while (enums.hasMoreElements()){
            String key = (String)enums.nextElement();
            if(key != null){
                String value = properties.getProperty(key);
                setting.properties.put(key, value);
                if(key.startsWith("typeMapping.")){
                    if(value != null){
                        value = value.replace(" ", "_").toLowerCase();
                    }
                    setting.typeMapping.put(key.substring("typeMapping.".length()), value);
                }
            }

        }


        if(!StringUtil.isNullOrEmpty(setting.excludeTables)){
            String[] arr = setting.excludeTables.split(",");
            for(String s : arr){
                if(!StringUtil.isNullOrEmpty(s)){
                    setting.excludeTableSet.add(s);
                }
            }
        }
        if(!StringUtil.isNullOrEmpty(setting.excludeColumns)){
            String[] arr = setting.excludeColumns.split(",");
            for(String s : arr){
                if(!StringUtil.isNullOrEmpty(s)){
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
        if(!StringUtil.isNullOrEmpty(tablePrefix) && !table.startsWith(tablePrefix)){
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

    public boolean truncatePrefix() {
        return truncatePrefix;
    }
}
