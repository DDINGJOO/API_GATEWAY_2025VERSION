package com.study.api_gateway.controller.enums;


import com.study.api_gateway.client.ProfileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/bff/v1/enums")
@RequiredArgsConstructor
public class EnumsController {
    private final ProfileClient profileClient;

    @GetMapping("/genres")
    public Mono<ResponseEntity<Map<Integer,String>>> genres(){
        return profileClient.fetchGenres()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/instruments")
    public Mono<ResponseEntity<Map<Integer,String>>> instruments(){
        return profileClient.fetchInstruments()
                .map(ResponseEntity::ok);
    }
}
