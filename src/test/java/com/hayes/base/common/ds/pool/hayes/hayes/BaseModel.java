package com.hayes.base.common.ds.pool.hayes.hayes;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public abstract class BaseModel {
  protected String id;
  
  protected boolean deleted;
  
  protected Date createTime;
  
  protected Date updateTime;
  
}
