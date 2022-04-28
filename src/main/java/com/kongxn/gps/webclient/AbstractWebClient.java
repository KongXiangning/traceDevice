package com.kongxn.gps.webclient;

import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.entity.LocationEntity;
import com.kongxn.gps.exceptions.TraceException;
import com.kongxn.gps.repository.LocationRepository;
import com.kongxn.gps.selenium.TraceGps;
import com.kongxn.gps.webclient.api.WebClientInterface;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Log4j2
public abstract class AbstractWebClient implements WebClientInterface {

    protected WebClient webClient;
    protected LocationRepository locationRepository;
    protected boolean runStatus = false;
    protected int locateInterval = 1;
    protected AccountEntity accountEntity;

    protected AtomicInteger errorCount = new AtomicInteger(0);

    private LocationEntity preLocation;

    protected static final ExecutorService executorService = new ThreadPoolExecutor(10, 100, 1000, TimeUnit.MILLISECONDS,new SynchronousQueue<Runnable>());

    protected AbstractWebClient(WebClient webClient,LocationRepository locationRepository,AccountEntity accountEntity){
        this.webClient = webClient;
        this.locationRepository = locationRepository;
        this.accountEntity = accountEntity;
    }

    @Override
    public void start(){
        errorCount.set(0);
        runStatus = true;
        keepalive();
        executorService.execute(() -> {
            while (runStatus){
                try {
                    queryLocate()
                            .map(this::dataHandler)
                            .onErrorResume(err -> {
                                log.error(err);
                                errorCount.decrementAndGet();
                                throw Exceptions.propagate(TraceException.newError(accountEntity.getPlatform(), accountEntity.getUsername(), accountEntity.getDeviceId(), err.getMessage()));
                            })
                            .block();
                    if (errorCount.get() > 10){
                        runStatus = false;
                        locateInterval = 10;
                        log.error("{} 平台 {} 用户的设备：{} 查询失败，结束操作。",accountEntity.getPlatform(),accountEntity.getUsername(),accountEntity.getDeviceId());
                    }
                    Thread.sleep(locateInterval * 30000);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            reset();
            TraceGps.restart(accountEntity);
        });
    }

    @Override
    public boolean dataHandler(LocationEntity locationEntity){
        try {
            locationRepository.save(locationEntity);
        }catch (Exception e){
            log.error(e);
        }
        if (preLocation != null){
            locateInterval = calInterval(preLocation, locationEntity);
        }
        preLocation = locationEntity;
        return true;
    }



}
