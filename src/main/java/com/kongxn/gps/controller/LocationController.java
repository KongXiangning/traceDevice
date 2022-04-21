package com.kongxn.gps.controller;

import com.kongxn.gps.selenium.WebDriverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emp/*")
public class LocationController {

    private final WebDriverFactory webDriverFactory;

    public LocationController(WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    @RequestMapping("/getlocation")
    public String getLocation(){
        System.out.println(webDriverFactory.getLocalProxyIp());
        return "success";
    }
}
