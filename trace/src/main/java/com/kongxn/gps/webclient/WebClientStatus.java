package com.kongxn.gps.webclient;


@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum WebClientStatus {

    RUN("run","运行中"),
    STOP("stop","停止"),
    ERR("err","异常");
    private final String status;
    private final String describe;


    WebClientStatus(String status, String describe) {
        this.status = status;
        this.describe = describe;
    }

    public String getStatus(){
        return status;
    }
}
