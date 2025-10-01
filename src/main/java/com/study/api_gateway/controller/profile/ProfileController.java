package com.study.api_gateway.controller.profile;


import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.profile.ProfileSearchCriteria;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.dto.profile.response.UserResponse;
import com.study.api_gateway.service.ImageConfirmService;
import com.study.api_gateway.util.ResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileClient profileClient;
    private final ImageConfirmService imageConfirmService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public Mono<ResponseEntity<BaseResponse>> fetchProfiles(@RequestParam String userId, @RequestParam ProfileSearchCriteria req, @RequestParam String cursor, @RequestParam int size, ServerHttpRequest request){
        return profileClient.fetchProfiles(userId, req, cursor, size)
                .collectList()
                .map(result -> responseFactory.ok(result, request));
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<BaseResponse>> fetchProfile(@PathVariable String userId, ServerHttpRequest request){
        return profileClient.fetchProfile(userId)
                .map(result -> responseFactory.ok(result, request));
    }

    @PutMapping("/{userId}/ver1")
    public Mono<ResponseEntity<BaseResponse>> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest req, ServerHttpRequest request){
        return profileClient.updateProfileVer1(userId, req)
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        if (req.getProfileImageId() != null) {
                            log.info("imageId = {} , userId = {}", req.getProfileImageId(), userId);
                            return imageConfirmService.confirmImage(userId, req.getProfileImageId())
                                    .thenReturn(responseFactory.ok("updated", request, HttpStatus.OK));
                        }
                        return Mono.just(responseFactory.ok("updated", request, HttpStatus.OK));
                    }
                    return Mono.just(responseFactory.error("update failed", HttpStatus.BAD_REQUEST, request));
                });
    }

    @PutMapping("/{userId}/ver2")
    public Mono<ResponseEntity<BaseResponse>> updateProfile2(@PathVariable String userId, @RequestBody ProfileUpdateRequest req, ServerHttpRequest request){
        return profileClient.updateProfileVer2(userId, req)
                .map(success -> Boolean.TRUE.equals(success)
                        ? responseFactory.ok("updated", request, HttpStatus.OK)
                        : responseFactory.error("update failed", HttpStatus.BAD_REQUEST, request)
                );
    }
}
