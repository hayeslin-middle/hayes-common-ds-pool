package com.hayes.base.common.ds.pool;

import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: hayes-common-ds-pool
 * @Class ScheduleTest
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022/4/13 17:28
 **/
@Log4j2
public class ScheduleTest {
    /** 调度执行器 */
    private ScheduledExecutorService executorService;

    @Test
    public void contextLoads2() throws ExecutionException, InterruptedException {

        if (getExecutorService() == null) {
            setExecutorService(Executors.newScheduledThreadPool(3));
        }
        AtomicInteger count = new AtomicInteger(0);
        log.info("打印日志：「{}」", count.getAndIncrement());
        ScheduledFuture<?> scheduledFuture = getExecutorService().scheduleWithFixedDelay(() -> {

            log.info("打印日志：「{}」", count.getAndIncrement());

        }, 30000L, 30000L, TimeUnit.MILLISECONDS);


        Object o = scheduledFuture.get();

    }
    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

}
