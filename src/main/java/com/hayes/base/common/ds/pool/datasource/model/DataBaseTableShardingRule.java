package com.hayes.base.common.ds.pool.datasource.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * @program: hayes-common-ds-pool
 * @Class DataBaseTableShardingRule
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-10 20:47
 **/
@Getter
@Setter
public class DataBaseTableShardingRule {
    /**
     * 分库配置
     */
    private DefaultDatabaseShardingStrategy defaultDatabaseShardingStrategy;
    /**
     * 分表配置
     */
    private Collection<TableShardingStrategy> tableGroupShardingStrategy;

    @Getter
    @Setter
    public static final class DefaultDatabaseShardingStrategy {

        private String algorithmExpression;
        private String defaultShardingColumn;

    }

    @Getter
    @Setter
    public static final class TableShardingStrategy {

        private String primaryKey;
        private String logicTable;
        private String shardingColumn;
        private String actualDataNodes;
        private String algorithmExpression;

    }


}
