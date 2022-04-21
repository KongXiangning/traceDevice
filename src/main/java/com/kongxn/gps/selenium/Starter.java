package com.kongxn.gps.selenium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Starter implements CommandLineRunner {

    private final HuaweiGps huaweiGps;

    public Starter(HuaweiGps huaweiGps) {
        this.huaweiGps = huaweiGps;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("starter");
        huaweiGps.init();
//        Connection connection = new Connection();
//        connection.init();
        System.out.println("end");
    }
}
