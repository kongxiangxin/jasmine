package org.yidan.jasmine.dao;

import org.yidan.jasmine.settings.JdbcProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class DBConnection {
    private JdbcProperty property;

    public DBConnection(JdbcProperty property) throws ClassNotFoundException {
        this.property = property;
        Class.forName(property.getDriver());
    }

    public Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("remarksReporting", "true");
        props.setProperty("useInformationSchema", "true");//设置可以获取tables remarks信息
        props.setProperty("remarks", "true"); //设置可以获取remarks信息
        props.setProperty("user", this.property.getUsername());
        props.setProperty("password", this.property.getPassword());

        return DriverManager.getConnection(this.property.getUrl(), props);
    }
}
