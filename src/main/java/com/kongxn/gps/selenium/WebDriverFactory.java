package com.kongxn.gps.selenium;

import lombok.Data;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Component
@ConfigurationProperties(prefix = "webdriver")
public class WebDriverFactory {

    private static int portIndex = 0;
    /**
     * 远程游览器地址
     * */
    private String remoteWebDriverAddr;
    /**
     * 该IP需要能被远程游览器访问
     * */
    private String localProxyIp;

    private ChromeOptions options;
    private Random random = new Random();

    public WebDriverFactory(){
        options = new ChromeOptions();
        options.addArguments("--no-sandbox")
//                .addArguments("--headless") //代理会出错
                .addArguments("--disable-gpu")
                .addArguments("--disable-dev-shm-usage")
                .addArguments("blink-settings=imagesEnabled=false")
                .addArguments("--ignore-certificate-errors")
                .addArguments("user-agent=\"User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36\"");
    }


    public WebDriver instance(Capabilities capabilities) throws MalformedURLException {
        return new RemoteWebDriver(new URL(remoteWebDriverAddr),capabilities);
    }

    public Driver instance() throws MalformedURLException {
        return new Driver(instance(options), null);
    }

    public Driver instanceProxy() throws MalformedURLException {
        int port = 29900;
        synchronized (this){
            if (portIndex <= 99){
                port += portIndex++;
            }else {
                portIndex = 0;
            }
        }
        BrowserMobProxyServer proxyServer = new BrowserMobProxyServer();
        proxyServer.start(port);

        DesiredCapabilities browsers = new DesiredCapabilities();
        browsers.setCapability(ChromeOptions.CAPABILITY,options);

        browsers.setCapability("acceptInsecureCerts",true);
        browsers.setCapability(CapabilityType.PROXY, createProxy(port));

        return new Driver(instance(browsers), proxyServer);
    }

    private Proxy createProxy(int port){
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(new InetSocketAddress(localProxyIp,port));
        String proxyPath = localProxyIp + ":" + port;
        return seleniumProxy.setHttpProxy(proxyPath).setSslProxy(proxyPath);
    }
}
