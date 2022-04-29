package com.kongxn.gps.selenium;

import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.huawei.CloudHuaweiSelenium;
import com.kongxn.gps.selenium.api.SeleniumAppInterface;
import com.kongxn.gps.webclient.ServiceRequestManager;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;

@Component
public class SeleniumAppFactory {

    private final ServiceRequestManager serviceRequestManager;
    private final WebDriverFactory webDriverFactory;

    public SeleniumAppFactory(ServiceRequestManager serviceRequestManager, WebDriverFactory webDriverFactory) {
        this.serviceRequestManager = serviceRequestManager;
        this.webDriverFactory = webDriverFactory;
    }

    public SeleniumAppInterface getSelenium(AccountEntity accountEntity) throws MalformedURLException {
        if ("huawei".equals(accountEntity.getPlatform())){
            return new CloudHuaweiSelenium(webDriverFactory.instanceProxy(),serviceRequestManager,accountEntity);
        }
        return null;
    }
}
