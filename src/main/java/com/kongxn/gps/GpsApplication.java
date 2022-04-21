package com.kongxn.gps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsApplication {

    public static void main(String[] args) throws Exception {
//        Connection connection = new Connection();
//        connection.init();
        /*Random random = new Random();
        StringBuilder rd = new StringBuilder("_02_");
        rd.append(System.currentTimeMillis()/1000+"_");
        for (int i = 0; i < 8; i++) {
            rd.append(1 + random.nextInt(8));
        }
        System.out.println(rd);*/
        SpringApplication.run(GpsApplication.class, args);

    }

}
