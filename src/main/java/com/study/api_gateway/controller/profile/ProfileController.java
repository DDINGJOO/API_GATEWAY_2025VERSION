package com.study.api_gateway.controller.profile;


import com.study.api_gateway.client.ImageClient;
import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.profile.ProfileSearchCriteria;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.dto.profile.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileClient profileClient;
    private final ImageClient imageClient;

    @GetMapping
    public Mono<ResponseEntity<BaseResponse>> fetchProfiles(@RequestParam String userId, @RequestParam ProfileSearchCriteria req, @RequestParam String cursor, @RequestParam int size) {
        return profileClient.fetchProfiles(userId, req, cursor, size)
                .collectList()
                .map(list -> BaseResponse.success(list, Map.of("path", "/bff/v1/profiles")));
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<BaseResponse>> fetchProfile(@PathVariable String userId) {
        return profileClient.fetchProfile(userId)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/profiles/" + userId)));
    }

    @PutMapping("/{userId}/ver1")
    public Mono<ResponseEntity<BaseResponse>> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest req) {
        // 이미지 처리와 프로필 업데이트를 병렬로 실행
        Mono<Void> imageMono = req.getProfileImageId() != null
                ? imageClient.confirmImage(List.of(req.getProfileImageId()), userId)
                : Mono.empty();

        Mono<Boolean> profileMono = profileClient.updateProfileVer1(userId, req)
                .cache(); // 병렬 진행 시 재구독으로 인한 다중 호출 방지

        // 호출한 서비스(클라이언트)들의 요청 주소 정보를 수집
        List<String> calledServices = new ArrayList<>();
        if (req.getProfileImageId() != null) {
            // 이미지 호출이 있을 때 image service 경로 예시를 넣음 (실제 경로/서비스명은 필요시 조정)
            calledServices.add("/api/images/" + userId + "/confirm");
        }
        calledServices.add("/api/profiles/profiles/" + userId + "/ver1"); // profile service 경로

        // 병렬 실행: 이미지가 있든 없든 profileMono는 항상 실행되고, 이미지가 있으면 imageMono도 병렬 실행
        return Mono.when(imageMono, profileMono)
                // profileMono 결과를 사용하여 응답 생성
                .then(profileMono.map(result ->
                        BaseResponse.success(result, Map.of(
                                "calledServices", calledServices,
                                "path", "/bff/v1/profiles/" + userId + "/ver1"
                        ))
                ))
                .onErrorResume(ex -> {
                    String msg = "프로필 업데이트 중 오류가 발생했습니다: " + ex.getMessage();
                    return Mono.just(BaseResponse.error(msg, Map.of(
                            "calledServices", calledServices,
                            "path", "/bff/v1/profiles/" + userId + "/ver1"
                    )));
                });
    }

    @PutMapping("/{userId}/ver2")
    public Mono<ResponseEntity<BaseResponse>> updateProfile2(@PathVariable String userId, @RequestBody ProfileUpdateRequest req) {
        return profileClient.updateProfileVer2(userId, req)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/profiles/" + userId + "/ver2")));
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<BaseResponse>> validateProfile(@RequestParam String type, @RequestParam String value) {
        return profileClient.validateProfile(type, value)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/profiles/validate")));
    }
}
