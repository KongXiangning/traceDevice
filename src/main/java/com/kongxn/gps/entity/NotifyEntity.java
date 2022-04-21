package com.kongxn.gps.entity;

import lombok.Data;

@Data
public class NotifyEntity {

    private String traceId;
    private String tag;
    private final String module = "findPhone";
}
