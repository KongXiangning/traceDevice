package com.kongxn.gps.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface QueryLocationInterface {

    static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    double getLatitude();
    double getLongitude();
    float getLatitude_WGS();
    float getLongitude_WGS();
    long getExecuteTime();
    long getQueryTime();
    Date getExecuteDate();
    Date getQueryDate();
    String getIsCharging();
    String getPercentage();
    double getAccuracy();
    String getNetworkName();
}
