package org.yidan.idea.plugin.jasmine.dao;

import org.yidan.idea.plugin.jasmine.settings.JdbcProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        return DriverManager.getConnection(this.property.getUrl(), this.property.getUsername(), this.property.getPassword());
    }
}
