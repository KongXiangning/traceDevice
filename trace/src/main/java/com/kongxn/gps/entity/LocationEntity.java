package com.kongxn.gps.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "location_log")
public class LocationEntity {

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

    public LocationEntity(String platform, String username, String phoneId, QueryLocationInterface entity) {
        this.platform = platform;
        this.username = username;
        this.phoneId = phoneId;
        latitude = entity.getLatitude();
        longitude = entity.getLongitude();
        latitudeWgs = entity.getLatitude_WGS();
        longitudeWgs = entity.getLongitude_WGS();
        executeTime = entity.getExecuteTime();
        queryTime = entity.getQueryTime();
        executeDate = entity.getExecuteDate();
        queryDate = entity.getQueryDate();

        isCharging = entity.getIsCharging();
        percentage = entity.getPercentage();
        accuracy = entity.getAccuracy();
        network = entity.getNetworkName();
    }

    public LocationEntity() {
        
    }
}
