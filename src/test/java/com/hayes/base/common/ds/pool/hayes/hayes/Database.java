package com.hayes.base.common.ds.pool.hayes.hayes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Database extends BaseModel {
  private boolean master;
  
  private int slaveLevel;
  
  private String name;
  
  private String desc;
  
  private int version = 1;
  
  private boolean current = true;
  
  private String host;
  
  private int port;
  
  private InstanceInfo instanceInfo;
  
  private String mark;
  
  private String operatorName;
  
  private String operatorId;


  @Getter
  @Setter
  public static class InstanceInfo {
    private String type;
    
    private int cpu;
    
    private String memory;
    
    private String store;
    

  }
}
