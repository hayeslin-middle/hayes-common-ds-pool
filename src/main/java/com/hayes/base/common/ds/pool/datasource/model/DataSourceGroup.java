package com.hayes.base.common.ds.pool.datasource.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private int version ;
    private DataBaseCluster cluster;

    @Getter
    @Setter
    public static final class DataBaseCluster {

        private String clusterName;
        private String clusterDesc;
        private List<DataBase> dataBaseList;

        /**
         * 如果集群下用户名｜密码｜数据库名 一样
         */
        private String dbName;
        private String username;
        private String password;

    }

    @Getter
    @Setter
    public static final class DataBase {

        private String dsName;
        private boolean master;
        private String host;
        private int port = 3306;

        private String username;
        private String password;
        private String dbName;

    }

}
