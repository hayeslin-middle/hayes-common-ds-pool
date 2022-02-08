package com.hayes.base.common.ds.pool.utils;

import com.hayes.base.common.ds.pool.datasource.model.DataSourceConfig;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsUtils
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 16:04
 **/
public class HdsUtil {
    private static final Logger log = LoggerFactory.getLogger(HdsUtil.class);

    /**
     * @param group
     * @return
     */
    public static DataSourceConfig convertJDBCConfig(DataSourceGroup group) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setName(group.getCluster().getClusterName());
        dataSourceConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceConfig.setUrl(ReplicationUtil.generateClusterJdbcUrl(group.getCluster()));
        dataSourceConfig.setUsername(group.getCluster().getUsername());
        dataSourceConfig.setPassword(group.getCluster().getPassword());
        return dataSourceConfig;
    }

    /**
     * 获取master
     *
     * @param dataBaseList
     * @return
     */
    public static List<DataSourceGroup.DataBase> getSlaves(List<DataSourceGroup.DataBase> dataBaseList) {
        return dataBaseList.stream().filter(db -> !db.isMaster()).collect(Collectors.toList());
    }

    /**
     * 获取slaves
     *
     * @param dataBaseList
     * @return
     */
    public static Optional<DataSourceGroup.DataBase> getMaster(List<DataSourceGroup.DataBase> dataBaseList) {
        return dataBaseList.stream().filter(DataSourceGroup.DataBase::isMaster).findFirst();
    }

}
