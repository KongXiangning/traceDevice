package com.kongxn.gps.huawei;

import com.google.gson.Gson;
import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.entity.LocationEntity;
import com.kongxn.gps.exceptions.TraceException;
import com.kongxn.gps.huawei.entity.NotifyEntity;
import com.kongxn.gps.huawei.entity.QueryEntity;
import com.kongxn.gps.huawei.entity.QueryLocationEntity;
import com.kongxn.gps.repository.LocationRepository;
import com.kongxn.gps.tools.GpsTool;
import com.kongxn.gps.webclient.AbstractWebClient;
import com.kongxn.gps.webclient.WebClientStatus;
import lombok.extern.log4j.Log4j2;
import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarNameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.kongxn.gps.huawei.Constants.*;

@Log4j2
public class HuaweiClient extends AbstractWebClient {

    private WebClientStatus webClientStatus;

    //保存请求头数据
    private Map<String, List<HarNameValuePair>> headerMap = new HashMap<>();
    //保存请求cookie数据
    private Map<String,List<HarCookie>> cookieMap = new HashMap<>();
    private String CSRFToken = "";
    private String JSESSIONID = "";
    private String traceId;

    private Lock lock = new ReentrantLock();
    private Random random = new Random();
    private Gson gson = new Gson();

    private boolean switchGSM = false;
    private int locateInterval = 10;
    private boolean startHeartBeat = true;
    private Semaphore startHeartBeatSP = new Semaphore(1);

    //请求实体
    private NotifyEntity notify = new NotifyEntity();
    private QueryEntity query = new QueryEntity();

    public HuaweiClient(WebClient webClient, LocationRepository locationRepository, AccountEntity accountEntity) {
        super(webClient, locationRepository, accountEntity);
    }

