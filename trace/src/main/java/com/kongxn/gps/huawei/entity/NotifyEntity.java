package com.kongxn.gps.huawei.entity;

import lombok.Data;

@Data
public class NotifyEntity {

    private String traceId;
    private String tag;
    private final String module = "findPhone";
}
