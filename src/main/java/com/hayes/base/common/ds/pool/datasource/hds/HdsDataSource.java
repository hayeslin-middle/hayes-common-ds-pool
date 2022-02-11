package com.hayes.base.common.ds.pool.datasource.hds;

import cn.hutool.core.collection.CollUtil;
import com.hayes.base.common.ds.pool.datasource.hds.config.HdsConfig;
import com.hayes.base.common.ds.pool.datasource.model.DataBaseTableShardingRule;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.ds.pool.datasource.model.EncryptColumnRule;
import com.hayes.base.common.ds.pool.datasource.model.ReadWriteSplitRule;
import com.hayes.base.common.ds.pool.exception.HdsException;
import com.hayes.base.common.ds.pool.utils.HdsUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsDataSource
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 14:58
 **/
@Log4j2
public class HdsDataSource extends HdsConfig implements Closeable, DataSource {

    private volatile ShardingSphereDataSource dataSourceHolder;
    private volatile boolean inited = false;
    private Lock lock = new ReentrantLock();

    public void init() {

        if (this.inited) return;

        try {
            this.lock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new HdsException("init interrupt", e);
        }
        try {
            if (this.inited) return;

            DataSourceGroup dataSourceGroup = getDataSourceGroup();
            this.dataSourceHolder = this.buildDataSource(dataSourceGroup);
            startScheduleRefresh();
        } catch (Exception e) {
            log.error("init error: {}", e.getMessage(), e);
            throw new HdsException("数据源初始化失败", e);
        } finally {
            this.inited = true;
            this.lock.unlock();
        }

    }

    /**
     * 创建数据源
     *
     * @param group
     * @return
     */
    private ShardingSphereDataSource buildDataSource(DataSourceGroup group) {

        DataSourceGroup.DataBase single = group.getSingle();
        if (Objects.nonNull(single) && StringUtils.isNotBlank(single.getDsName())) {
            return createSingleDataSource(single);
        } else {
            return createClusterDataSource(group);
        }

    }

    /**
     * 创建单机数据源
     *
     * @param single
     * @return
     */
    private ShardingSphereDataSource createSingleDataSource(DataSourceGroup.DataBase single) {
        try {
            DataSource dataSource = HdsUtil.createSingleDataSource(single);
            return new ShardingSphereDataSource(single.getDsName(), null,
                    Collections.singletonMap(single.getDsName(), dataSource),
                    Collections.emptyList(), null);
        } catch (Exception e) {
            throw new HdsException("interrupt eeror ", e);
        }
    }

    /**
     * 创建集群数据源
     *
     * @param group
     * @return
     */
    private ShardingSphereDataSource createClusterDataSource(DataSourceGroup group) {


        try {
            // 获取数据源
            DataSourceGroup.DataBaseCluster cluster = group.getCluster();
            Map<String, DataSource> clusterDataSource = HdsUtil.createClusterDataSource(cluster.getDataBaseMap());

            // 获取规则
            List<RuleConfiguration> ruleConfigurationList = new ArrayList<>();
            ruleConfigurationList.add(createReadwriteSplittingRuleContext(group.getSettings().getRwRules()));
            ruleConfigurationList.add(createShardingRuleContext(group.getSettings().getShardingRule()));
            ruleConfigurationList.add(createEncryptColumnRuleContext(group.getSettings().getEncryptColumnRules()));

            //* 其他配置 */
            Properties otherProperties = new Properties();
            Map<String, String> otherRules = group.getSettings().getOtherRules();
            if (Objects.nonNull(otherRules)) {
                //otherProperties.setProperty("sql-show", "true");
                for (Map.Entry<String, String> stringObjectEntry : otherRules.entrySet()) {
                    otherProperties.setProperty(stringObjectEntry.getKey(), stringObjectEntry.getValue());
                }
            }
            return new ShardingSphereDataSource("cluster", null, clusterDataSource,
                    ruleConfigurationList.stream().filter(Objects::nonNull).collect(Collectors.toList()), otherProperties);
        } catch (Exception e) {
            throw new HdsException("interrupt eeror ", e);
        }

    }

