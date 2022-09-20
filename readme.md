# HDS 

| Hayes DataSource

企业实际开发中：

1⃣️ 一般都会做隐藏数据源信息，数据源信息**统一管理和存放**。

2⃣️ 其次一些数据源配置过于繁杂，主从同步，读写分离，外加分库分表各种配置，意在**复杂配置简单化**。


本程序意在配置信息统一管理。简化配置，分类管理。**支持版本号动态刷新数据源。**



## 核心功能

1. 读写分离
2. 分库分表
3. 动态刷新数据源


## 基本使用

1. 坐标依赖 

```xml
<dependency>
    <groupId>com.hayes.base</groupId>
    <artifactId>hayes-common-ds-pool</artifactId>
    <version>${ds-pool.version}</version>
</dependency>
```

2. 配置applicationName

微服务项目都要配置吧，不用多说了吧

3. redis [ hds:applicationName ]

设置redis中key为：[ hds:applicationName ] value：[示例.json ](doc/示例.json)

4. 完结， 启动


## 配置详解

1. applicationDesc ：见名知意 。 略
2. applicationName ：见名知意 。 略
3. single & cluster 互斥，两者只会加载一个，s先c后

```json
{
   "single": {
        "dbName": "lottery",
        "dsName": "ds",
        "host": "127.0.0.1",
        "password": "123456",
        "port": 3306,
        "username": "root"
    },
    "cluster": {
        "clusterDesc": "阿里RDS",
        "clusterName": "alibaba-rds",
        "dataBaseMap": {
            "ds": {
                "dbName": "lottery",
                "dsName": "ds",
                "host": "127.0.0.1",
                "password": "123456",
                "port": 3306,
                "username": "root"
            },
            "ds1": {
                "dbName": "lottery_01",
                "dsName": "ds1",
                "host": "127.0.0.1",
                "password": "123456",
                "port": 3306,
                "username": "root"
            },
            "ds2": {
                "dbName": "lottery_02",
                "dsName": "ds2",
                "host": "127.0.0.1",
                "password": "123456",
                "port": 3306,
                "username": "root"
            }
        }
    }
}
```

4. settings

```json
1. 分表策略：Inline ，原因算法简单

{
    "settings": {
        "otherRules": {
            "sql-show": "false"
        },
        "shardingRule": {
            "defaultDatabaseShardingStrategy": {
                "algorithmExpression": "ds$->{ col1 % 2 + 1 }",
                "defaultShardingColumn": "col1"
            },
            "tableGroupShardingStrategy": [
                {
                    "actualDataNodes": "ds$->{1..2}.table1_00$->{0..3}",
                    "algorithmExpression": "table1_00$->{ col1 % 4 }",
                    "logicTable": "table1",
                    "shardingColumn": "col1"
                },
                {
                    "actualDataNodes": "ds$->{1..2}.table2",
                    "logicTable": "table2"
                }
            ]
        }
    }

}

```



5. version ：见名知意
   1. 更改了数据源，需要version版本号加1，无需重启服务，可自行刷新
   2. 默认开启自动刷新，每30s刷新一次。🉑️自行配置
