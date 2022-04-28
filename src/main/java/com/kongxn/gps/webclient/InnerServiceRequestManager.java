package com.kongxn.gps.webclient;

import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.repository.LocationRepository;
import com.kongxn.gps.webclient.api.WebClientInterface;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.data.util.Pair;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author kxn
 */
public class InnerServiceRequestManager implements ServiceRequestManager{

    private final WebClient.Builder webClientBuilder;
    private final LocationRepository locationRepository;
    private final Map<Pair<String,String>, AbstractWebClient> webClientMap = new HashMap<>();

    private Class[] argsClass = new Class[3];

    public InnerServiceRequestManager(WebClient.Builder webClientBuilder, LocationRepository locationRepository) {
        this.webClientBuilder = webClientBuilder;
        this.locationRepository = locationRepository;
        argsClass[0] = WebClient.class;
        argsClass[1] = LocationRepository.class;
        argsClass[2] = AccountEntity.class;
    }

    @Override
    public <T extends AbstractWebClient> T getWebClient(AccountEntity accountEntity, Class<T> clz) throws Exception {
        Pair<String,String> key = Pair.of(accountEntity.getPlatform(), accountEntity.getUsername());
        if (webClientMap.get(key) != null){
            return (T) webClientMap.get(key);
        }
        WebClient webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(TcpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000*4)
                        .doOnConnected(connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(4, TimeUnit.MINUTES));
                            connection.addHandlerLast(new WriteTimeoutHandler(4, TimeUnit.MINUTES));
                        }))))
                .build();
        T client = clz.getConstructor(argsClass).newInstance(webClient,locationRepository,accountEntity);
        webClientMap.put(key, client);
        return client;
    }



}
