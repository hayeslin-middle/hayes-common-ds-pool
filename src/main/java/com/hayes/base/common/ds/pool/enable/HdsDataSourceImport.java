package com.hayes.base.common.ds.pool.enable;

import com.hayes.base.common.ds.pool.datasource.hds.dynamic.SourceConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsDataSourceImport
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022/7/6 10:38
 **/
@Component
public class HdsDataSourceImport implements ImportBeanDefinitionRegistrar {


    @Resource
    private SourceConfiguration localSourceConfiguration;
    @Resource
    private SourceConfiguration redisSourceConfiguration;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {

        System.out.println("packages value::" + annotationMetadata.getAnnotationAttributes(EnableHdsDataSource.class.getName()));

    }

}
