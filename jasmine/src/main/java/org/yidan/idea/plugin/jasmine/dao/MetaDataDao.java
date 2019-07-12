package org.yidan.idea.plugin.jasmine.dao;

import org.apache.commons.lang.StringUtils;
import org.yidan.idea.plugin.jasmine.mapping.NamingStrategy;
import org.yidan.idea.plugin.jasmine.mapping.TypeMapping;
import org.yidan.idea.plugin.jasmine.meta.Column;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.meta.Table;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;
import org.yidan.idea.plugin.jasmine.settings.JdbcProperty;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class MetaDataDao {
    private Connection conn;
    private DatabaseMetaData metaData;
    private GenerateSetting setting;

    public MetaDataDao(GenerateSetting setting) throws SQLException, ClassNotFoundException {
        this.setting = setting;
        this.init(setting.getJdbcProperty());
    }

    private void init(JdbcProperty property) throws SQLException, ClassNotFoundException {
        try {
            this.conn = (new DBConnection(property)).getConnection();
            this.metaData = this.conn.getMetaData();
        } catch (Throwable e) {
            throw e;
        }
    }

//    public List<String> getDabatases() throws Throwable {
//        Statement statement = this.conn.createStatement();
//        ResultSet rs = statement.executeQuery("show databases");
//        ArrayList databases = new ArrayList();
//
//        while(rs.next()) {
//            databases.add(rs.getString(1));
//        }
//
//        statement.close();
//        rs.close();
//        return databases;
//    }
//
//

    public Database getDatabase() throws Throwable {
        Database database = new Database();
        fillTables(database);
        return database;
    }

    private void fillTables(Database database) throws Throwable {
        String tablePattern;
        if (StringUtils.isNotBlank(setting.getTableName())) {
            tablePattern = setting.getTableName();
        } else if (StringUtils.isNotBlank(setting.getTablePrefix())) {
            tablePattern = setting.getTablePrefix() + "%";
        } else {
            tablePattern = "%";
        }
        ResultSet rs = this.metaData.getTables(null, "%", tablePattern, new String[]{"TABLE"});
        Map<String, String> tableNameMapping = setting.getTableNameMapping();
        Map<String, String> classNameMapping = setting.getClassNameMapping();

        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            if (setting.isValidTable(tableName)) {
                String className = null;
                if (classNameMapping.containsKey(tableName)) {
                    className = classNameMapping.get(tableName);
                } else {
                    if (StringUtils.isBlank(setting.getTableName()) && setting.truncatePrefix() && StringUtils.isNotBlank(setting.getTablePrefix())) {
                        //去掉前缀
                        className = NamingStrategy.tableToClassName(tableName.substring(setting.getTablePrefix().length()));
                    } else {
                        className = NamingStrategy.tableToClassName(tableName);
                    }
                }
                //判断表名是否有mapping
                String mappedName = tableName;
                if (tableNameMapping.containsKey(tableName)) {
                    mappedName = tableNameMapping.get(tableName);
                }

                String remarks = rs.getString("REMARKS");

                Table table = new Table(tableName, mappedName, className, remarks);

                fillColumns(table);
                database.addTable(table);
            }
        }

        rs.close();
    }

    private void fillColumns(Table table) throws Throwable {
        ResultSet rs = this.metaData.getPrimaryKeys(null, null, table.getRawName());
        Set<String> keys = new HashSet<>();
        while (rs.next()){
            String column = rs.getString("COLUMN_NAME");
            keys.add(column);
        }
        rs.close();

        rs = this.metaData.getColumns(null, "%", table.getRawName(), "%");
        Map<String, String> typeMapping = setting.getTypeMapping();
        while (rs.next()) {
            String column = rs.getString("COLUMN_NAME");
            if (setting.isValidColumn(column)) {
                String javaType = null;
                String typeName = rs.getString("TYPE_NAME");
                JDBCType jdbcType = JDBCType.valueOf(rs.getInt("DATA_TYPE"));
                if (!typeMapping.isEmpty()) {
                    String key = typeName.replace(" ", "_").toLowerCase();
                    if (typeMapping.containsKey(key)) {
                        javaType = typeMapping.get(key);
                    }
                }
                if (javaType == null) {
                    javaType = TypeMapping.dbToJavaType(jdbcType);
                }
                String remarks = rs.getString("REMARKS");
                Column c = new Column(column, typeName, jdbcType, javaType, remarks);
                table.addColumn(c);

                if(keys.contains(column)){
                    c.setPK(true);
                    table.addPK(c);
                }
            }
        }
        rs.close();
    }


}
