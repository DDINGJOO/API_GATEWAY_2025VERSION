package com.study.api_gateway.client;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ImageClient {
    private final WebClient webClient;

    public ImageClient(@Qualifier(value = "imageWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

}
