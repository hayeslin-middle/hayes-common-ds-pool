package com.hayes.base.common.ds.pool;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.hayes.base.common.ds.pool.mapper.Award;
import com.hayes.base.common.ds.pool.mapper.AwardMapper;
import com.hayes.base.common.ds.pool.mapper.UserStrategyExport;
import com.hayes.base.common.ds.pool.mapper.UserStrategyExportMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @program: hayes-common-ds-pool
 * @Class DemoApplicationTests
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 15:09
 **/
@Log4j2
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HdsApplicationStarter.class)
public class DemoApplicationTests {

    @Autowired
    private AwardMapper awardMapper;
    @Autowired
    private UserStrategyExportMapper userStrategyExportMapper;

    @Test
    public void contextLoads() {
        // 测试读写分离
        List<Award> list = new LambdaQueryChainWrapper<>(awardMapper).list();

        log.info("查询条数：{}", list.size());

        boolean flag = new LambdaUpdateChainWrapper<>(awardMapper).set(Award::getUpdateTime, LocalDateTime.now()).eq(Award::getId, 1L).update();

        log.info("修改：{}", flag);

    }

    @Test
    public void contextLoads1() {
        // 测试分库分表
        List<UserStrategyExport> list = new LambdaQueryChainWrapper<>(userStrategyExportMapper).eq(UserStrategyExport::getUserId, 100).list();

        log.info("查询条数：{}", list.size());

    }

}
