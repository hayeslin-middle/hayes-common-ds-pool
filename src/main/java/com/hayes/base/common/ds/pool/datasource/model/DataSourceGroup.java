package com.hayes.base.common.ds.pool.datasource.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @program: hayes-common-ds-pool
 * @Class DatasourceGroup
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-01-29 10:54
 **/
@Getter
@Setter
public class DataSourceGroup {

    private String applicationName;
    private String applicationDesc;
    private int version;

    /**
     * single 和 cluster 互斥
     */
    private DataBase single;
    private DataBaseCluster cluster;

    private DataSourceRule settings;

    @Getter
    @Setter
    public static final class DataBaseCluster {

        private String clusterName;
        private String clusterDesc;
        private List<String> dsNames;
        private Map<String, DataBase> dataBaseMap;

        /**
         * 如果集群下用户名｜密码｜数据库名 一样
         */
        private String dbName;
        private String username;
        private String password;

    }

    @Getter
    @Setter
    public static final class DataSourceRule {

        // 读写分离
        private Collection<ReadWriteSplitRule> rwRules;
        // 分库分表
        private DataBaseTableShardingRule shardingRule;
        // 数据加密 （暂未支持）
        private Collection<EncryptColumnRule> encryptColumnRules;

        //其他配置
        private Map<String, String> otherRules;

    }



    @Getter
    @Setter
    public static final class DataBase {

        private String dsName;
        private String host;
        private int port = 3306;

        private String username;
        private String password;
        private String dbName;

    }

}
