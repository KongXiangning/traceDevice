package com.kongxn.gps.webclient;

import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ClientHandler{

    private WebClient webClient;

    public ClientHandler(WebClient webClient){
        this.webClient = webClient;
    }

    protected Mono<ClientResponse> get(String url, String type){
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(ClientResponse.class)
                .doOnError(Exception.class, error ->{
                    System.out.println(error.getMessage());
                })
                .retry();
    }
}
