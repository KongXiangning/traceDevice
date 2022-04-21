package com.kongxn.gps.entity;

import lombok.Data;

@Data
public class QueryEntity {

    private String deviceId;
    private int deviceType;
    private String endpointCrypted;
    private String perDeviceType;
    private int sequence;
    private String traceId;
}
