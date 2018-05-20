package org.yidan.idea.plugin.jasmine.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class Table {
    private String name;
    private String className;
    private List<Column> columns = new ArrayList<>();

    public Table(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public List<Column> getColumns() {
        return this.columns;
    }

    public String getName() {
        return this.name;
    }

    public String getClassName() {
        return className;
    }

    public void addColumn(Column column){
        columns.add(column);
    }

}