    //todo 异常处理，重登
    @Override
    public void keepalive(){
        startHeartBeatSP.tryAcquire();
        //刷新jessionid
        executorService.execute(() -> {
            Future<String> future;
            while (runStatus){
                future = executorService.submit(this::getNotify);
                try {
                    future.get(5, TimeUnit.MINUTES);
                    if (startHeartBeat){
                        startHeartBeat = false;
                        startHeartBeatSP.release();
                    }
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    log.error(e);
                }
            }
        });
        //刷新token
        executorService.execute(() -> {
            try {
                startHeartBeatSP.tryAcquire(3, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error(e);
            }
            while (runStatus){
                try {
                    executorService.execute(this::getHeartbeatCheck);
                    Thread.sleep(61*1000);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        });

        //刷新手机定位
        executorService.execute(() -> {
            while (runStatus){
                try {
                    executorService.execute(this::locate);
                    int index = this.locateInterval;
                    while (index > 0){
                        index--;
                        Thread.sleep(30000);
                        if (switchGSM){
                            switchGSM = false;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        });

    }

    @Override
    public int calInterval(LocationEntity pre, LocationEntity current) {
        if (current.getNetwork().contains("中国")){
            if (!pre.getNetwork().contains("中国")){
                switchGSM = true;
                this.locateInterval = MIN_INTERVAL;
            }else {
                BigDecimal distance = BigDecimal.valueOf(GpsTool.calDistance(pre.getLatitude(), pre.getLongitude(), current.getLatitude(), current.getLongitude()));
                if (distance.longValue() > 80){
                    this.locateInterval = MIN_INTERVAL;
                }else {
                    this.locateInterval += 1;
                    if (this.locateInterval > 10){
                        this.locateInterval = 10;
                    }
                }
            }
        }else {
            this.locateInterval = MAX_INTERVAL;
        }
        return locateInterval;
    }


    @Override
    public void reset() {
        CSRFToken = "";
        JSESSIONID = "";
        startHeartBeat = true;
        startHeartBeatSP.release();
    }

    @Override
    public WebClientStatus status() {
        return webClientStatus;
    }

    private String getNotify(){
        return webClient.post().uri(NOTIFY_URL)
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
                        log.error(e);
                    }finally {
                        lock.unlock();
                    }
                    rs.releaseBody();
                    return rs.bodyToMono(String.class);
                })
                .map(body -> {
                    setNotifyTag(gson.fromJson(body, Map.class).get("tag").toString());
                    return body;
                })
                .onErrorResume(err -> {
                    log.error(err);
                    errorCount.incrementAndGet();
                    throw Exceptions.propagate(TraceException.newError(PLATFORM_NAME, accountEntity.getUsername(), accountEntity.getDeviceId(), err.getMessage()));
                })
                .block();
    }

    private String genNotifyBody(){
        notify.setTraceId("07100"+getTraceId());
        return gson.toJson(notify);
    }

    public void getHeartbeatCheck(){
        webClient.get().uri(HEART_CHECK_URL + "traceId=07100" + getTraceId())
                .cookies(analysisCookies(HEARTBEAT_CHECK))
                .headers(analysisHeader(HEARTBEAT_CHECK))
                .exchangeToMono(rs -> {
                    if (rs.statusCode() != HttpStatus.OK){
                        return Mono.error(TraceException.newError(PLATFORM_NAME, accountEntity.getUsername(), accountEntity.getDeviceId(), "heartbeatCheck httpCode :"+rs.statusCode()));
                    }
                    lock.lock();
                    try {
                        updateJSESSIONID(rs.cookies());
                        updateCSRFToken(rs.cookies());
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                    rs.releaseBody();
                    return Mono.just("success");
                })
                .onErrorResume(err -> {
                    log.error(err);
                    errorCount.incrementAndGet();
                    throw Exceptions.propagate(TraceException.newError(PLATFORM_NAME, accountEntity.getUsername(), accountEntity.getDeviceId(), err.getMessage()));
                })
                .block();
    }

    /**
     * 刷新手机定位请求，不返回位置信息
     * */
    private void locate(){
        webClient.post().uri(LOCATE_URL)
                .cookies(analysisCookies(QUERY_LOCATE_RESULT))
                .headers(analysisHeader(QUERY_LOCATE_RESULT))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(genQueryBody()))
                .exchangeToMono(rs -> {
                    return rs.bodyToMono(String.class);
                })
                .map(body -> {
                    return body;
                })
                .onErrorResume(err -> {
                    log.error(err);
                    errorCount.incrementAndGet();
                    throw Exceptions.propagate(TraceException.newError(PLATFORM_NAME, accountEntity.getUsername(), accountEntity.getDeviceId(), err.getMessage()));
                })
                .block();
    }

    @Override
    public Mono<LocationEntity> queryLocate(){
        return webClient.post().uri(QUERY_LOCATE_URL)
                .cookies(analysisCookies(QUERY_LOCATE_RESULT))
                .headers(analysisHeader(QUERY_LOCATE_RESULT))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(genQueryBody()))
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> {
                    while (body.contains("\\\"")){
                        body = body.replace("\\\"","\"");
                    }
                    String jsonStr = body.replace("\"{","{").replace("}\"","}");
                    QueryLocationEntity entity = gson.fromJson(jsonStr, QueryLocationEntity.class);
                    return entity;
                })
                .map(entity -> {
                    return new LocationEntity(PLATFORM_NAME, accountEntity.getUsername(), accountEntity.getDeviceId(), entity);
                });
    }

    private String genQueryBody(){
        if (traceId != null && !traceId.isEmpty()){
            query.setTraceId("01001_02"+getTraceId()+traceId);
        }else {
            query.setTraceId("01001_02"+getTraceId());
        }
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

    public void setQueryEntity(Map<String,Object> queryMap,String traceId){
        query.setDeviceId(String.valueOf(queryMap.get("deviceId")));
        query.setDeviceType(((Double)queryMap.get("deviceType")).intValue());
        query.setEndpointCrypted(String.valueOf(queryMap.get("endpointCrypted")));
        query.setPerDeviceType(String.valueOf(queryMap.get("perDeviceType")));
        query.setSequence(((Double)queryMap.get("sequence")).intValue());
        this.traceId = traceId;
    }

    private void updateJSESSIONID(MultiValueMap<String, ResponseCookie> cookies){
        if (cookies.getFirst("JSESSIONID") != null){
            JSESSIONID = Objects.requireNonNull(cookies.getFirst("JSESSIONID")).getValue();
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
