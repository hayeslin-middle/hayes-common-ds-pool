package com.hayes.base.common.ds.pool.hayes.hayes;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Application  {
  private String name;
  
  private String serviceLevel;
  
  private String gitName;
  
  private String desc;
  
  private String belong;
  
  private String belongCode;
  
  private List<User> owner;
  
  private Setting setting;
  
  private List<String> dataSourceGroupList;
  
  private String operatorName;
  
  private String operatorId;
  
  private String mark;
  
  private String warnTopic;
  
  private String sqlStatTopic;

  public static final class Setting {}
}
