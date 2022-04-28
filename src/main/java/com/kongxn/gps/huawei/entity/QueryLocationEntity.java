package com.kongxn.gps.huawei.entity;

import com.kongxn.gps.entity.QueryLocationInterface;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.util.Date;

@Data
@Log4j2
public class QueryLocationEntity implements QueryLocationInterface {

    private String code;
    private String info;
    private String exeResult;
    private long executeTime;
    private long currentTime;
    private LocateInfoEntity locateInfo;

    @Override
    public double getLatitude() {
        return locateInfo.latitude;
    }

    @Override
    public double getLongitude() {
        return locateInfo.longitude;
    }

    @Override
    public float getLatitude_WGS() {
        return locateInfo.latitude_WGS;
    }

    @Override
    public float getLongitude_WGS() {
        return locateInfo.longitude_WGS;
    }

    @Override
    public long getQueryTime() {
        return currentTime;
    }

    @Override
    public Date getExecuteDate() {
        try {
            return SDF.parse(SDF.format(executeTime));
        } catch (ParseException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public Date getQueryDate() {
        try {
            return SDF.parse(SDF.format(currentTime));
        } catch (ParseException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public String getIsCharging() {
        return locateInfo.batteryStatus.isCharging;
    }

    @Override
    public String getPercentage() {
        return locateInfo.batteryStatus.percentage;
    }

    @Override
    public double getAccuracy() {
        return locateInfo.accuracy;
    }

    @Override
    public String getNetworkName() {
        return locateInfo.networkInfo.name;
    }

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
