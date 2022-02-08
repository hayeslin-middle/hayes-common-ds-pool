package com.hayes.base.common.ds.pool.datasource.hds.dynamic.impl;

import com.alibaba.fastjson.JSONObject;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import com.hayes.base.common.ds.pool.exception.HdsException;
import com.hayes.base.common.ds.pool.exception.HdsResultCode;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.redis.service.RedisService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @program: hayes-common-ds-pool
 * @Class RedisSource
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 15:54
 **/
@Log4j2
@Service
public class RedisSourceConfiguration implements SourceConfiguration {

    @Autowired
    private RedisService redisService;

    @Override
    public DataSourceGroup load(String applicationName) {

        String key = "hds:" + applicationName;

        Object obj = redisService.get(key);
        if (Objects.isNull(obj)) throw new HdsException(HdsResultCode.NO_DS_CF);

        DataSourceGroup dataSourceGroup = JSONObject.parseObject(obj.toString(), DataSourceGroup.class);

        log.info("数据源配置信息：App:{}, Cluster: {} ", dataSourceGroup.getApplicationDesc(), dataSourceGroup.getCluster().getClusterDesc());

        return dataSourceGroup;
    }
}
