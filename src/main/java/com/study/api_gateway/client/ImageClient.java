package com.study.api_gateway.client;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class ImageClient {
    private final WebClient webClient;

    public ImageClient(@Qualifier(value = "imageWebClient") WebClient webClient) {
        this.webClient = webClient;
    }



    public Mono<Void> confirmImage(String imageId){

        String uriString = UriComponentsBuilder.fromPath("/api/images/"+imageId)
                .toUriString();


        return webClient.post()
                .uri(uriString)
                .retrieve()
                .bodyToMono(Void.class);
    }

}
