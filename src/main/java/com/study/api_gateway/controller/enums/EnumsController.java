package com.study.api_gateway.controller.enums;


import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.client.ImageClient;
import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.response.ConsentsTable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/bff/v1/enums")
@RequiredArgsConstructor
public class EnumsController {
    private final ProfileClient profileClient;
    private final AuthClient authClient;
    private final ImageClient imageClient;

    @GetMapping("/genres")
    public Mono<ResponseEntity<BaseResponse>> genres(){
        return profileClient.fetchGenres()
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/enums/genres")));
    }

    @GetMapping("/instruments")
    public Mono<ResponseEntity<BaseResponse>> instruments(){
        return profileClient.fetchInstruments()
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/enums/instruments")));
    }

    @GetMapping("/locations")
    public Mono<ResponseEntity<BaseResponse>> locations(){
        return profileClient.fetchLocations()
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/enums/locations")));
    }

    @GetMapping("/consents")
    public Mono<ResponseEntity<BaseResponse>> consents(@RequestParam(name = "all") Boolean all){
        return authClient.fetchAllConsents(all)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/enums/consents")));
    }

    @GetMapping("/extensions")
    public Mono<ResponseEntity<BaseResponse>> extensions(){
        return imageClient.getExtensions()
                .map(result -> BaseResponse.success(result, Map.of("path", "/api/images/enums/extensions")));
    }

    @GetMapping("/reference-type")
    public Mono<ResponseEntity<BaseResponse>> referenceType(){
        return imageClient.getReferenceType()
                .map(result -> BaseResponse.success(result, Map.of("path", "/api/images/enums/referenceTypes")));
    }
}
