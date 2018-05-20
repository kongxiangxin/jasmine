package org.yidan.idea.plugin.jasmine.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class Database {
    private List<Table> tables = new ArrayList<>();

    public List<Table> getTables() {
        return tables;
    }

    public void addTable(Table table){
        tables.add(table);
    }
}
