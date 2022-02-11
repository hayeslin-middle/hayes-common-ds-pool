package com.hayes.base.common.ds.pool.hayes.hayes;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Getter
@Setter
public class DataSourceGroup extends BaseModel {
  private String name;
  
  private String desc;
  
  private Application application;
  
  private String appName;
  
  private DatabaseCluster cluster;
  
  private String dbName;
  
  private List<String> databaseList;
  
  private String token;
  
  private Setting setting;
  

  private int version = 1;
  
  private boolean current = true;
  
  private String mark;
  
  private String operatorName;
  
  private String operatorId;


  public List<Database> selectedDatabaseList() {
    Map<String, Database> databaseMap = this.cluster.getDatabaseList().stream().collect(Collectors.toMap(Database::getName, database -> database));
    return this.databaseList.stream().map(databaseMap::get).filter(Objects::nonNull).collect(Collectors.toList());
  }
  @Getter
  @Setter
  public static final class Setting implements Serializable {
    private Map<String, DataBaseSetting> dataBaseSettingMap;

    private DataSourceConfig dataSourceConfig;

    private ReplicationConfig replicationConfig;
    
  }
  @Getter
  @Setter
  public static class DataBaseSetting implements Serializable {
    private int weight;
    
  }
  @Getter
  @Setter
  public static final class DataSourceConfig {
    private transient String url;
    
    private String username;
    
    private String password;
    


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
    

    private boolean removeAbandoned = true;
    

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
  @Getter
  @Setter
  public static final class ReplicationConfig {

    private boolean readFromMasterWhenNoSlaves = true;
    

    private boolean readFromMaster = false;
    

    private String loadBalanceStrategy = "random";
    

    private int retriesAllDown = 120;
    

    private String otherSetting = "useUnicode=true&characterEncoding=utf-8&useSSL=false";
    
  }
}
