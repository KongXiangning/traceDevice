package com.kongxn.gps.webclient;

import com.google.gson.Gson;
import com.kongxn.gps.entity.Location;
import com.kongxn.gps.entity.NotifyEntity;
import com.kongxn.gps.entity.QueryEntity;
import com.kongxn.gps.entity.QueryLocationEntity;
import com.kongxn.gps.repository.LocationRepository;
import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarNameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.kongxn.gps.Constants.*;

public class Client {

    private WebClient webClient;
    private Map<String,List<HarNameValuePair>> headerMap = new HashMap<>();
    private Map<String,List<HarCookie>> cookieMap = new HashMap<>();
    private String CSRFToken = "";
    private String JSESSIONID = "";
    private String phoneId;
    private String userName;

    private Lock lock = new ReentrantLock();
    private Random random = new Random();
    private Gson gson = new Gson();

    private NotifyEntity notify = new NotifyEntity();
    private QueryEntity query = new QueryEntity();
    private Boolean heartBegin = true;

    private ExecutorService executorService = new ThreadPoolExecutor(3, 10, 1000, TimeUnit.MILLISECONDS,new SynchronousQueue<Runnable>());
    private LocationRepository locationRepository;

    public Client(WebClient webClient,LocationRepository locationRepository){
        this.webClient = webClient;
        this.locationRepository = locationRepository;
    }

    public void executorNotify(){
        executorService.execute(this::getNotify);
    }

    private void getNotify(){
        webClient.post().uri(NOTIFY_URL)
                .cookies(analysisCookies(NOTIFY))
                .headers(analysisHeader(NOTIFY))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(genNotifyBody()))
                .exchangeToMono(rs -> {
                    lock.lock();
                    try {
                        updateCSRFToken(rs.cookies());
                        updateJSESSIONID(rs.cookies());
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                    rs.releaseBody();
                    return rs.bodyToMono(String.class);
                })
                .map(body -> {
                    System.out.println(body);
                    setNotifyTag(gson.fromJson(body, Map.class).get("tag").toString());
                    return body;
                }).block();
        executorService.execute(this::getNotify);
        if (heartBegin){
            heartBegin = false;
            executorService.execute(this::getHeartbeatCheck);
            executorService.execute(this::queryLocateResult);
//            executorService.execute(this::locate);
        }
        System.out.println("notify-end");
    }

