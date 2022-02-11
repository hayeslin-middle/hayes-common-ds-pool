package com.hayes.base.common.ds.pool.hayes.hayes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class DatabaseCluster extends BaseModel {
  private String name;
  
  private String desc;
  
  private List<Database> databaseList;
  
  private int version = 1;
  
  private boolean current = true;
  
  private String mark;
  
  private String operatorName;
  
  private String operatorId;
  
}
