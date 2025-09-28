package com.study.api_gateway.controller.enums;


import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.client.ProfileClient;
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

    @GetMapping("/locations")
    public Mono<ResponseEntity<Map<String,String>>> locations(){
        return profileClient.fetchLocations()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/consents")
    public Mono<ResponseEntity<Map<String, ConsentsTable>>> consents(@RequestParam(name = "all") Boolean all){
        var result = authClient.fetchAllConsents(all);
        return result.map(ResponseEntity::ok);

    }
}
