package com.kongxn.gps.selenium;

import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.selenium.api.SeleniumAppInterface;
import com.kongxn.gps.webclient.ServiceRequestManager;

public abstract class AbstractSeleniumAppImpl implements SeleniumAppInterface {

    protected Driver driver;
    protected ServiceRequestManager serviceRequestManager;
    protected AccountEntity accountEntity;

    public AbstractSeleniumAppImpl(Driver driver, ServiceRequestManager serviceRequestManager, AccountEntity accountEntity){
        this.driver = driver;
        this.serviceRequestManager = serviceRequestManager;
        this.accountEntity = accountEntity;
    }
}
