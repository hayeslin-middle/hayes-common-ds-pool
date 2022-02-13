package com.hayes.base.common.ds.pool.datasource.hds.config;

import com.alibaba.fastjson.JSONObject;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceStorage;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.ds.pool.exception.HdsException;
import com.hayes.base.common.ds.pool.exception.HdsResultCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsConfig
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 14:59
 **/
@Log4j2
@Setter
@Getter
public abstract class HdsConfig {

    private String applicationName;

    private ScheduledExecutorService executorService;

    private long refreshInterval = 30000L;

    private boolean autoRefresh = false;

    private SourceConfiguration redisSourceConfiguration;

    private SourceConfiguration localSourceConfiguration;

    private Integer version;

    protected DataSourceGroup getDataSourceGroup() {

        DataSourceGroup load = redisSourceConfiguration.load(this.applicationName);
        if (Objects.isNull(load) || StringUtils.isBlank(load.getApplicationName())) {
            load = localSourceConfiguration.load(this.applicationName);
            if (Objects.isNull(load) || StringUtils.isBlank(load.getApplicationName())) {
                throw new HdsException(HdsResultCode.NO_DS_CF);
            }
        } else {
            try {
                SourceStorage.saveFile(applicationName, JSONObject.toJSONString(load, true));
            } catch (IOException e) {
                log.warn("配置本地化失败", e);
            }
        }
        return load;
    }

    public long getRefreshInterval() {
        if (refreshInterval < 10000L) {
            return 10000L;
        }
        return refreshInterval;
    }
}
