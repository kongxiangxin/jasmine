package org.yidan.idea.plugin.jasmine.dao;

import org.yidan.idea.plugin.jasmine.mapping.NamingStrategy;
import org.yidan.idea.plugin.jasmine.mapping.TypeMapping;
import org.yidan.idea.plugin.jasmine.meta.Column;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.meta.Table;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;
import org.yidan.idea.plugin.jasmine.settings.JdbcProperty;
import io.netty.util.internal.StringUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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
        ResultSet rs = this.metaData.getTables(null, "%", "%", new String[]{"TABLE"});

        while(rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            if(setting.isValidTable(tableName)){
                String className = null;
                if(setting.truncatePrefix() && !StringUtil.isNullOrEmpty(setting.getTablePrefix())){
                    className = NamingStrategy.tableToClassName(tableName.substring(setting.getTablePrefix().length()));
                }else{
                    className = NamingStrategy.tableToClassName(tableName);
                }
                Table table = new Table(tableName, className);
                fillColumns(table);
                database.addTable(table);
            }
        }

        rs.close();
    }

    private void fillColumns(Table table) throws Throwable {
        ResultSet rs = this.metaData.getColumns(null, "%", table.getName(), "%");
        Map<String,String> typeMapping = setting.getTypeMapping();
        while(rs.next()) {
            String column = rs.getString("COLUMN_NAME");
            if(setting.isValidColumn(column)){
                String javaType = null;
                String typeName = rs.getString("TYPE_NAME");
                JDBCType jdbcType = JDBCType.valueOf(rs.getInt("DATA_TYPE"));
                if(!typeMapping.isEmpty()){
                    String key = typeName.replace(" ", "_").toLowerCase();
                    if(typeMapping.containsKey(key)){
                        javaType = typeMapping.get(key);
                    }
                }
                if(javaType == null){
                    javaType = TypeMapping.dbToJavaType(jdbcType);
                }
                table.addColumn(new Column(column, typeName, jdbcType, javaType));
            }
        }
        rs.close();
    }
}
