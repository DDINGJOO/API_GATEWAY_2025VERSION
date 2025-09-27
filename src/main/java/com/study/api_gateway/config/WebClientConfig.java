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


    @Value("${service.image.url}")
    private String ImageDns;
    @Value("${service.image.port}")
    private String ImagePort;

    private String normalizeHost(String raw) {
        if (raw == null) return "";
        // 공백 제거
        String s = raw.trim();
        // scheme 제거 (http:// 또는 https://)
        s = s.replaceFirst("(?i)^https?://", "");
        // 만약 포트가 이미 포함되어 있다면 그대로 사용하게(포트는 별도로 붙이지 않음)
        return s;
    }


    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        String host = normalizeHost(AuthDns);
        String url = "http://%s:%s".formatted(host, AuthPort);

        return builder
                .baseUrl(url)
                .build();
    }

    @Bean
    public WebClient profileWebClient(WebClient.Builder builder) {
        String host = normalizeHost(ProfileDns);
        String url = "http://%s:%s".formatted(host, ProfilePort);


        return builder
                .baseUrl(url)
                .build();
    }

    @Bean
    public WebClient imageWebClient(WebClient.Builder builder) {
        String host = normalizeHost(ImageDns);
        String url = "http://%s:%s".formatted(host, ImagePort);


        return builder
                .baseUrl(url)
                .build();
    }

}
