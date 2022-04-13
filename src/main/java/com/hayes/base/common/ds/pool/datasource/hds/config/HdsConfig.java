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
    /** 应用名 */
    private String applicationName;
    /** 调度执行器 */
    private ScheduledExecutorService executorService;
    /** 定时刷新间隔 */
    private long refreshInterval = 30000L;
    /** 是否开启定时刷新配置 */
    private boolean autoRefresh = true;
    /** redis数据源配置 */
    private SourceConfiguration redisSourceConfiguration;
    /** 本地数据源配置 */
    private SourceConfiguration localSourceConfiguration;
    /** 版本号 */
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
