package com.kongxn.gps.selenium;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Starter implements CommandLineRunner {

    private final TraceGps traceGps;

    public Starter(TraceGps traceGps) {
        this.traceGps = traceGps;
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("starter");
        traceGps.start();
        System.out.println("end");
    }
}
