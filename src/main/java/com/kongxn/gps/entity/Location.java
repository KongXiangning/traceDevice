package com.kongxn.gps.entity;

import lombok.Data;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Entity(name = "location_log")
public class Location {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String platform;
    private String username;
    private String phoneId;
    private double latitude;
    @Column(nullable=true)
    private float latitudeWgs;
    private double longitude;
    @Column(nullable=true)
    private float longitudeWgs;
    private long executeTime;
    private long queryTime;
    private Date executeDate;
    private Date queryDate;
    private String isCharging;
    private String percentage;
    private String network;
    private double accuracy;

    public Location(String platform, String username,String phoneId,QueryLocationEntity entity) {
        this.platform = platform;
        this.username = username;
        this.phoneId = phoneId;
        latitude = entity.getLocateInfo().getLatitude();
        longitude = entity.getLocateInfo().getLongitude();
        latitudeWgs = entity.getLocateInfo().getLatitude_WGS();
        longitudeWgs = entity.getLocateInfo().getLongitude_WGS();
        executeTime = entity.getExecuteTime();
        queryTime = entity.getCurrentTime();
        try {
            executeDate = sdf.parse(sdf.format(entity.getExecuteTime()));
            queryDate = sdf.parse(sdf.format(entity.getCurrentTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        isCharging = entity.getLocateInfo().getBatteryStatus().getIsCharging();
        percentage = entity.getLocateInfo().getBatteryStatus().getPercentage();
        accuracy = entity.getLocateInfo().getAccuracy();
        network = entity.getLocateInfo().getNetworkInfo().getName();
    }

    public Location() {
        
    }
}
