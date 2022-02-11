package com.hayes.base.common.ds.pool;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.hayes.base.common.ds.pool.mapper.*;
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

    @Autowired
    private UserTakeActivityMapper userTakeActivityMapper;

    @Test
    public void contextLoads() {

        // {"applicationDesc":"Mr.HayesLin「数据源测试」","applicationName":"Mr.HayesLin","cluster":{"clusterDesc":"阿里云mysql集群","clusterName":"cluster","dataBaseMap":{"m1":{"dbName":"lottery","dsName":"m1","host":"centos-7-docker","password":"123456","port":3306,"username":"root"},"s1":{"dbName":"lottery","dsName":"s1","host":"centos-8-docker","password":"123456","port":3306,"username":"root"},"s2":{"dbName":"lottery","dsName":"s2","host":"centos-8-docker-test","password":"123456","port":3306,"username":"root"}}},"settings":{"otherRules":{"sql-show":"true"},"rwRules":[{"name":"rw","readDataSourceNames":["m1","s1","s2"],"writeDataSourceName":"m1"}]},"version":1}

        // 测试读写分离
        for (int i = 0; i < 3; i++) {
            List<Award> list = new LambdaQueryChainWrapper<>(awardMapper).list();

            log.info("查询条数：{}", list.size());

            boolean flag = new LambdaUpdateChainWrapper<>(awardMapper).set(Award::getUpdateTime, LocalDateTime.now()).eq(Award::getId, 1L).update();

            log.info("修改：{}", flag);
        }

    }

    @Test
    public void contextLoads1() {

        // {"applicationDesc":"Mr.HayesLin「数据源测试」","applicationName":"Mr.HayesLin","cluster":{"clusterDesc":"阿里云mysql集群","clusterName":"cluster","dataBaseMap":{"ds2":{"dbName":"lottery_02","dsName":"ds2","host":"centos-7-docker","password":"123456","port":3306,"username":"root"},"ds1":{"dbName":"lottery_01","dsName":"ds1","host":"centos-7-docker","password":"123456","port":3306,"username":"root"},"ds":{"dbName":"lottery","dsName":"ds","host":"centos-7-docker","password":"123456","port":3306,"username":"root"}}},"settings":{"otherRules":{"sql-show":"true"},"shardingRule":{"defaultDatabaseShardingStrategy":{"algorithmExpression":"ds$->{ user_id % 2 + 1 }","defaultShardingColumn":"user_id"},"tableGroupShardingStrategy":[{"actualDataNodes":"ds$->{1..2}.user_strategy_export_00$->{0..3}","algorithmExpression":"user_strategy_export_00$->{ user_id % 4 }","logicTable":"user_strategy_export","shardingColumn":"user_id"},{"actualDataNodes":"ds$->{1..2}.user_take_activity","logicTable":"user_take_activity"}]}},"version":1}

        // 测试分库分表
        List<UserStrategyExport> list = new LambdaQueryChainWrapper<>(userStrategyExportMapper).eq(UserStrategyExport::getUserId, 10003).list();
        log.info("查询条数：{}", list.size());
        List<UserTakeActivity> lists = new LambdaQueryChainWrapper<>(userTakeActivityMapper).eq(UserTakeActivity::getUserId, 10004).list();
        log.info("查询条数：{}", lists.size());
        List<Award> list3 = new LambdaQueryChainWrapper<>(awardMapper).eq(Award::getId,1).list();
        log.info("查询条数：{}", list3.size());

    }

}
