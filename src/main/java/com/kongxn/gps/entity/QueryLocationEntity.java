package com.kongxn.gps.entity;

import lombok.Data;

@Data
public class QueryLocationEntity {

    private String code;
    private String info;
    private String exeResult;
    private long executeTime;
    private long currentTime;
    private LocateInfoEntity locateInfo;

    @Data
    public static class LocateInfoEntity{
        private double accuracy;
        private BatteryEntity batteryStatus;
        private String country;
        private double createTime;
        private double isLockScreen;
        private double latitude;
        private float latitude_WGS;
        private double longitude;
        private float longitude_WGS;
        private NetworkInfoEntity networkInfo;
        private double type;
    }


    @Data
    public static class BatteryEntity{
        private String isCharging;
        private String percentage;
    }

    @Data
    public static class NetworkInfoEntity{
        private String name;
        private String signal;
        private String type;
    }
}
