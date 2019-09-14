package org.yidan.jasmine.settings;

import java.util.Properties;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class JdbcProperty {
    private String driver;
    private String url;
    private String username;
    private String password;

    private JdbcProperty(){
    }

    public static JdbcProperty getInstance(Properties properties){
        if(properties == null){
            return null;
        }
        JdbcProperty property = new JdbcProperty();
        property.driver = properties.getProperty("jdbc.driver");
        property.url = properties.getProperty("jdbc.url");
        property.username = properties.getProperty("jdbc.username");
        property.password = properties.getProperty("jdbc.password");
        return property;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
