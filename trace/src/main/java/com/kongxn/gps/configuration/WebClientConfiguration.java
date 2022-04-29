package com.kongxn.gps.configuration;

import com.kongxn.gps.repository.LocationRepository;
import com.kongxn.gps.webclient.InnerServiceRequestManager;
import com.kongxn.gps.webclient.ServiceRequestManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public ServiceRequestManager serviceRequestManager(ObjectProvider<WebClientCustomizer> customizerProvider, LocationRepository locationRepository){
        WebClient.Builder builder = WebClient.builder();

        customizerProvider.orderedStream().forEach(customizer -> customizer.customize(builder));
        return new InnerServiceRequestManager(builder, locationRepository);
    }
}
