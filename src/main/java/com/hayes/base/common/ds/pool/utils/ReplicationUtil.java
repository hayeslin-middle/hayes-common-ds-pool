package com.hayes.base.common.ds.pool.utils;

import com.hayes.base.common.ds.pool.exception.HdsException;
import com.hayes.base.common.ds.pool.exception.HdsResultCode;
import com.hayes.base.common.ds.pool.model.DataSourceConfig;
import com.hayes.base.common.ds.pool.model.DataSourceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @program: hayes-common-ds-pool
 * @Class ReplicationUtil
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-08 16:05
 **/
public class ReplicationUtil {
    private static final Logger log = LoggerFactory.getLogger(ReplicationUtil.class);

    /**
     * 获取集群 jdbc-url
     *
     * @param cluster
     * @return
     */
    public static String generateClusterJdbcUrl(DataSourceGroup.DataBaseCluster cluster) {
        Optional<DataSourceGroup.DataBase> masterDataBase = HdsUtil.getMaster(cluster.getDataBaseList());
        if (!masterDataBase.isPresent()) {
            // masterDataBase 不存在
            throw new HdsException(HdsResultCode.NO_MASTER);
        }
        DataSourceConfig config = new DataSourceConfig();
        String jdbcUrl = getJdbcUrl(cluster.getDataBaseList());
        StringBuilder sb = new StringBuilder("jdbc:mysql:replication://");
        sb.append(jdbcUrl).append("/").append(cluster.getDbName());
        sb.append("?readFromMasterWhenNoSlaves=").append(config.getReplicationConfig().isReadFromMasterWhenNoSlaves());
        sb.append("&loadBalanceStrategy=").append(config.getReplicationConfig().getLoadBalanceStrategy());
        sb.append("&allowMasterDownConnections=true");
        sb.append("&allowSlaveDownConnections=true");
        sb.append("&retriesAllDown=").append(config.getReplicationConfig().getRetriesAllDown());
        sb.append(config.getReplicationConfig().getOtherSetting());
        sb.append("&allowPublicKeyRetrieval=true");

        return sb.toString();

    }

    private static String getJdbcUrl(List<DataSourceGroup.DataBase> dataBaseList) {
        List<String> jdbcUrls = new ArrayList<>();
        for (DataSourceGroup.DataBase db : dataBaseList) {
            if (db.isMaster()) {
                jdbcUrls.add(String.format("address=(protocol=tcp)(type=master)(host=%s)(port=%s)", db.getHost(), db.getPort()));
            } else {
                jdbcUrls.add(String.format("address=(protocol=tcp)(type=slave)(host=%s)(port=%s)", db.getHost(), db.getPort()));
            }
        }
        return String.join(",", jdbcUrls);
    }

}
