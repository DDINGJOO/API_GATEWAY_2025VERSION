package com.study.api_gateway.controller.profile;


import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.profile.ProfileSearchCriteria;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.dto.profile.response.UserResponse;
import com.study.api_gateway.service.ImageConfirmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileClient profileClient;
    private final ImageConfirmService imageConfirmService;



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
        return profileClient.updateProfileVer1(userId, req)
                // update 성공 여부에 따라 후속 처리
                .flatMap(success -> {
                    if (!success) {
                        return Mono.just(false);
                    }
                    String imageId = req.getImageId();
                    if (imageId == null || imageId.isBlank()) {
                        // 이미지 없음 => 그냥 성공 반환
                        return Mono.just(true);
                    }
                    // imageConfirmService.confirmImage(...)가 Mono<Void>를 반환한다고 가정한 경우
                    // confirmImage가 블로킹이라면 imageConfirmService 내부에서 적절히 subscribeOn 처리 필요
                    return imageConfirmService.confirmImage(imageId)
                            .thenReturn(true)
                            // confirm 실패 시 원하는 동작: 실패로 처리하거나 무시할 수 있음
                            .onErrorResume(e -> {
                                // 로깅 후 실패로 처리 (또는 Mono.just(true)로 무시)
                                // log.error("image confirm failed", e);
                                return Mono.just(false);
                            });
                })
                // 전체 흐름에서 발생한 예외를 처리
                .onErrorResume(e -> {
                    // log.error("updateProfile failed", e);
                    return Mono.just(false);
                });
    }

    @PutMapping("/{userId}/ver2")
    public Mono<Boolean> updateProfile2(@PathVariable String userId, @RequestBody ProfileUpdateRequest req){
        return profileClient.updateProfileVer2(userId,req);
    }




}