    /**
     * 获取读写分离规则配置
     *
     * @param rwRule
     * @return
     */
    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRuleContext(Collection<ReadWriteSplitRule> rwRule) {
        if (rwRule == null) return null;

        List<ReadwriteSplittingDataSourceRuleConfiguration> readwriteSplittingDataSourceRuleConfigurations = new ArrayList<>();
        for (ReadWriteSplitRule readWriteSplitRule : rwRule) {
            readwriteSplittingDataSourceRuleConfigurations.add(
                    new ReadwriteSplittingDataSourceRuleConfiguration(readWriteSplitRule.getName(), readWriteSplitRule.getAutoAwareDataSourceName(),
                            readWriteSplitRule.getWriteDataSourceName(), readWriteSplitRule.getReadDataSourceNames(), readWriteSplitRule.getLoadBalancerName()));
        }

        //负载均衡算法
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalanceMaps = new HashMap<>();
        loadBalanceMaps.put("random", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()));
        loadBalanceMaps.put("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));

        return new ReadwriteSplittingRuleConfiguration(readwriteSplittingDataSourceRuleConfigurations, loadBalanceMaps);
    }

    /**
     * 获取分库分表规则配置
     *
     * @param shardingRule
     * @return
     */
    private ShardingRuleConfiguration createShardingRuleContext(DataBaseTableShardingRule shardingRule) {
        if (shardingRule == null) return null;

        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        // 默认主键生成策略
        shardingRuleConfiguration.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "snowflake"));
        // 默认分库策略
        DataBaseTableShardingRule.DefaultDatabaseShardingStrategy defaultDatabaseShardingStrategy = shardingRule.getDefaultDatabaseShardingStrategy();
        if (Optional.ofNullable(defaultDatabaseShardingStrategy).map(DataBaseTableShardingRule.DefaultDatabaseShardingStrategy::getDefaultShardingColumn).isPresent()) {
            Properties defaultDatabaseStrategyInlineProps = new Properties();
            defaultDatabaseStrategyInlineProps.setProperty("algorithm-expression", defaultDatabaseShardingStrategy.getAlgorithmExpression());
            shardingRuleConfiguration.getShardingAlgorithms().put("default_db_strategy_inline", new ShardingSphereAlgorithmConfiguration("INLINE", defaultDatabaseStrategyInlineProps));
            shardingRuleConfiguration.setDefaultDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration(defaultDatabaseShardingStrategy.getDefaultShardingColumn(), "default_db_strategy_inline"));
        }
        // 分表规则
        Collection<DataBaseTableShardingRule.TableShardingStrategy> tableGroupShardingStrategy = shardingRule.getTableGroupShardingStrategy();
        if (CollUtil.isNotEmpty(tableGroupShardingStrategy)) {
            for (DataBaseTableShardingRule.TableShardingStrategy tableShardingStrategy : tableGroupShardingStrategy) {
                // 设置分表规则
                if (Optional.ofNullable(tableShardingStrategy).map(DataBaseTableShardingRule.TableShardingStrategy::getLogicTable).isPresent()) {
                    ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(tableShardingStrategy.getLogicTable(), tableShardingStrategy.getActualDataNodes());
                    Properties tableShardingStrategyInlineProps = new Properties();
                    tableShardingStrategyInlineProps.setProperty("algorithm-expression", tableShardingStrategy.getAlgorithmExpression());
                    // // // t_order_user_id_strategy_inline
                    String algorithmKey = String.format("%s_%s_strategy_inline", tableShardingStrategy.getLogicTable(), tableShardingStrategy.getShardingColumn());
                    shardingRuleConfiguration.getShardingAlgorithms().put(algorithmKey, new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingStrategyInlineProps));
                    shardingTableRuleConfiguration.setTableShardingStrategy(new ComplexShardingStrategyConfiguration(tableShardingStrategy.getShardingColumn(), algorithmKey));
                    if (StringUtils.isNotBlank(tableShardingStrategy.getPrimaryKey())) {
                        // 主键策略
                        shardingTableRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration(tableShardingStrategy.getPrimaryKey(), "snowflake"));
                    }
                    // 绑定分表表
                    shardingRuleConfiguration.getBindingTableGroups().add(tableShardingStrategy.getLogicTable());
                    // 保存分表规则
                    shardingRuleConfiguration.getTables().add(shardingTableRuleConfiguration);
                }
            }
        }
        // 主键生成算法
        shardingRuleConfiguration.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
        shardingRuleConfiguration.getKeyGenerators().put("uuid", new ShardingSphereAlgorithmConfiguration("UUID", new Properties()));

        // 绑定所有分表表
        //shardingRuleConfiguration.setBindingTableGroups(shardingRule.getBindingTableGroups());
        // 默认分表策略
        // shardingRuleConfiguration.setDefaultTableShardingStrategy();
        // 自动分表规则
        // shardingRuleConfiguration.setAutoTables();
        // 广播的表
        // shardingRuleConfiguration.setBroadcastTables();
        // 默认分片键
        // shardingRuleConfiguration.setDefaultShardingColumn();
        // 自定义其他策略
        // shardingRuleConfiguration.setShardingAlgorithms();
        // 自定义主键生成策略
        // shardingRuleConfiguration.setKeyGenerators();

        return shardingRuleConfiguration;
    }

    /**
     * 获取数据加密规则配置
     *
     * @param encryptColumnRule
     * @return
     */
    private AlgorithmProvidedEncryptRuleConfiguration createEncryptColumnRuleContext(Collection<EncryptColumnRule> encryptColumnRule) {
        return null;
    }

    /**
     * 定时刷新数据源配置
     */
    private void startScheduleRefresh() {
        if (getExecutorService() == null) {
            setExecutorService(Executors.newScheduledThreadPool(3));
        }
        getExecutorService().scheduleWithFixedDelay(() -> {
            if (isAutoRefresh()) {
                try {
                    DataSourceGroup dataSourceGroup = getDataSourceGroup();
                    if (dataSourceGroup.getVersion() > getVersion()) {
                        reload(dataSourceGroup);
                    }
                } catch (Exception e) {
                    log.error("reload error: {}", e.getMessage());
                }
            }
        }, getRefreshInterval(), getRefreshInterval(), TimeUnit.MILLISECONDS);

    }

    /**
     * 重新加载数据源配置
     *
     * @param group
     */
    private void reload(DataSourceGroup group) {

        ShardingSphereDataSource newDruidDataSource = buildDataSource(group);
        ShardingSphereDataSource oldDruidDataSource = this.dataSourceHolder;
        this.dataSourceHolder = newDruidDataSource;
        try {
            oldDruidDataSource.close();
        } catch (Exception e) {
            log.error("close old dataSource error!", e);
        }
        log.info("datasource reloaded!");
    }

    @Override
    public void close() throws IOException {
        try {
            dataSourceHolder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        init();
        Connection connection = this.dataSourceHolder.getConnection();
        connection.setReadOnly(false);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        init();
        Connection connection = this.dataSourceHolder.getConnection(username, password);
        connection.setReadOnly(false);
        return connection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this.dataSourceHolder.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.dataSourceHolder.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.dataSourceHolder.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.dataSourceHolder.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.dataSourceHolder.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.dataSourceHolder.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.dataSourceHolder.getParentLogger();
    }

//    public void sharding(){
//        /* 数据源配置 */
//        HikariDataSource writeDataSource0 = new HikariDataSource();
//        writeDataSource0.setDriverClassName("com.mysql.jdbc.Driver");
//        writeDataSource0.setJdbcUrl("jdbc:mysql://localhost:3306/db0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
//        writeDataSource0.setUsername("root");
//        writeDataSource0.setPassword("");
//
//        HikariDataSource writeDataSource1 = new HikariDataSource();
//// ...忽略其他数据库配置项
//
//        HikariDataSource read0OfwriteDataSource0 = new HikariDataSource();
//// ...忽略其他数据库配置项
//
//        HikariDataSource read1OfwriteDataSource0 = new HikariDataSource();
//// ...忽略其他数据库配置项
//
//        HikariDataSource read0OfwriteDataSource1 = new HikariDataSource();
//// ...忽略其他数据库配置项
//
//        HikariDataSource read1OfwriteDataSource1 = new HikariDataSource();
//// ...忽略其他数据库配置项
//
//        Map<String, DataSource> datasourceMaps = new HashMap<>(6);
//
//        datasourceMaps.put("write_ds0", writeDataSource0);
//        datasourceMaps.put("write_ds0_read0", read0OfwriteDataSource0);
//        datasourceMaps.put("write_ds0_read1", read1OfwriteDataSource0);
//
//        datasourceMaps.put("write_ds1", writeDataSource1);
//        datasourceMaps.put("write_ds1_read0", read0OfwriteDataSource1);
//        datasourceMaps.put("write_ds1_read1", read1OfwriteDataSource1);
//
//        /* 分片规则配置 */
//        // 表达式 ds_${0..1} 枚举值表示的是主从配置的逻辑数据源名称列表
//        ShardingTableRuleConfiguration tOrderRuleConfiguration = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${[0, 1]}");
//        tOrderRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
//        tOrderRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "tOrderInlineShardingAlgorithm"));
//        Properties tOrderShardingInlineProps = new Properties();
//        tOrderShardingInlineProps.setProperty("algorithm-expression", "t_order_${order_id % 2}");
//        tOrderRuleConfiguration.getShardingAlgorithms().putIfAbsent("tOrderInlineShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE",tOrderShardingInlineProps));
//
//
//        ShardingTableRuleConfiguration tOrderItemRuleConfiguration = new ShardingTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${[0, 1]}");
//        tOrderItemRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "snowflake"));
//        tOrderItemRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_item_id", "tOrderItemInlineShardingAlgorithm"));
//        Properties tOrderItemShardingInlineProps = new Properties();
//        tOrderItemShardingInlineProps.setProperty("algorithm-expression", "t_order_item_${order_item_id % 2}");
//        tOrderItemRuleConfiguration.getShardingAlgorithms().putIfAbsent("tOrderItemInlineShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE",tOrderItemShardingInlineProps));
//
//        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
//        shardingRuleConfiguration.getTables().add(tOrderRuleConfiguration);
//        shardingRuleConfiguration.getTables().add(tOrderItemRuleConfiguration);
//        shardingRuleConfiguration.getBindingTableGroups().add("t_order, t_order_item");
//        shardingRuleConfiguration.getBroadcastTables().add("t_bank");
//        // 默认分库策略
//        shardingRuleConfiguration.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "default_db_strategy_inline"));
//        Properties defaultDatabaseStrategyInlineProps = new Properties();
//        defaultDatabaseStrategyInlineProps.setProperty("algorithm-expression", "ds_${user_id % 2}");
//        shardingRuleConfiguration.getShardingAlgorithms().put("default_db_strategy_inline", new ShardingSphereAlgorithmConfiguration("INLINE", defaultDatabaseStrategyInlineProps));
//        // 分布式序列算法配置
//        Properties snowflakeProperties = new Properties();
//        shardingRuleConfiguration.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", snowflakeProperties));
//
//        /* 数据加密规则配置 */
//        Properties encryptProperties = new Properties();
//        encryptProperties.setProperty("aes-key-value", "123456");
//        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("username", "username", "", "username_plain", "name_encryptor");
//        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd", "pwd", "assisted_query_pwd", "", "pwd_encryptor");
//        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest));
//
//        Map<String, ShardingSphereAlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>(2, 1);
//        encryptAlgorithmConfigs.put("name_encryptor", new ShardingSphereAlgorithmConfiguration("AES", encryptProperties));
//        encryptAlgorithmConfigs.put("pwd_encryptor", new ShardingSphereAlgorithmConfiguration("assistedTest", encryptProperties));
//        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);
//
//        /* 读写分离规则配置 */
//        Properties readwriteProps1 = new Properties();
//        readwriteProps1.setProperty("write-data-source-name", "write_ds0");
//        readwriteProps1.setProperty("read-data-source-names", "write_ds0_read0, write_ds0_read1");
//        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration1 = new ReadwriteSplittingDataSourceRuleConfiguration("ds_0", "Static", readwriteProps1, "roundRobin");
//        Properties readwriteProps2 = new Properties();
//        readwriteProps2.setProperty("write-data-source-name", "write_ds0");
//        readwriteProps2.setProperty("read-data-source-names", "write_ds1_read0, write_ds1_read1");
//        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration2 = new ReadwriteSplittingDataSourceRuleConfiguration("ds_1", "Static", readwriteProps2, "roundRobin");
//
//        //负载均衡算法
//        Map<String, ShardingSphereAlgorithmConfiguration> loadBalanceMaps = new HashMap<>(1);
//        loadBalanceMaps.put("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));
//
//        ReadwriteSplittingRuleConfiguration readWriteSplittingyRuleConfiguration = new ReadwriteSplittingRuleConfiguration(Arrays.asList(dataSourceConfiguration1, dataSourceConfiguration2), loadBalanceMaps);
//
//        /* 其他配置 */
//        Properties otherProperties = new Properties();
//        otherProperties.setProperty("sql-show", "true");
//
//        /* shardingDataSource 就是最终被 ORM 框架或其他 jdbc 框架引用的数据源名称 */
//        DataSource shardingDataSource = ShardingSphereDataSourceFactory.createDataSource(datasourceMaps, Arrays.asList(shardingRuleConfiguration, readWriteSplittingyRuleConfiguration, encryptRuleConfiguration), otherProperties);
//    }


}
