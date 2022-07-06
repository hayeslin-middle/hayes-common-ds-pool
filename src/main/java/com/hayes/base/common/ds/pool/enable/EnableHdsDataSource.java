package com.hayes.base.common.ds.pool.enable;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @program: hayes-common-ds-pool
 * @Class EnableHds
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022/7/6 10:36
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HdsDataSourceImport.class)
public @interface EnableHdsDataSource {

    String value();

}
