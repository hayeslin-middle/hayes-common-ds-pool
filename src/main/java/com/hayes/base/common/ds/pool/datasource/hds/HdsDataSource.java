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
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

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
            return new ShardingSphereDataSource(DefaultSchema.LOGIC_NAME, null, clusterDataSource,
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

        // 分库策略
        DataBaseTableShardingRule.DefaultDatabaseShardingStrategy defaultDatabaseShardingStrategy = shardingRule.getDefaultDatabaseShardingStrategy();
        if (Optional.ofNullable(defaultDatabaseShardingStrategy).map(DataBaseTableShardingRule.DefaultDatabaseShardingStrategy::getDefaultShardingColumn).isPresent()) {
            Properties defaultDatabaseStrategyInlineProps = new Properties();
            log.info("绑定 sharding database algorithms ： {}", defaultDatabaseShardingStrategy.getAlgorithmExpression());
            defaultDatabaseStrategyInlineProps.setProperty("algorithm-expression", defaultDatabaseShardingStrategy.getAlgorithmExpression());
            shardingRuleConfiguration.getShardingAlgorithms().put("default_db_strategy_inline", new ShardingSphereAlgorithmConfiguration("INLINE", defaultDatabaseStrategyInlineProps));
            shardingRuleConfiguration.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration(defaultDatabaseShardingStrategy.getDefaultShardingColumn(), "default_db_strategy_inline"));
        }
        // 分表策略
        Collection<DataBaseTableShardingRule.TableShardingStrategy> tableGroupShardingStrategy = shardingRule.getTableGroupShardingStrategy();
        if (CollUtil.isNotEmpty(tableGroupShardingStrategy)) {
            for (DataBaseTableShardingRule.TableShardingStrategy tableShardingStrategy : tableGroupShardingStrategy) {
                // 分表规则start
                if (Optional.ofNullable(tableShardingStrategy).map(DataBaseTableShardingRule.TableShardingStrategy::getLogicTable).isPresent()) {
                    log.info("绑定 sharding table: {}", tableShardingStrategy.getLogicTable());
                    ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(tableShardingStrategy.getLogicTable(), tableShardingStrategy.getActualDataNodes());
                    // 分库配置- 上面有了默认暂不需要
                    //shardingTableRuleConfiguration.setDatabaseShardingStrategy();
                    // 分表配置
                    if (StringUtils.isNotBlank(tableShardingStrategy.getShardingColumn()) && StringUtils.isNotBlank(tableShardingStrategy.getAlgorithmExpression())) {
                        String algorithmKey = String.format("%s_%s_strategy_inline", tableShardingStrategy.getLogicTable(), tableShardingStrategy.getShardingColumn());
                        shardingTableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration(tableShardingStrategy.getShardingColumn(), algorithmKey));

                        Properties tableShardingStrategyInlineProps = new Properties();
                        log.info("绑定 sharding table algorithms ： {}", tableShardingStrategy.getAlgorithmExpression());
                        tableShardingStrategyInlineProps.setProperty("algorithm-expression", tableShardingStrategy.getAlgorithmExpression());
                        shardingRuleConfiguration.getShardingAlgorithms().put(algorithmKey, new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingStrategyInlineProps));
                    }
                    // 主键策略
                    if (StringUtils.isNotBlank(tableShardingStrategy.getPrimaryKey())) {
                        shardingTableRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration(tableShardingStrategy.getPrimaryKey(), "snowflake"));
                    }
                    // 绑定分表表
                    shardingRuleConfiguration.getBindingTableGroups().add(tableShardingStrategy.getLogicTable());
                    // 保存分表规则
                    shardingRuleConfiguration.getTables().add(shardingTableRuleConfiguration);
                }
            }
        }
        // 主键生成策略
        shardingRuleConfiguration.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "snowflake"));

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

}
