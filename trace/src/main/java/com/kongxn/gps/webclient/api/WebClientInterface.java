package com.kongxn.gps.webclient.api;

import com.kongxn.gps.entity.LocationEntity;
import com.kongxn.gps.webclient.WebClientStatus;
import reactor.core.publisher.Mono;

public interface WebClientInterface {

    void start();

    void keepalive();

    Mono<LocationEntity> queryLocate();

    boolean dataHandler(LocationEntity locationEntity);

    int calInterval(LocationEntity pre,LocationEntity current);

    void reset();

    WebClientStatus status();
}
