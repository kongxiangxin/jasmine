# jasmine
基于数据库模型、velocity模板的代码生成工具。可用命令行方式运行，也可以作为插件在IntelliJ IDEA里运行

理论上支持所有提供jdbc driver的数据库，例如：
* MySQL
* SQL Server
* Oracle
* PostgreSQL

## Quick Start
1. 去releases页面，下载最新的jasmine-[version].zip，解压。
2. clone源码至本地，根据实际情况，修改demo/jasmine.properties中jdbc相关的配置（主要是数据库连接配置）
3. 执行以下命令：
```shell script
/path/to/jasmine-[version]/bin/jasmine /path/to/jasmine-src/demo/jasmine.properties
```
如果一切正常，会在demo下看到生成出来的文件

## IDEA插件安装方法
在IDEA里，通过Install plugin from disk，选择下载下来的zip包即可（不需要解压）。

安装成功之后，在IDEA的工具栏中，会多出一个jasmine的图标，如下图：
![jasmine插件](https://wx1.sinaimg.cn/mw690/65c35b14ly1g6zfo4b5sxj21hs0g0juh.jpg)


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
| `tableNameMapping.xxx`| 如果我们把某个大表的数据，分割到多张表中，可利用tableNameMapping和classNameMapping <br> 例如，订单数据量太大，根据订单号hash到10张表里，表名可能是这个样子：order_01、order_02、...、order_10，这10张表的结构完全相同。<br> 我们可以指定tableName，只利用其中一张表作为模型来生成代码，但是如果在模板中不特殊处理的话，生成的实体类名是Order01，生成的sql中的表名是order_01。这显然不是我们想要的结果。 |
| `classNameMapping.xxx`| 同上 |

示例：
```properties
#jasmine代码生成配置文件

#编码
#fileEncoding = UTF-8

#数据库配置
jdbc.driver=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/jasmine_demo?useUnicode=true&characterEncoding=UTF8
jdbc.username=root
jdbc.password=123


#表名，如果指定表名，则表前缀方式失效
#tableName = my_table1

#表名前缀，以此前缀开头的表，才会被读入模型中，当tableName未指定时才有效。如果tablePrefix为空并且tableName为空，会读取所有表。
tablePrefix = jm_
#true或false。 当指定了tablePrefix才有效。 如果为true，自动读入的模型名称会自动去除掉tablePrefix
truncatePrefix = true

#排除掉的表名，如果需要排除多张表，用英文字符逗号(,)隔开。排除掉的表，不会读到模型中。
excludeTables=jm_table1,jm_table2

#排除掉的列名，如果需要排除多个列，用英文字符逗号(,)隔开。排除掉的列，不会读到模型中。
#excludeColumns = _id

#原始表名和读到模型中的表名映射关系。默认情况下会以原始表名作为模型中的表名，但特殊情况下需要改变它，比如在数据水平切分（分表）的情况下，一般和classNameMapping配合使用
tableNameMapping.order_01 = $[order]$
classNameMapping.order_01 = Order

#jdbc的TYPE_NAME -> pojo类型映射，用来改变默认的映射规则
typeMapping.bit = Boolean
typeMapping.boolean = Boolean
```


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
| TINYINT | java.lang.Short |
| SMALLINT | java.lang.Short |
| INTEGER | java.lang.Integer |
| BIGINT | java.lang.BIGINT |
| FLOAT | java.lang.Float |
| REAL | java.lang.Double |
| DOUBLE | java.lang.Double |
| NUMERIC | java.math.BigDecimal |
| DECIMAL | java.math.BigDecimal |
| CHAR | java.lang.String |
| VARCHAR | java.lang.String |
| LONGVARCHAR | java.lang.String |
| NVARCHAR | java.lang.String |
| LONGNVARCHAR | java.lang.String |
| DATE | java.util.Date |
| TIME | java.util.Date |
| TIMESTAMP | java.util.Date |
| TIME_WITH_TIMEZONE | java.util.Date |
| TIMESTAMP_WITH_TIMEZONE | java.util.Date |
| BINARY | java.lang.Byte[] |
| VARBINARY | java.lang.Byte[] |
| LONGVARBINARY | java.lang.Byte[] |
| CLOB | java.sql.Clob |
| BLOB | java.sql.Blob |
| ARRAY | java.sql.Array |
| 其他 | java.lang.String |


以上是默认映射，我们可以用typeMapping.xxx来改变它。
jasmine读取ResultSet的TYPE_NAME字段，转换成小写（如果包含空格，替换成下划线），然后在前面拼接字符串"typeMapping."作为key，查找映射。

例如，我们在jasmine.properties里做如下配置：
```
typeMapping.bit = Boolean
```
那么在生成时，遇到bit类型的列，它的javaType就是Boolean，而不是默认的boolean了。

> 如何确定TYPE_NAME的值呢？ 可以写个velocity模板，把列的type属性输出。
> demo里的EntityBase模板输出了type，如下：
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

## 模板
### 路径和命名
模板基于velocity语法，需要满足以下两个条件，才可以被jasmine识别。
1. 模板文件以*.jm.vm命名
2. 模板文件存放在jasmine.properties文件所在的目录下（可以和jasmine.properties在同一目录，也可以在子目录下）

> 在demo中，你会发现除了*.jm.vm文件，还有很多*.tpl.vm、*.vm文件，这些模板文件，并不是由jasmine主动识别的，而是在*.jm.vm文件中直接或间接解析的。

### VelocityContext中的对象
jasmine在调用velocity解析模板的时候，向VelocityContext中塞入以下java对象，在模板中可以引用这些对象，读取这些对象的属性，甚至调用这些对象的方法。
#### model
model就是上面提到的Database对象，通过它，可以遍历表、字段.
> 注意，模板中调用process或generate方法，在新的模板中，上下文中的$model对象不一定是Database

#### setting
jasmine.properties中的配置，会读入setting对象中。请参考[GenerateSetting](./jasmine/src/main/java/org/yidan/jasmine/settings/GenerateSetting.java)的定义
#### processor
process是[TemplateProcessor](./jasmine/src/main/java/org/yidan/jasmine/TemplateProcessor.java)类的实例，它提供以下两个方法，可以在模板中调用。
```
/**
 * 调用velocity引擎解析模板，忽略生成的内容。
 * 可以在模板中调用本方法，用来在模板中解析别的模板
 * @param templateFile 模板路径，相对于.jm.vm文件的路径
 * @param model 传入模板velocityContext中的对象，以model为key，在模板中可以用$model引用它
 */
void process(String templateFile, Object model);

/**
 * 调用velocity引擎解析模板，并把解析出的文本，保存至outputFile中
 * 可以在模板中调用本方法，用来在模板中解析别的模板，并保存至outputFile中
 * @param templateFile 模板路径，相对于.jm.vm文件的路径
 * @param outputFile 输出文件路径，相对于.jm.vm文件的路径
 * @param replaceIfExists 如果outputFile已经存在，是否替换
 * @param model 传入模板velocityContext中的对象，以model为key，在模板中可以用$model引用它
 */
void generate(String templateFile, String outputFile, boolean replaceIfExists, Object model);

```
> 注意，模板中调用process或generate方法，会改变上下文中的$model对象，可以参考demo

## 示例DEMO —— 基于MySQL数据库的MyBatis实体和映射文件生成模板
Demo文件夹中的模板，是经过<b>精心打磨</b>的、可以直接拿来用于生产的模板，它支持以下特性：

1. 一键生成实体类、MyBatis Repository、MyBatis Mapper
2. 实体类、MyBatis Repository和MyBatis Mapper均利用继承策略，划分出XXXX和XXXXBase两个文件，其中XXXX如果文件存在则不覆盖，XXXXBase每次生成都会覆盖。
如果在生成后你需要做一些代码上的调整，请在XXXX文件中修改，而不要在XXXXBase中修改。这样做的好处是一旦我们的表结构发生变化需要重新生成时，不会覆盖您手动改过的代码。
3. 如果表存在is_deleted字段，生成的delete方法是逻辑删除而不是物理删除
4，如果表存在record_version字段，update语句带有乐观锁，即update .... set record_version=record_version + 1 where .... and record_version=#{record_version}
5. 如果表存在create_time，insert语句这一列的值是now()
6. 如果表存在update_time, insert和update语句这一列的值是now()

入口模板：tpl1.jm.vm

```
##tpl1.jm.vm

## model是Database对象
#set($cfg = {
    "basePkg" : "com.abc.demo",
    "tplPath" : "./tpl",
    "baseTargetPath" : "../src/main/java/com/abc/demo",
    "db":${model}
})

#set($cfg.package = "${cfg.basePkg}.entity")
#set($cfg.modelPkg = "${cfg.basePkg}.model")
#set($cfg.targetPath = "${cfg.baseTargetPath}/entity")
$processor.process("${cfg.tplPath}/Entity.tpl.vm", $cfg)

#set($cfg.entityPkg = "${cfg.basePkg}.entity")
#set($cfg.repoPkg = "${cfg.basePkg}.repository")
#set($cfg.targetPath = "${cfg.baseTargetPath}/repository")
$processor.process("${cfg.tplPath}/Repository.tpl.vm", $cfg)

#set($cfg.targetPath = "../src/main/resources/mapper")
$processor.process("${cfg.tplPath}/Mapper.tpl.vm", $cfg)
```

在tpl1.jm.vm模板里，调用$processor.process分别解析Entity、Repository和Mapper，并向引擎传递了新的model

```
## Entity.tpl.vm

## 此时，model是tpl1.jm.vm中传入的cfg

#foreach($table in $model.db.tables)

    #set($entityModel = {"table" : $table, "package":$model.package, "modelPkg":$model.modelPkg})
    #set($entityModel.className = ${table.className})

    ##生成xxxxBase.java，如果目标文件存在，替换
    $processor.generate("${model.tplPath}/EntityBase.vm", "${model.targetPath}/${table.className}Base.java", true, $entityModel)
    
    ##生成xxxx.java，如果目标文件存在，忽略，不替换
    $processor.generate("${model.tplPath}/Entity.vm", "${model.targetPath}/${table.className}.java", false, 
)
#end
```
在Entity.tpl.vm里，调用$processor.generate生成EntityBase和Entity，并像引擎传递新的model
对于EntityBase，每次生成都会替换目标文件；对于Entity，如果目标文件已经存在了，就会忽略这个文件的生成。

下面再看看EntityBase.vm的定义片段
```
## 此时，model是Entity.tpl.vm中传入的entityModel
package $model.package;

/**
* ${model.table.remarks}
* <p>
* ${model.className}Base.
* </p>
* <p>
* auto generated by jasmine, please do not modify it!
* </p>
*/
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
```

