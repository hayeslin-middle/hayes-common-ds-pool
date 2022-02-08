package com.hayes.base.common.ds.pool.exception;

import com.hayes.base.common.core.result.ResultCode;

/**
 * @program: hayes-common-dependencies-spring-cloud
 * @enum HdsResultCode
 * @description: 关于此枚举的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-01-29 17:51
 **/
public enum HdsResultCode implements ResultCode {
    NO_GROUP(200001,"不存在数据源组"),
    NO_DS_CF(200002,"不存在数据源配置"),
    NO_MASTER(200003,"不存在master数据源"),
    DS_INIT_ERROR(200004,"数据源初始化失败"),
    INVALID_VERSION(200005,"无效的版本数据")
    ;

    private int code;
    private String message;

    HdsResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
