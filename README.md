# jasmine
基于数据库模型、velocity模板的代码生成工具。可用命令行方式运行，也可以作为插件在IntelliJ IDEA里运行

理论上支持所有支持jdbc driver的数据库，例如：
* MySQL
* SQL Server
* Oracle
* PostgreSQL

## jasmine.properties
jasmine的配置文件，可用配置项如下：

| **属性名** | **说明** |
| -------- | --------------- |
| `jdbc.driver`            | jdbc driver的名字，例如：<br><ul><li>com.mysql.jdbc.Driver</li><li>oracle.jdbc.driver.OracleDriver</li><li>org.postgresql.Driver</li><li>com.microsoft.sqlserver.jdbc.SQLServerDriver</li></ul> |
| `jdbc.url`         | jdbc连接串，例如mysql的连接串是jdbc:mysql://ip:port/database。更多内容请参考各类jdbc driver的官方文档。 |
| `jdbc.username`         | 数据库用户名|
| `jdbc.password`  | 密码 |
| `tableName` | 表名，如果指定表名，会忽略tablePrefix值，只把该表读入模型中 |
| `tablePrefix` | 表名前缀，以此前缀开头的表，才会被读入模型中，当tableName未指定时才有效。如果tablePrefix为空并且tableName为空，会读取所有表。  |
| `truncatePrefix`   | true或false。 当指定了tablePrefix才有效。 如果为true，自动读入的模型名称会自动去除掉tablePrefix |
| `excludeTables`                 | 排除掉的表名，如果需要排除多张表，用英文字符逗号(,)隔开。排除掉的表，不会读到模型中。 |
| `excludeColumns`  | 排除掉的列名，如果需要排除多个列，用英文字符逗号(,)隔开。排除掉的列，不会读到模型中。 |
| `fileEncoding`    | 模板文件和生成文件的编码，符合java的charset命名。默认UTF-8 |
| `typeMapping.xxx` | jdbc的TYPE_NAME -> pojo类型映射。例如typeMapping.bit=Boolean，表示要把JDBCType的BIT类型，映射为Boolean类型。 详情请参考下面的类型映射章节 |
| `tableNameMapping.xxx`| 原始表名和读到模型中的表名映射关系。默认情况下会以原始表名作为模型中的表名，但特殊情况下需要改变它，比如在数据水平切分（分表）的情况下，一般和classNameMapping配合使用。具体用法可参考示例demo |
| `classNameMapping.xxx`| 同上 |

## 数据库模型
jasmine在解析velocity模板之前，会根据配置连接数据库，并读入数据模型。

### Database
| **属性** | **说明** |
| ------ | ------ |
| `List<Table> tables` | 表集合。 tableName、tablePrefix、excludeTables会影响表集合|

### Table
| **属性** | **说明** |
| ------ | ------ |
| `String rawName` | 原始表名 |
| `String name` | 表名，受tableNameMapping影响。 |
| `String className` | pojo类名。受classNameMapping影响。 |
| `String remarks` | 表的注释 |
| `List<Column> columns` | 字段集合，受excludeColumns影响 |
| `List<Column> pks` | 主键集合 |

### Column
| **属性** | **说明** |
| ------ | ------ |
| `String name` | 原始字段名。 例如user_name |
| `String fieldName` | 根据命名规则转换后的pojo字段名，例如userName |
| `String propertyName` | 根据命名规则转换后的pojo属性名，例如UserName |
| `String type` | jdbc的TYPE_NAME，例如BIT、VARCHAR |
| `JDBCType jdbcType` | JDBCType|
| `String javaType` | pojo字段类型 |
| `String remarks` | 字段注释 |
| `boolean isPK` | 是否主键 |

### 命名规则
table的className、Column的fieldName、propertyName命名遵循以下规则样例

| **name id db** | **name in model** |
| ------ | ------ |
| name | Name |
| user_name | UserName |
| userName | UserName |
| UserName | UserName |

### 类型映射
jasmine调用DatabaseMetaData.getColumns方法获取每张表的列定义。该方法签名如下：
```
ResultSet getColumns(String catalog, String schemaPattern,
                         String tableNamePattern, String columnNamePattern)
```
然后把ResultSet的DATA_TYPE字段值转换为JDBCType，再根据映射确定它的pojo类型。默认的类型映射关系如下表：
详见代码[TypeMapping.java](./jasmine/src/main/java/org/yidan/jasmine/mapping/TypeMapping.java)

| **JDBC Type** | **POJO Type** |
| ------ | ------ |
| BIT | boolean |
| BOOLEAN | boolean |
| TINYINT | java.long.Short |
| SMALLINT | java.long.Short |
| INTEGER | java.long.Integer |
| BIGINT | java.long.BIGINT |
| FLOAT | java.long.Float |
| REAL | java.long.Double |
| DOUBLE | java.long.Double |
| NUMERIC | java.math.BigDecimal |
| DECIMAL | java.math.BigDecimal |
| CHAR | java.long.String |
| VARCHAR | java.long.String |
| LONGVARCHAR | java.long.String |
| NVARCHAR | java.long.String |
| LONGNVARCHAR | java.long.String |
| DATE | java.util.Date |
| TIME | java.util.Date |
| TIMESTAMP | java.util.Date |
| TIME_WITH_TIMEZONE | java.util.Date |
| TIMESTAMP_WITH_TIMEZONE | java.util.Date |
| BINARY | java.long.Byte[] |
| VARBINARY | java.long.Byte[] |
| LONGVARBINARY | java.long.Byte[] |
| CLOB | java.sql.Clob |
| BLOB | java.sql.Blob |
| ARRAY | java.sql.Array |


以上是默认映射，我们可以用typeMapping.xxx来改变它。
jasmine读取ResultSet的TYPE_NAME字段，转换成小写（如果包含空格，替换成下划线），然后在前面拼接字符串"typeMapping."作为key，查找映射。

例如，我们在jasmine.properties里做如下配置：
```
typeMapping.bit = Boolean
```
那么在生成时，遇到bit类型的列，它的javaType就是Boolean，而不是默认的boolean了。

> 如何确定TYPE_NAME的值呢？ 可以写个velocity模板，把列的type属性输出。
> 提供的demo里的EntityBase模板输出了type，如下：
```
public class ${model.className}Base {

## ~~ begin column to field
#foreach($column in $model.table.columns)
    /**
    * ${column.remarks}
    * type ${column.type}
    */
	private $column.javaType $column.fieldName;
#end  ## ~~ end column to field
...
}
```
输出结果如下：
```
public class UserBase {

    /**
    * 
    * type BIGINT UNSIGNED
    */
	private Long id;
    /**
    * 
    * type VARCHAR
    */
    private String account;
    ...
}
```