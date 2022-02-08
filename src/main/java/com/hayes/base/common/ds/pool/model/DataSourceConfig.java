package com.hayes.base.common.ds.pool.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @program: hayes-common-ds-pool
 * @Class DataSourceConfig
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-01-30 14:18
 **/
@Setter
@Getter
public class DataSourceConfig {

    private String name;
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    private Config config = new Config();
    private ReplicationConfig replicationConfig = new ReplicationConfig();

    @Setter
    @Getter
    public static final class Config {
        private int initialSize = 0;
        private int minIdle = 0;
        private int maxActive = 8;
        private long maxWait = -1L;
        private long timeBetweenEvictionRunsMillis = 60000L;
        private long minEvictableIdleTimeMillis = 1800000L;
        private String validationQuery = "SELECT 1";
        private boolean keepAlive = false;
        private boolean testWhileIdle = true;
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean poolPreparedStatements = false;
        private int maxPoolPreparedStatementPerConnectionSize = 10;
        private boolean removeAbandoned = false;
        private int removeAbandonedTimeout = 1800;
        private boolean useUnfairLock = true;
        private int pingTimeout = 1;
        private long healthCheckInterval = 10000L;
        private long reportSqlStatInterval = 10000L;
        private boolean mergeSql = true;
        private long slowSqlMillis = 3000L;
        private boolean logSlowSql = true;
        private boolean logAllSql = false;
        private boolean statementLogErrorEnabled = true;
        private boolean statementExecutableSqlLogEnable = false;
        private boolean logSqlUcase = true;
        private boolean logSqlPrettyFormat = false;
        private boolean openUtf8mb4 = true;
        private boolean useSQLRouter = true;
    }

    @Setter
    @Getter
    public static final class ReplicationConfig {
        private boolean readFromMasterWhenNoSlaves = true;
        private boolean readFromMaster = false;
        private String loadBalanceStrategy = "random";
        private int retriesAllDown = 120;
        private String otherSetting = "&useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
    }

}
