package com.hayes.base.common.ds.pool.datasource.hds.dynamic.impl;

import com.alibaba.fastjson.JSONObject;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import com.hayes.base.common.ds.pool.datasource.model.DataBaseTableShardingRule;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.ds.pool.datasource.model.ReadWriteSplitRule;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @program: hayes-common-ds-pool
 * @Class RedisSource
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 15:54
 **/
@Log4j2
@Service
public class LocalSourceConfiguration implements SourceConfiguration {

    @Override
    public DataSourceGroup load(String applicationName) {
        //return rwSplitConfiguration();
        return shardingTbConfiguration();


    }

    private DataSourceGroup rwSplitConfiguration() {
        //
        Map<String, DataSourceGroup.DataBase> map = getRwSplitConfigurationDataBaseMap();

        DataSourceGroup.DataBaseCluster dataBaseCluster = new DataSourceGroup.DataBaseCluster();
        dataBaseCluster.setClusterName("cluster");
        dataBaseCluster.setClusterDesc("阿里云mysql集群");
        dataBaseCluster.setDataBaseMap(map);


        ReadWriteSplitRule readWriteSplitRule = new ReadWriteSplitRule();
        readWriteSplitRule.setName("rw");
        readWriteSplitRule.setWriteDataSourceName("m1");
        readWriteSplitRule.setReadDataSourceNames(Arrays.asList("m1", "s1", "s2"));
        //readWriteSplitRule.setLoadBalancerName("ROUND_ROBIN");
        DataSourceGroup.DataSourceRule settings = new DataSourceGroup.DataSourceRule();
        settings.setRwRules(Collections.singleton(readWriteSplitRule));
        settings.setOtherRules(Collections.singletonMap("sql-show", "true"));

        DataSourceGroup dataSourceGroup = new DataSourceGroup();
        dataSourceGroup.setApplicationName("Mr.HayesLin");
        dataSourceGroup.setApplicationDesc("Mr.HayesLin「数据源测试」");
        dataSourceGroup.setVersion(1);
        dataSourceGroup.setCluster(dataBaseCluster);
        dataSourceGroup.setSettings(settings);
        log.info(JSONObject.toJSONString(dataSourceGroup));
        return dataSourceGroup;
    }


    private Map<String, DataSourceGroup.DataBase> getRwSplitConfigurationDataBaseMap() {
        DataSourceGroup.DataBase db_m1 = new DataSourceGroup.DataBase();
        db_m1.setDsName("m1");
        db_m1.setHost("centos-7-docker");
        db_m1.setUsername("root");
        db_m1.setPassword("123456");
        db_m1.setDbName("lottery");
        DataSourceGroup.DataBase db_s1 = new DataSourceGroup.DataBase();
        db_s1.setDsName("s1");
        db_s1.setHost("centos-8-docker");
        db_s1.setUsername("root");
        db_s1.setPassword("123456");
        db_s1.setDbName("lottery");

        DataSourceGroup.DataBase db_s2 = new DataSourceGroup.DataBase();
        db_s2.setDsName("s2");
        db_s2.setHost("centos-8-docker-test");
        db_s2.setUsername("root");
        db_s2.setPassword("123456");
        db_s2.setDbName("lottery");

        Map<String, DataSourceGroup.DataBase> map = new HashMap<>();
        map.put(db_m1.getDsName(), db_m1);
        map.put(db_s1.getDsName(), db_s1);
        map.put(db_s2.getDsName(), db_s2);
        return map;
    }

    private DataSourceGroup shardingTbConfiguration() {
        //
        Map<String, DataSourceGroup.DataBase> map = getShardingTbConfigurationDataBaseMap();

        DataSourceGroup.DataBaseCluster dataBaseCluster = new DataSourceGroup.DataBaseCluster();
        dataBaseCluster.setClusterName("cluster");
        dataBaseCluster.setClusterDesc("阿里云mysql集群");
        dataBaseCluster.setDataBaseMap(map);


        DataBaseTableShardingRule.DefaultDatabaseShardingStrategy defaultDatabaseShardingStrategy = new DataBaseTableShardingRule.DefaultDatabaseShardingStrategy();
        defaultDatabaseShardingStrategy.setAlgorithmExpression("ds$->{ user_id % 2 + 1 }");
        defaultDatabaseShardingStrategy.setDefaultShardingColumn("user_id");

        List<DataBaseTableShardingRule.TableShardingStrategy> tableShardingStrategies = new ArrayList<>();
        DataBaseTableShardingRule.TableShardingStrategy tableShardingStrategy = new DataBaseTableShardingRule.TableShardingStrategy();
        tableShardingStrategy.setLogicTable("user_strategy_export");
        tableShardingStrategy.setActualDataNodes("ds$->{1..2}.user_strategy_export_00$->{0..3}");
        tableShardingStrategy.setShardingColumn("user_id");
        tableShardingStrategy.setAlgorithmExpression("user_strategy_export_00$->{ user_id % 4 }");
        DataBaseTableShardingRule.TableShardingStrategy tableShardingStrategy2 = new DataBaseTableShardingRule.TableShardingStrategy();
        tableShardingStrategy2.setLogicTable("user_take_activity");
        tableShardingStrategy2.setActualDataNodes("ds$->{1..2}.user_take_activity");
        tableShardingStrategies.add(tableShardingStrategy);
        tableShardingStrategies.add(tableShardingStrategy2);

        DataBaseTableShardingRule tableShardingRule = new DataBaseTableShardingRule();
        tableShardingRule.setDefaultDatabaseShardingStrategy(defaultDatabaseShardingStrategy);
        tableShardingRule.setTableGroupShardingStrategy(tableShardingStrategies);


        DataSourceGroup.DataSourceRule settings = new DataSourceGroup.DataSourceRule();
        settings.setShardingRule(tableShardingRule);
        settings.setOtherRules(Collections.singletonMap("sql-show", "true"));

        DataSourceGroup dataSourceGroup = new DataSourceGroup();
        dataSourceGroup.setApplicationName("Mr.HayesLin");
        dataSourceGroup.setApplicationDesc("Mr.HayesLin「数据源测试」");
        dataSourceGroup.setVersion(1);
        dataSourceGroup.setCluster(dataBaseCluster);
        dataSourceGroup.setSettings(settings);
        log.info(JSONObject.toJSONString(dataSourceGroup));
        return dataSourceGroup;
    }

    private Map<String, DataSourceGroup.DataBase> getShardingTbConfigurationDataBaseMap() {
        DataSourceGroup.DataBase db_m1 = new DataSourceGroup.DataBase();
        db_m1.setDsName("ds");
        db_m1.setHost("centos-7-docker");
        db_m1.setUsername("root");
        db_m1.setPassword("123456");
        db_m1.setDbName("lottery");
        DataSourceGroup.DataBase db_s1 = new DataSourceGroup.DataBase();
        db_s1.setDsName("ds1");
        db_s1.setHost("centos-7-docker");
        db_s1.setUsername("root");
        db_s1.setPassword("123456");
        db_s1.setDbName("lottery_01");

        DataSourceGroup.DataBase db_s2 = new DataSourceGroup.DataBase();
        db_s2.setDsName("ds2");
        db_s2.setHost("centos-7-docker");
        db_s2.setUsername("root");
        db_s2.setPassword("123456");
        db_s2.setDbName("lottery_02");

        Map<String, DataSourceGroup.DataBase> map = new HashMap<>();
        map.put(db_m1.getDsName(), db_m1);
        map.put(db_s1.getDsName(), db_s1);
        map.put(db_s2.getDsName(), db_s2);
        return map;
    }


}
