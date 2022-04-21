package com.kongxn.gps.webclient;

public interface ServiceRequestManager {

    Client getWebClient(String clientId,String url);

    Client getWebClient(String clientId);
}
