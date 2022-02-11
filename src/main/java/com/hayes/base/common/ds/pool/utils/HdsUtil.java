package com.hayes.base.common.ds.pool.utils;

import com.alibaba.fastjson.JSONObject;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.ds.pool.exception.HdsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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
     * 创建单机数据源
     *
     * @param single
     * @return
     */
    public static DataSource createSingleDataSource(DataSourceGroup.DataBase single) {

        Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(single), Map.class);
        map.put("jdbcUrl", String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true", single.getHost(), single.getPort(), single.getDbName()));
        map.put("driverClassName","com.mysql.cj.jdbc.Driver");
        try {
            return DataSourceUtil.getDataSource("com.zaxxer.hikari.HikariDataSource", map);
        } catch (Exception e) {
            throw new HdsException("创建数据源失败",e);
        }

    }

    /**
     * 创建集群数据源
     *
     * @param cluster
     * @return
     */
    public static Map<String, DataSource> createClusterDataSource(Map<String, DataSourceGroup.DataBase> cluster) {
        Map<String, DataSource> dsMap = new HashMap<>();
        for (Map.Entry<String, DataSourceGroup.DataBase> dataBaseEntry : cluster.entrySet()) {
            dsMap.put(dataBaseEntry.getKey(), createSingleDataSource(dataBaseEntry.getValue()));
        }
        return dsMap;
    }

    ///**
    // * @param group
    // * @return
    // */
    //public static DataSourceConfig convertJDBCConfig(DataSourceGroup group) {
    //    DataSourceConfig dataSourceConfig = new DataSourceConfig();
    //    dataSourceConfig.setName(group.getCluster().getClusterName());
    //    dataSourceConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
    //    dataSourceConfig.setUrl(ReplicationUtil.generateClusterJdbcUrl(group.getCluster()));
    //    dataSourceConfig.setUsername(group.getCluster().getUsername());
    //    dataSourceConfig.setPassword(group.getCluster().getPassword());
    //    return dataSourceConfig;
    //}
    ///**
    // * 获取master
    // *
    // * @param dataBaseList
    // * @return
    // */
    //public static List<DataSourceGroup.DataBase> getSlaves(List<DataSourceGroup.DataBase> dataBaseList) {
    //    return dataBaseList.stream().filter(db -> !db.isMaster()).collect(Collectors.toList());
    //}
    //
    ///**
    // * 获取slaves
    // *
    // * @param dataBaseList
    // * @return
    // */
    //public static Optional<DataSourceGroup.DataBase> getMaster(List<DataSourceGroup.DataBase> dataBaseList) {
    //    return dataBaseList.stream().filter(DataSourceGroup.DataBase::isMaster).findFirst();
    //}

}
