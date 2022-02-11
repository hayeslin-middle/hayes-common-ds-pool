package com.hayes.base.common.ds.pool.datasource.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReadWriteSplitRule {

    private String name;

    private String autoAwareDataSourceName;

    private String writeDataSourceName;

    private List<String> readDataSourceNames;

    private String loadBalancerName;

}