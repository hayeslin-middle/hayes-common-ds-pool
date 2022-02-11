package com.hayes.base.common.ds.pool.config;

import com.hayes.base.common.ds.pool.datasource.hds.HdsDataSource;
import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsDataSourceAutoConfiguration
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 14:57
 **/
@Log4j2
@Configuration
@ConditionalOnClass(HdsDataSource.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class HdsDataSourceAutoConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;
    @Resource
    private SourceConfiguration redisSourceConfiguration;

    @ConditionalOnMissingBean
    @Bean(name = "hdsDataSource", initMethod = "init", destroyMethod = "close")
    public HdsDataSource hdsDataSource() {
        log.info("Init HdsDataSource.....");
        HdsDataSource hdsDataSource = new HdsDataSource();
        hdsDataSource.setApplicationName(applicationName);
        hdsDataSource.setSource(redisSourceConfiguration);
        return hdsDataSource;
    }

}
