package com.kongxn.gps.huawei;

import com.google.gson.Gson;
import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.selenium.AbstractSeleniumAppImpl;
import com.kongxn.gps.selenium.Driver;
import com.kongxn.gps.selenium.TraceGps;
import com.kongxn.gps.webclient.ServiceRequestManager;
import lombok.extern.log4j.Log4j2;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.kongxn.gps.huawei.Constants.*;

@Log4j2
public class CloudHuaweiSelenium extends AbstractSeleniumAppImpl {

    Gson gson = new Gson();
    private HuaweiClient client;

    public CloudHuaweiSelenium(Driver driver, ServiceRequestManager serviceRequestManager, AccountEntity accountEntity) {
        super(driver,serviceRequestManager,accountEntity);
    }

    @Override
    public boolean start() {
        init();
        try {
            while (!login()){
                Thread.sleep(5000);
            }
            Thread.sleep(10000);
            if (findDevices() && getToken()){
                client.start();
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        }finally {
            driver.close();
        }
        return false;
    }

    private boolean login() {
        try {
            driver.getWebDriver().get(CLOUD_URL);
            Thread.sleep(1000);
            changeLanguage();
            driver.getWebDriver().switchTo().frame("frameAddress");
            driver.findElementByXpath("//input[contains(@class,'hwid-input userAccount')]").sendKeys(accountEntity.getUsername());
            driver.findElementByXpath("//input[@class='hwid-input hwid-input-pwd']").sendKeys(accountEntity.getPassword());
            Thread.sleep(1000);
            driver.findElementByXpath("//div[@class='normalBtn']").click();
            Thread.sleep(2500);

            if ( driver.findElementByXpath("//div[@id='userName']") != null){
                log.info("Account Login succeeded!: {}",accountEntity.getUsername());
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean findDevices(){
        try {
            driver.getProxyServer().newHar("getGps");
            driver.findElementByXpath("//div[@class='warpHome mobile']").click();
            Thread.sleep(10000);
            List<WebElement> elements = driver.findElementByXpaths("//div[@class='device_list_item']//span[@class='device_name']");
            if (elements != null && elements.size() > 0){
                WebElement element = elements.get(0);
                element.click();
                for (int index = 0; index < elements.size();) {
                    if (driver.findElementByXpath("//div[@class='header_name_item']") == null){
                        log.error("选择设备失败");
                        return false;
                    }else if (driver.findElementByXpath("//div[@class='header_name_item']").getText().equals(accountEntity.getDeviceId())){
                        return true;
                    }else {
                        driver.findElementByXpath("//div[@class='current_device']//span[@class='device_name']").click();
                        List<WebElement> tmps = driver.findElementByXpaths("//div[@class='device_list_item']//span[@class='device_name']");
                        tmps.get(++index).click();
                    }
                }
            }
            if (driver.findElementByXpath("//div[@class='header_name_item']") == null){
                log.error("选择设备失败");
                return false;
            }
            return true;
        }catch (Exception e){
            log.error(e);
            TraceGps.restart(accountEntity);
            return false;
        }
    }

    /**
     * 获取请求中的token/JSESSIONID/
     * */
    private boolean getToken() throws Exception {
        Har har = driver.getProxyServer().endHar();
        List<HarEntry> entries = har.getLog().getEntries();

        //只读最新的一个请求
        boolean notifyState = false,heartbeatCheck = false,queryLocateState = false;
        client = serviceRequestManager.getWebClient(accountEntity, HuaweiClient.class);
        for (int i = entries.size()-1 ; i >= 0; i--) {
            HarEntry entry = entries.get(i);
            if (entry.getRequest().getUrl().contains(NOTIFY) && !notifyState){
                notifyState = true;
                List<HarCookie> cookies = entry.getRequest().getCookies();
                List<HarNameValuePair> headers = entry.getRequest().getHeaders();
                client.addRequestInfo(NOTIFY, headers,cookies);
                if (entry.getResponse().getStatus() == 0){
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
            if (entry.getRequest().getUrl().contains(HEARTBEAT_CHECK) && !heartbeatCheck){
                heartbeatCheck = true;
                List<HarCookie> cookies = entry.getRequest().getCookies();
                List<HarNameValuePair> headers = entry.getRequest().getHeaders();
                client.addRequestInfo(HEARTBEAT_CHECK, headers,cookies);
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
            if (entry.getRequest().getUrl().contains(QUERY_LOCATE_RESULT) && !queryLocateState){
                String traceId = "";
                Map<String,Object> body = gson.fromJson(entry.getRequest().getPostData().getText(), Map.class);

                if (body.get("traceId") == null || body.get("traceId").toString().isEmpty()){
                    continue;
                }
                String tempTraceId = body.get("traceId").toString();
                if (tempTraceId.length() > 28){
                    traceId = tempTraceId.substring(28);
                }
                log.info("设备请求信息记录成功:{}",traceId);
                queryLocateState = true;
                List<HarCookie> cookies = entry.getRequest().getCookies();
                List<HarNameValuePair> headers = entry.getRequest().getHeaders();
                client.addRequestInfo(QUERY_LOCATE_RESULT, headers,cookies);
                client.setQueryEntity(body, traceId);
            }
        }
        return true;
    }

    private void changeLanguage() throws InterruptedException {
        if (!driver.getWebDriver().getTitle().contains("华为")){
            String js = "div = document.getElementById('wrapper');div.style.overflow='visible';div.style.top='-2700px';";
            ((JavascriptExecutor)driver.getWebDriver()).executeScript(js);
            driver.getWebDriver().findElement(By.xpath("//span[contains(@class,'langBtn')]")).click();
            driver.getWebDriver().findElement(By.xpath("//li[@code='zh-cn']")).click();
            Thread.sleep(1000);
        }
        if (driver.getWebDriver().getTitle().contains("华为")){
            log.info("The language is " + driver.getWebDriver().findElement(By.xpath("//span[contains(@class,'langBtn')]")).getText());
        }
    }


    private void init(){
        WebDriver webDriver = driver.getWebDriver();
        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        driver.getProxyServer().setHarCaptureTypes(CaptureType.REQUEST_HEADERS,
                CaptureType.REQUEST_COOKIES,
                CaptureType.RESPONSE_HEADERS,
                CaptureType.RESPONSE_COOKIES,
                CaptureType.RESPONSE_CONTENT,
                CaptureType.REQUEST_CONTENT);
    }

}
