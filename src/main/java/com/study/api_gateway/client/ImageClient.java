package com.study.api_gateway.client;


import com.study.api_gateway.dto.auth.response.ConsentsTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ImageClient {
    private final WebClient webClient;

    public ImageClient(@Qualifier(value = "imageWebClient") WebClient webClient) {
        this.webClient = webClient;
    }



//    public Mono<Void> confirmImage(String referenceId,String imageId){
//
//        String uriString = UriComponentsBuilder.fromPath("/api/images/"+referenceId+"/confirm")
//                .queryParam("imageId",imageId)
//                .toUriString();
//
//        log.info("confirmImage uriString : {}",uriString);
//        return webClient.patch()
//                .uri(uriString)
//                .retrieve()
//                .bodyToMono(Void.class);
//    }
//
	
	public Mono<Void> confirmImage(String referenceId, List<String> imageIds){
		String uriString = UriComponentsBuilder.fromPath("/api/images/"+referenceId+"/confirm")
				.queryParam("imageIds",imageIds)
				.toUriString();
		return webClient.patch()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}

    public Mono<Map<String,String >> getExtensions(){
        String uriString = UriComponentsBuilder.fromPath("/api/enums/extensions")
                .toUriString();
        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }

    public Mono<Map<String,String >> getReferenceType(){
        String uriString = UriComponentsBuilder.fromPath("/api/enums/referenceType")
                .toUriString();
        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }

}
