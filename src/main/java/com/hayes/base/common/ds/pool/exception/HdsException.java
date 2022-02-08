package com.hayes.base.common.ds.pool.exception;

import com.hayes.base.common.core.result.ResultCode;

/**
 * @program: hayes-common-redis
 * @Class HdsException
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-01-29 10:47
 **/
public class HdsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    public HdsException() {
    }

    public HdsException(String message) {
        this.message = message;
    }


    public HdsException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public HdsException(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public HdsException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
