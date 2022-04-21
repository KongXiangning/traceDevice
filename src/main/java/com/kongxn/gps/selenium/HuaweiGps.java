package com.kongxn.gps.selenium;

import com.google.gson.Gson;
import com.kongxn.gps.Constants;
import com.kongxn.gps.webclient.Client;
import com.kongxn.gps.webclient.QueryTask;
import com.kongxn.gps.webclient.ServiceRequestManager;
import lombok.Data;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "huawei")
public class HuaweiGps {

    private String url = "https://cloud.huawei.com/";
    private String username;
    private String password;
    private String phoneId;

    private final ServiceRequestManager serviceRequestManager;
    private final WebDriverFactory webDriverFactory;
    private final QueryTask queryTask;


    public HuaweiGps(WebDriverFactory webDriverFactory, ServiceRequestManager serviceRequestManager, QueryTask queryTask) {
        this.webDriverFactory = webDriverFactory;
        this.serviceRequestManager = serviceRequestManager;
        this.queryTask = queryTask;
    }

    public void init() throws MalformedURLException {
        Driver driver = webDriverFactory.instanceProxy();
        try {
            WebDriver webDriver = driver.getWebDriver();
            webDriver.manage().window().maximize();
            webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
//            driver.getProxyServer().newHar("getGps");
            driver.getProxyServer().setHarCaptureTypes(CaptureType.REQUEST_HEADERS,
                    CaptureType.REQUEST_COOKIES,
                    CaptureType.RESPONSE_HEADERS,
                    CaptureType.RESPONSE_COOKIES,
                    CaptureType.RESPONSE_CONTENT,
                    CaptureType.REQUEST_CONTENT);

            while (!login(driver)){
                Thread.sleep(5000);
            }
            if (findDevices(driver)){
                System.out.println("begin findDevices");
                Thread.sleep(10000);
                Har har = driver.getProxyServer().endHar();
                driver.close();
                List<HarEntry> entries = har.getLog().getEntries();
                System.out.println("har size:"+entries.size());
                boolean notifyState = false,heartbeatCheck = false,queryLocateState = false;
                Client client = serviceRequestManager.getWebClient(username, "https://cloud.huawei.com/");
                for (int i = entries.size()-1 ; i >= 0; i--) {
                    HarEntry entry = entries.get(i);
                    if ("https://cloud.huawei.com/notify".equals(entry.getRequest().getUrl()) && !notifyState){
                        notifyState = true;
                        List<HarCookie> cookies = entry.getRequest().getCookies();
                        List<HarNameValuePair> headers = entry.getRequest().getHeaders();
                        client.addRequestInfo(Constants.NOTIFY, headers,cookies);
                        if (entry.getResponse().getStatus() == 0){
                            Gson gson = new Gson();
                            Map<String,String> body = gson.fromJson(entry.getRequest().getPostData().getText(), Map.class);
                            client.setNotifyTag(body.get("tag"));
                            for (HarCookie cookie : cookies) {
                                if ("CSRFToken".equals(cookie.getName())){
                                    client.setCSRFToken(cookie.getValue());
                                }
                                if ("JSESSIONID".equals(cookie.getName())){
                                    client.setJSESSIONID(cookie.getValue());
                                }
                            }
                        }

                    }
                    if (entry.getRequest().getUrl().contains("heartbeatCheck") && !heartbeatCheck){
                        heartbeatCheck = true;
                        List<HarCookie> cookies = entry.getRequest().getCookies();
                        List<HarNameValuePair> headers = entry.getRequest().getHeaders();
                        client.addRequestInfo(Constants.HEART, headers,cookies);
                        if (entry.getResponse().getStatus() == 0){
                            for (HarCookie cookie : cookies) {
                                if ("CSRFToken".equals(cookie.getName())){
                                    client.setCSRFToken(cookie.getValue());
                                }
                                if ("JSESSIONID".equals(cookie.getName())){
                                    client.setJSESSIONID(cookie.getValue());
                                }
                            }
                        }
                    }
                    if (entry.getRequest().getUrl().contains("queryLocateResult") && !queryLocateState){
                        queryLocateState = true;
                        List<HarCookie> cookies = entry.getRequest().getCookies();
                        List<HarNameValuePair> headers = entry.getRequest().getHeaders();
                        client.addRequestInfo(Constants.QUERY, headers,cookies);
                        Gson gson = new Gson();
                        Map<String,Object> body = gson.fromJson(entry.getRequest().getPostData().getText(), Map.class);
                        client.setQueryEntity(body, phoneId, username);
                    }


                }
                client.executorNotify();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            driver.close();
        }
    }

    private boolean login(Driver driver){
        try {
            driver.getWebDriver().get(url);
            Thread.sleep(1000);
            changeLanguage(driver);
            driver.getWebDriver().switchTo().frame("frameAddress");
            driver.findElementByXpath("//input[contains(@class,'hwid-input userAccount')]").sendKeys(username);
            driver.findElementByXpath("//input[@class='hwid-input hwid-input-pwd']").sendKeys(password);
            Thread.sleep(1000);
            driver.findElementByXpath("//div[@class='normalBtn']").click();
            Thread.sleep(2500);

            if ( driver.findElementByXpath("//div[@id='userName']") != null){
                System.out.println("Account Login succeeded! : " + username);
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private void changeLanguage(Driver driver) throws InterruptedException {
        if (!driver.getWebDriver().getTitle().contains("华为")){
            System.out.println("changeLanguage");
            String js = "div = document.getElementById('wrapper');div.style.overflow='visible';div.style.top='-2700px';";
            ((JavascriptExecutor)driver.getWebDriver()).executeScript(js);
            System.out.println("executor success");
            driver.getWebDriver().findElement(By.xpath("//span[contains(@class,'langBtn')]")).click();
            driver.getWebDriver().findElement(By.xpath("//li[@code='zh-cn']")).click();
            Thread.sleep(1000);
        }
        if (driver.getWebDriver().getTitle().contains("华为")){
            System.out.println("The language is " + driver.getWebDriver().findElement(By.xpath("//span[contains(@class,'langBtn')]")).getText());
        }
    }

    private boolean findDevices(Driver driver){
        System.out.println("findDevices start");
        try {
            driver.getProxyServer().newHar("getGps");
            driver.findElementByXpath("//div[@class='warpHome mobile']").click();
            Thread.sleep(10000);
            List<WebElement> elements = driver.findElementByXpaths("//div[@class='device_item']");
            if (elements != null && elements.size() > 0){
                elements.get(0).click();
                Thread.sleep(10000);
            }
            if (driver.findElementByXpath("//div[@class='header_name_item']") == null){
                System.out.println("选择设备失败");
                return false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
