package com.kongxn.gps.controller;

import com.google.gson.Gson;
import com.kongxn.gps.entity.LocationEntity;
import com.kongxn.gps.repository.LocationRepository;
import com.kongxn.gps.selenium.WebDriverFactory;
import com.kongxn.gps.tools.CoordinateTransferUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/geo/")
public class LocationController {

    private final LocationRepository locationRepository;
    private final Gson gson = new Gson();

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @RequestMapping("/getlocation/{platform}/{username}")
    public String getLocation(@PathVariable("platform") String platform,@PathVariable("username") String username){
        LocationEntity locationEntity = locationRepository.getLastLocation(platform,username);
        return gson.toJson(locationEntity);
    }

    @RequestMapping("/getlocation/wgs84/{platform}/{username}")
    public String getLocationWgs84(@PathVariable("platform") String platform,@PathVariable("username") String username){
        LocationEntity locationEntity = locationRepository.getLastLocation(platform,username);
        if ("huawei".equals(platform)){
            Map<String,Double> transformRes = CoordinateTransferUtils.bd09ToWgs84(locationEntity.getLongitude(), locationEntity.getLatitude());
            locationEntity.setLongitude(transformRes.get("lon"));
            locationEntity.setLatitude(transformRes.get("lat"));
        }
        return gson.toJson(locationEntity);
    }
}
