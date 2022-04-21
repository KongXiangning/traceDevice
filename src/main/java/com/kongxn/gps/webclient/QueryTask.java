package com.kongxn.gps.webclient;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableScheduling
public class QueryTask {

    private List<String> keys = new ArrayList<>();
    private final ServiceRequestManager serviceRequestManager;

    public QueryTask(ServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    public void addTaskKey(String key){
        keys.add(key);
    }

    /*@Scheduled(cron = "13 0/3 * * * ?")
    public void getNotify(){
        System.out.println(keys.size());
        for (String key : keys) {
            serviceRequestManager.getWebClient(key).executorNotify();
        }
    }*/

    /*@Scheduled(cron = "1 0/1 * * * ?")
    public void heartbeatCheck(){
        System.out.println(keys.size());
        for (String key : keys) {
            serviceRequestManager.getWebClient(key).getHeartbeatCheck();
        }
    }*/
}
