package com.hayes.base.common.ds.pool.datasource.hds.config;

import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsConfig
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 14:59
 **/
@Setter
@Getter
public abstract class HdsConfig {

    private String applicationName;

    private SourceConfiguration source;

    private ScheduledExecutorService executorService;

    private long refreshInterval = 30000L;

    private boolean autoRefresh = false;

    private Integer version;

    protected DataSourceGroup getDataSourceGroup() {
        return source.load(this.applicationName);
    }

    public long getRefreshInterval() {
        if (refreshInterval < 10000L) {
            return 10000L;
        }
        return refreshInterval;
    }
}
