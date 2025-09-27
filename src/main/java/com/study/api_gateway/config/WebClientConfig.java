package com.study.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {
    @Value("${service.auth.url}")
    private String AuthDns;
    @Value("${service.auth.port}")
    private String AuthPort;


    @Value("${service.profile.url}")
    private String ProfileDns;
    @Value("${service.profile.port}")
    private String ProfilePort;


    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        String url = "http://%s:%s".formatted(AuthDns, AuthPort);

        return builder
                .baseUrl(url)
                .build();
    }

    @Bean
    public WebClient profileWebClient(WebClient.Builder builder) {
        String url = "http://%s:%s".formatted(ProfileDns, ProfilePort);

        return builder
                .baseUrl(url)
                .build();
    }

}
