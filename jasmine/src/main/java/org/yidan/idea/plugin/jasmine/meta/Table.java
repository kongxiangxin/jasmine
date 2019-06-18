package org.yidan.idea.plugin.jasmine.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class Table {
    private String rawName;
    private String name;
    private String className;
    private String remarks;
    private List<Column> columns = new ArrayList<>();
    private List<Column> pks = new ArrayList<>();

    public Table(String rawName, String name, String className, String remarks) {
        this.rawName = rawName;
        this.name = name;
        this.className = className;
        this.remarks = remarks;
    }

    public List<Column> getColumns() {
        return this.columns;
    }

    public List<Column> getPks() {
        return pks;
    }

    public String getName() {
        return this.name;
    }

    public String getClassName() {
        return className;
    }

    public String getRawName() {
        return rawName;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public String getRemarks() {
        return remarks;
    }

    public void addPK(Column column) {
        pks.add(column);
    }
}
