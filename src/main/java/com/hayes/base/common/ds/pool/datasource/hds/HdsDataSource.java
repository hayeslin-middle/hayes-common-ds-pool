package com.hayes.base.common.ds.pool.datasource.hds;

import com.alibaba.druid.pool.DruidDataSource;
import com.hayes.base.common.ds.pool.datasource.hds.config.HdsConfig;
import com.hayes.base.common.ds.pool.exception.HdsException;
import com.hayes.base.common.ds.pool.exception.HdsResultCode;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceConfig;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.ds.pool.utils.HdsUtil;
import lombok.extern.log4j.Log4j2;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsDataSource
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 14:58
 **/
@Log4j2
public class HdsDataSource extends HdsConfig implements Closeable, DataSource, ConnectionPoolDataSource {

    private volatile boolean inited = false;
    private volatile DruidDataSource dataSourceHolder;
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

    private DruidDataSource buildDataSource(DataSourceGroup group) {

        DataSourceConfig config = HdsUtil.convertJDBCConfig(group);

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setName(config.getName());
        druidDataSource.setDriverClassName(config.getDriverClassName());
        druidDataSource.setUrl(config.getUrl());
        druidDataSource.setUsername(config.getUsername());
        druidDataSource.setPassword(config.getPassword());

        druidDataSource.setInitialSize(config.getConfig().getInitialSize());
        druidDataSource.setMinIdle(config.getConfig().getMinIdle());
        druidDataSource.setMaxActive(config.getConfig().getMaxActive());
        druidDataSource.setMaxWait(config.getConfig().getMaxWait());
        druidDataSource.setTimeBetweenEvictionRunsMillis(config.getConfig().getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(config.getConfig().getMinEvictableIdleTimeMillis());
        druidDataSource.setValidationQuery(config.getConfig().getValidationQuery());
        druidDataSource.setKeepAlive(config.getConfig().isKeepAlive());
        druidDataSource.setTestWhileIdle(config.getConfig().isTestWhileIdle());
        druidDataSource.setTestOnBorrow(config.getConfig().isTestOnBorrow());
        druidDataSource.setTestOnReturn(config.getConfig().isTestOnReturn());
        druidDataSource.setPoolPreparedStatements(config.getConfig().isPoolPreparedStatements());
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(config.getConfig().getMaxPoolPreparedStatementPerConnectionSize());
        druidDataSource.setRemoveAbandoned(config.getConfig().isRemoveAbandoned());
        druidDataSource.setRemoveAbandonedTimeout(config.getConfig().getRemoveAbandonedTimeout());
        druidDataSource.setDefaultAutoCommit(Boolean.TRUE);
        druidDataSource.setDefaultReadOnly(Boolean.FALSE);
        if (config.getConfig().isUseUnfairLock()) druidDataSource.setUseUnfairLock(Boolean.TRUE);
        try {
            druidDataSource.init();
            log.info("datasource - {} init success", config.getName());
        } catch (SQLException e) {
            druidDataSource.close();
            log.error("datasource - " + config.getName() + " init error", e);
            throw new HdsException(HdsResultCode.DS_INIT_ERROR);
        }
        setVersion(group.getVersion());
        return druidDataSource;
    }

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

    private void reload(DataSourceGroup group) {

        DruidDataSource newDruidDataSource = buildDataSource(group);
        DruidDataSource oldDruidDataSource = this.dataSourceHolder;
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
        dataSourceHolder.close();
    }

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        init();
        PooledConnection pooledConnection = this.dataSourceHolder.getPooledConnection();
        pooledConnection.getConnection().setReadOnly(false);
        return pooledConnection;
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        init();
        PooledConnection pooledConnection = this.dataSourceHolder.getPooledConnection(user, password);
        pooledConnection.getConnection().setReadOnly(false);
        return pooledConnection;
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
