package com.kongxn.gps.map.webtools;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientTool {

    private final WebClient.Builder webClientBuilder = WebClient.builder();


}
