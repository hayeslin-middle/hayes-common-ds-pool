package com.hayes.base.common.ds.pool.datasource.hds.dynamic.impl;

import com.alibaba.fastjson.JSONObject;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceStorage;
import com.hayes.base.common.ds.pool.datasource.model.DataSourceGroup;
import com.hayes.base.common.ds.pool.exception.HdsException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * @program: hayes-common-ds-pool
 * @Class RedisSource
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 15:54
 **/
@Log4j2
@Service
public class LocalSourceConfiguration extends SourceStorage implements SourceConfiguration {


    @Override
    public DataSourceGroup load(String applicationName) {
        Optional<String> localSourceConfigOptional;
        try {
            localSourceConfigOptional = loadFile(applicationName);
        } catch (IOException e) {
            throw new HdsException("load local config error, please check you system!", e);
        }
        return localSourceConfigOptional.map(c -> JSONObject.parseObject(c, DataSourceGroup.class)).orElse(null);

    }

}
