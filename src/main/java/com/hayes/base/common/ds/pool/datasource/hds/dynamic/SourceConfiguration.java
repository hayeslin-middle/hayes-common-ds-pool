package com.hayes.base.common.ds.pool.datasource.hds.dynamic;

import com.hayes.base.common.ds.pool.model.DataSourceGroup;

/**
 * @program: hayes-common-ds-pool
 * @interface DsSource
 * @description: 关于此接口的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 15:52
 **/
public interface SourceConfiguration {

    /**
     * 加载数据源配置
     *
     * @param applicationName
     * @return
     */
    DataSourceGroup load(String applicationName);

}
