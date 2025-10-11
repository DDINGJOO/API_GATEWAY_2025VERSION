package com.study.api_gateway.controller.profile;


import com.study.api_gateway.client.LikeClient;
import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.service.ImageConfirmService;
import com.study.api_gateway.util.ResponseFactory;
import com.study.api_gateway.util.cache.ProfileCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/v1/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileClient profileClient;
    private final ImageConfirmService imageConfirmService;
    private final ResponseFactory responseFactory;
	private final LikeClient likeClient;
	private final ProfileCache profileCache; // 프로필 수정 성공 시 캐시 무효화에 사용
	private final String categoryId = "PROFILE";

    @Operation(summary = "프로필 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ProfilesList", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [ { \"userId\": \"u1\" } ],\n  \"request\": { \"path\": \"/bff/v1/profiles\" }\n}")))
    })
    @GetMapping
    public Mono<ResponseEntity<BaseResponse>> fetchProfiles(@RequestParam(required = false) String city,
                                                            @RequestParam(required = false) String nickname,
                                                            @RequestParam(required = false) List<Integer> genres,
                                                            @RequestParam(required = false) List<Integer> instruments,
                                                            @RequestParam(required = false) Character sex,
                                                            @RequestParam(required = false) String cursor,
                                                            @RequestParam(required = false) Integer size,
                                                            ServerHttpRequest request) {
	    return profileClient.fetchProfiles(city, nickname, genres, instruments, sex, cursor, size)
                .collectList()
                .map(result -> responseFactory.ok(result, request));
    }

    @Operation(summary = "프로필 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ProfileOne", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"userId\": \"u1\", \"nickname\": \"닉\" },\n  \"request\": { \"path\": \"/bff/v1/profiles/{userId}\" }\n}")))
    })
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<BaseResponse>> fetchProfile(@PathVariable String userId, ServerHttpRequest request){
	    return reactor.core.publisher.Mono.zip(
			    profileClient.fetchProfile(userId),
			    likeClient.getUserLikedCounts(categoryId, userId)
	    ).map(tuple2 -> responseFactory.ok(java.util.Map.of(
			    "profile", tuple2.getT1(),
			    "liked", tuple2.getT2()
	    ), request));
    }

//    @Operation(summary = "프로필 수정 ver1")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "성공",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "ProfileUpdateV1", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"updated\",\n  \"request\": { \"path\": \"/bff/v1/profiles/{userId}/ver1\" }\n}")))
//    })
//    @PutMapping("/{userId}/ver1")
//    public Mono<ResponseEntity<BaseResponse>> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest req, ServerHttpRequest request){
//		List<String> imageIds = new ArrayList<>();
//        return profileClient.updateProfileVer1(userId, req)
//                .flatMap(success -> {
//                    if (Boolean.TRUE.equals(success)) {
//                        if (req.getProfileImageId() != null) {
//                            log.info("imageId = {} , userId = {}", req.getProfileImageId(), userId);
//						imageIds.add(req.getProfileImageId());
//                            return imageConfirmService.confirmImage(userId,imageIds)
//                                    .thenReturn(responseFactory.ok("updated", request, HttpStatus.OK));
//                        }
//                        return Mono.just(responseFactory.ok("updated", request, HttpStatus.OK));
//                    }
//                    return Mono.just(responseFactory.error("update failed", HttpStatus.BAD_REQUEST, request));
//                });
//    }
	
	@Operation(summary = "프로필 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ProfileUpdateV2", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"updated\",\n  \"request\": { \"path\": \"/bff/v1/profiles/{userId}/ver2\" }\n}")))
    })
	@PutMapping("/{userId}")
    public Mono<ResponseEntity<BaseResponse>> updateProfile2(@PathVariable String userId, @RequestBody ProfileUpdateRequest req, ServerHttpRequest request){
        return profileClient.updateProfileVer2(userId, req)
		        .map(success -> {
			        if (Boolean.TRUE.equals(success)) {
				        // 프로필 수정 성공 시, 해당 사용자 캐시를 비동기로 무효화하여 이후 조회에서 최신 정보를 받도록 처리
				        profileCache.evict(userId)
						        .doOnError(e -> log.warn("Failed to evict profile cache after update userId={}: {}", userId, e.toString()))
						        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
						        .subscribe();
				        return responseFactory.ok("updated", request, HttpStatus.OK);
			        }
			        return responseFactory.error("update failed", HttpStatus.BAD_REQUEST, request);
		        });
    }
}
