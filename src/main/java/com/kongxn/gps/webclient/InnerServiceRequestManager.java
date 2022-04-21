package com.kongxn.gps.webclient;

import com.kongxn.gps.repository.LocationRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class InnerServiceRequestManager implements ServiceRequestManager{

    private final WebClient.Builder webClientBuilder;
    private final LocationRepository locationRepository;
    private final Map<String, Client> serviceWebClient = new ConcurrentHashMap<>();

    public InnerServiceRequestManager(WebClient.Builder webClientBuilder, LocationRepository locationRepository) {
        this.webClientBuilder = webClientBuilder;
        this.locationRepository = locationRepository;
    }

    @Override
    public Client getWebClient(String clientId,String url) {
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000*4)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(4, TimeUnit.MINUTES));
                    connection.addHandlerLast(new WriteTimeoutHandler(4, TimeUnit.MINUTES));
                });
        return serviceWebClient.computeIfAbsent(clientId,
                id -> new Client(webClientBuilder
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                        .build(),locationRepository
        ));
    }

    @Override
    public Client getWebClient(String clientId) {
        return serviceWebClient.get(clientId);
    }


}
