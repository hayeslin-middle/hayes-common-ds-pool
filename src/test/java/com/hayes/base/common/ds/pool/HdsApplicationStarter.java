package com.hayes.base.common.ds.pool;

import com.hayes.base.common.ds.pool.enable.EnableHdsDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: hayes-common-ds-pool
 * @Class HdsAoolicationStarter
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 15:08
 **/
@EnableHdsDataSource(value = "hayes-lottery")
@SpringBootApplication
public class HdsApplicationStarter {
    public static void main(String[] args) {
        SpringApplication.run(HdsApplicationStarter.class, args);
    }
}
