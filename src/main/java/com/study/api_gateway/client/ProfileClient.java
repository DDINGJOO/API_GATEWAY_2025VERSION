package com.study.api_gateway.client;


import com.study.api_gateway.dto.profile.ProfileSearchCriteria;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.dto.profile.response.UserResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ProfileClient {
    private final WebClient webClient;
    private final String PREFIX = "/api/profiles";

    public ProfileClient(@Qualifier(value = "profileWebClient") WebClient webClient) {
        this.webClient = webClient;
    }


    public Mono<Map<Integer, String>> fetchGenres() {

        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/genres")
                .toUriString();

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {});
    }

    public Mono<Map<Integer, String>> fetchInstruments() {
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/instruments")
                .toUriString();
        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {});
    }

    public Mono<Map<String, String>> fetchLocations() {
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/locations")
                .toUriString();
        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }


    public Mono<Boolean> updateProfileVer1(String userId, ProfileUpdateRequest req)
    {

        String uriString = UriComponentsBuilder.fromPath(PREFIX +"/profiles/"+userId +"/ver1")
                .toUriString();

        return webClient.put()
                .uri(uriString)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> updateProfileVer2(String userId, ProfileUpdateRequest req)
    {

        String uriString = UriComponentsBuilder.fromPath(PREFIX +"/peofiles/"+ userId +"/ver2")
                .toUriString();

        return webClient.put()
                .uri(uriString)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<UserResponse> fetchProfile(String userId){
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/profiles/" + userId)
                .toUriString();

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    public Flux<UserResponse> fetchProfiles(String userId, ProfileSearchCriteria req, String cursor, int size){
        String uriString = UriComponentsBuilder.fromPath(PREFIX)
                .queryParam("userId", userId)
                .queryParam("city", req.getCity())
                .queryParam("genres", req.getGenres())
                .queryParam("instruments", req.getInstruments())
                .queryParam("sex", req.getSex())
                .queryParam("cursor", cursor)
                .queryParam("size", size)
                .toUriString();

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToFlux(UserResponse.class);
    }

    public Mono<Boolean> validateProfile(String type, String value ){
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/validate")
                .queryParam("type", type)
                .queryParam("value", value)
                .toUriString();


        return webClient.post()
                .uri(uriString)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
