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

    private static final String DRIVER_CLASS_NAME = "driverClassName";
    private static final String JDBC_URL_NAME = "jdbcUrl";
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String JDBC_URL_PREFIX = "jdbc:mysql:";
    private static final String DATASOURCE_CLASS_NAME = "com.zaxxer.hikari.HikariDataSource";
    private static final String JDBC_URL_OTHER_SETTINGS = "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

    /**
     * 创建单机数据源
     *
     * @param single
     * @return
     */
    public static DataSource createSingleDataSource(DataSourceGroup.DataBase single) {

        Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(single), Map.class);
        map.put(JDBC_URL_NAME, String.format("%s//%s:%s/%s%s", JDBC_URL_PREFIX, single.getHost(), single.getPort(), single.getDbName(), JDBC_URL_OTHER_SETTINGS));
        map.put(DRIVER_CLASS_NAME, MYSQL_DRIVER_CLASS);
        try {
            return DataSourceUtil.getDataSource(DATASOURCE_CLASS_NAME, map);
        } catch (Exception e) {
            throw new HdsException("创建数据源失败", e);
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

}
