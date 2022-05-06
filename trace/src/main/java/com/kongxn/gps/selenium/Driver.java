package com.kongxn.gps.selenium;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Data
@AllArgsConstructor
public class Driver {

    private WebDriver webDriver;
    private BrowserMobProxyServer proxyServer;
    private boolean status = true;

    public Driver(WebDriver webDriver, BrowserMobProxyServer proxyServer) {
        this.webDriver = webDriver;
        this.proxyServer = proxyServer;
    }

    public void close(){
        status = false;
        if (!proxyServer.isStopped()){
            webDriver.close();
            webDriver.quit();
            proxyServer.stop();
        }
        TraceGps.startThread.release();
    }

    public boolean isRun(){
        return status;
    }

    public WebElement findElementByXpath(String xpathExpression){
        WebElement webElement;
        try {
            webElement = webDriver.findElement(By.xpath(xpathExpression));
        }catch (Exception e){
            webElement = null;
        }
        return webElement;
    }

    public List<WebElement> findElementByXpaths(String xpathExpression){
        List<WebElement> webElements;
        try {
            webElements = webDriver.findElements(By.xpath(xpathExpression));
        }catch (Exception e){
            webElements = null;
        }
        return webElements;
    }
}
