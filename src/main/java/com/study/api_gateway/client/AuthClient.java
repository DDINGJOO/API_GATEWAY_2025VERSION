package com.study.api_gateway.client;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthClient {
    private final WebClient webClient;

    public AuthClient(@Qualifier(value = "authWebClient") WebClient webClient) {
        this.webClient = webClient;
    }
}
