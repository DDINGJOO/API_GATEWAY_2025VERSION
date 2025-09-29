package com.study.api_gateway.client;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ImageClient {
    private final WebClient webClient;

    public ImageClient(@Qualifier(value = "imageWebClient") WebClient webClient) {
        this.webClient = webClient;
    }



    public Mono<Void> confirmImage(List<String> imageIds, String referenceId)
    {
        String uriString = UriComponentsBuilder.fromPath("/api/images/"+referenceId+"/confirm")
                .build()
                .toUriString();
        return webClient.patch()
                .uri(uriString)
                .bodyValue(imageIds)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
