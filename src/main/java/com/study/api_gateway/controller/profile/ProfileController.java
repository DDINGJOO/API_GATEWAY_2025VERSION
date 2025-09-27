package com.study.api_gateway.controller.profile;


import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.profile.ProfileSearchCriteria;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.dto.profile.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileClient profileClient;



    @GetMapping
    public Flux<UserResponse> fetchProfiles(@RequestParam String userId,@RequestParam ProfileSearchCriteria req,@RequestParam String cursor,@RequestParam int size){
        return profileClient.fetchProfiles(userId, req, cursor, size);
    }
    @GetMapping("/{userId}")
    public Mono<UserResponse> fetchProfile(@PathVariable String userId){
        return profileClient.fetchProfile(userId);
    }

    @PutMapping("/{userId}/ver1")
    public Mono<Boolean> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest req){
        return profileClient.updateProfileVer1(userId,req);
    }

    @PutMapping("/{userId}/ver2")
    public Mono<Boolean> updateProfile2(@PathVariable String userId, @RequestBody ProfileUpdateRequest req){
        return profileClient.updateProfileVer2(userId,req);
    }




}