    public void getHeartbeatCheck(){
        String uri = HEART_CHECK_URL + "traceId=07100" + getTraceId();
        webClient.get().uri(uri)
                .cookies(analysisCookies(HEART))
                .headers(analysisHeader(HEART))
                .exchangeToMono(rs -> {
                    lock.lock();
                    try {
                        updateJSESSIONID(rs.cookies());
                        updateCSRFToken(rs.cookies());
                        System.out.println("heartbeatCheck status:"+rs.statusCode());
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                    rs.releaseBody();
                    return Mono.just(rs);
                }).block();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.execute(this::getHeartbeatCheck);
        System.out.println("HeartbeatCheck-end");
    }

    private void queryLocateResult(){
        webClient.post().uri(LOCATE_URL)
                .cookies(analysisCookies(QUERY))
                .headers(analysisHeader(QUERY))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(genQueryBody()))
                .exchangeToMono(rs -> {
                    return rs.bodyToMono(String.class);
                })
                .map(body -> {
                    System.out.println(body);
                    return body;
                }).block();
        webClient.post().uri(QUERY_LOCATE_URL)
                .cookies(analysisCookies(QUERY))
                .headers(analysisHeader(QUERY))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(genQueryBody()))
                .retrieve()
                .bodyToMono(String.class)
                /*.exchangeToMono(rs -> {
                    return rs.bodyToMono(String.class);
                })*/
                .map(body -> {
                    while (body.contains("\\\"")){
                        body = body.replace("\\\"","\"");
                    }
                    String jsonStr = body.replace("\"{","{").replace("}\"","}");
                    QueryLocationEntity entity = gson.fromJson(jsonStr, QueryLocationEntity.class);
                    return entity;
                })
                .map(entity -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date1,date2;
                    try {
                        date1 = sdf.parse(sdf.format(entity.getExecuteTime()));
                        date2 = sdf.parse(sdf.format(entity.getCurrentTime()));
                        System.out.println(entity.toString()+":"+sdf.format(date1)+":"+sdf.format(date2));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return new Location("huawei",userName,phoneId,entity);
                })
                .map(location -> {
                    return locationRepository.save(location);
                }).block();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.execute(this::queryLocateResult);
        System.out.println("queryLocateResult-end");
    }

    /*private void locate(){
        webClient.post().uri(locateUrl)
                .cookies(analysisCookies(QUERY))
                .headers(analysisHeader(QUERY))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(genQueryBody()))
                .exchangeToMono(rs -> {
                    return rs.bodyToMono(String.class);
                })
                .map(body -> {
                    System.out.println(body);
                    return body;
                }).block();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        executorService.execute(this::locate);
    }*/

    private Consumer<HttpHeaders> analysisHeader(String urlType){
        return httpHeaders -> {
            for (HarNameValuePair header : headerMap.get(urlType)) {
                if ("CSRFToken".equals(header.getName())){
                    httpHeaders.add(header.getName(), CSRFToken);
                }else{
                    httpHeaders.add(header.getName(), header.getValue());
                }
            }
            StringBuilder sb = new StringBuilder();
            for (HarCookie cookie : cookieMap.get(urlType)) {
                sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
            }
            httpHeaders.add("Cookie", sb.substring(0, sb.length()-2));
        };
    }

    private Consumer<MultiValueMap<String, String>> analysisCookies(String urlType){
        return cookies -> {
            for (HarCookie cookie : cookieMap.get(urlType)) {
                if ("JSESSIONID".equals(cookie.getName())){
                    cookie.setValue(JSESSIONID);
                }
                if ("CSRFToken".equals(cookie.getName())){
                    cookie.setValue(CSRFToken);
                }
                cookies.add(cookie.getName(),cookie.getValue());
            }
        };
    }

    private String genNotifyBody(){
        notify.setTraceId("07100"+getTraceId());
        return gson.toJson(notify);
    }

    private String genQueryBody(){
        query.setTraceId("01001_02"+getTraceId()+"_"+phoneId);
        return gson.toJson(query);
    }

    private String getTraceId(){
        StringBuilder rd = new StringBuilder("_02_");
        rd.append(System.currentTimeMillis() / 1000).append("_");
        for (int i = 0; i < 8; i++) {
            rd.append(1 + random.nextInt(8));
        }
        return rd.toString();
    }

    private void updateJSESSIONID(MultiValueMap<String, ResponseCookie> cookies){
        if (cookies.getFirst("JSESSIONID") != null){
            JSESSIONID = cookies.getFirst("JSESSIONID").getValue();
        }
    }

    private void updateCSRFToken(MultiValueMap<String, ResponseCookie> cookies){
        if (cookies.getFirst("CSRFToken") != null){
            CSRFToken = cookies.getFirst("CSRFToken").getValue();
        }
    }

    public void addRequestInfo(String urlType,List<HarNameValuePair> headers,List<HarCookie> cookies){
        headerMap.put(urlType, headers);
        cookieMap.put(urlType, cookies);
    }

    public void setNotifyTag(String tag){
        notify.setTag(tag);
    }

    public void setQueryEntity(Map<String,Object> queryMap,String phoneId,String userName){
        query.setDeviceId(String.valueOf(queryMap.get("deviceId")));
        query.setDeviceType(((Double)queryMap.get("deviceType")).intValue());
        query.setEndpointCrypted(String.valueOf(queryMap.get("endpointCrypted")));
        query.setPerDeviceType(String.valueOf(queryMap.get("perDeviceType")));
        query.setSequence(((Double)queryMap.get("sequence")).intValue());
        this.phoneId = phoneId;
        this.userName = userName;
    }

    public void setPhoneId(String phoneId){
        this.phoneId = phoneId;
    }

    public void setCSRFToken(String csrfToken){
        if (CSRFToken.isEmpty()){
            CSRFToken = csrfToken;
        }
    }

    public void setJSESSIONID(String jsessionid){
        if (JSESSIONID.isEmpty()){
            JSESSIONID = jsessionid;
        }
    }
}
