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
	
	@Operation(summary = "프로필 목록 조회", description = "조건에 맞는 프로필 목록을 페이지네이션 메타데이터와 함께 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
		                    examples = @ExampleObject(name = "ProfilesList", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"content\": [{\"userId\": \"u1\"}],\n    \"pageable\": {...},\n    \"size\": 10,\n    \"numberOfElements\": 5,\n    \"first\": true,\n    \"last\": false\n  },\n  \"request\": { \"path\": \"/bff/v1/profiles\" }\n}")))
    })
    @GetMapping
    public Mono<ResponseEntity<BaseResponse>> fetchProfiles(@RequestParam(required = false) String city,
	                                                        @RequestParam(required = false, name = "nickName") String nickname,
                                                            @RequestParam(required = false) List<Integer> genres,
                                                            @RequestParam(required = false) List<Integer> instruments,
                                                            @RequestParam(required = false) Character sex,
                                                            @RequestParam(required = false) String cursor,
                                                            @RequestParam(required = false) Integer size,
                                                            ServerHttpRequest request) {
		return profileClient.fetchProfilesWithPage(city, nickname, genres, instruments, sex, cursor, size)
                .map(result -> responseFactory.ok(result, request));
    }
	
	@Operation(summary = "내 프로필 조회", description = "토큰에서 추출한 사용자 ID로 자신의 프로필을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
		                    examples = @ExampleObject(name = "MyProfile", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"profile\": { \"userId\": \"u1\", \"nickname\": \"닉\" }, \"liked\": 10 },\n  \"request\": { \"path\": \"/bff/v1/profiles/me\" }\n}"))),
		    @ApiResponse(responseCode = "401", description = "인증 실패",
				    content = @Content(mediaType = "application/json",
						    examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"토큰이 만료되었습니다\"\n}")))
    })
	@GetMapping("/me")
	public Mono<ResponseEntity<BaseResponse>> fetchMyProfile(ServerHttpRequest request) {
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userId = request.getHeaders().getFirst("X-User-Id");
		
		if (userId == null || userId.isEmpty()) {
			log.warn("X-User-Id header is missing or empty in /profiles/me request");
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		
		log.debug("Fetching profile for authenticated user: {}", userId);
		
		return reactor.core.publisher.Mono.zip(
				profileClient.fetchProfile(userId),
				likeClient.getUserLikedCounts(categoryId, userId)
		).map(tuple2 -> responseFactory.ok(java.util.Map.of(
				"profile", tuple2.getT1(),
				"liked", tuple2.getT2()
		), request));
	}
	
	@Operation(summary = "프로필 단건 조회", description = "특정 사용자의 프로필을 userId로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ProfileOne", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"profile\": { \"userId\": \"u1\", \"nickname\": \"닉\" }, \"liked\": 10 },\n  \"request\": { \"path\": \"/bff/v1/profiles/{userId}\" }\n}")))
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
	
	
	@Operation(summary = "내 프로필 수정", description = "토큰에서 추출한 사용자 ID로 자신의 프로필을 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "MyProfileUpdate", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": { \"path\": \"/bff/v1/profiles/me\" }\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"사용자 인증 정보를 찾을 수 없습니다\"\n}")))
	})
	@PutMapping("/me")
	public Mono<ResponseEntity<BaseResponse>> updateMyProfile(@RequestBody ProfileUpdateRequest req, ServerHttpRequest request) {
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userId = request.getHeaders().getFirst("X-User-Id");
		
		if (userId == null || userId.isEmpty()) {
			log.warn("X-User-Id header is missing or empty in /profiles/me PUT request");
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		
		log.debug("Updating profile for authenticated user: {}", userId);

		return profileClient.updateProfile(userId, req)
				.flatMap(success -> {
					if (Boolean.TRUE.equals(success)) {
						// 병렬 처리할 작업들을 준비
						Mono<Void> cacheEviction = profileCache.evict(userId)
								.doOnError(e -> log.warn("Failed to evict profile cache after update userId={}: {}", userId, e.toString()))
								.onErrorResume(e -> Mono.empty()); // 캐시 무효화 실패해도 계속 진행
						
						// 이미지 ID가 있으면 이미지 확정 처리
						Mono<Void> imageConfirmation = (req.getProfileImageId() != null && !req.getProfileImageId().isEmpty())
								? imageConfirmService.confirmImage(userId, List.of(req.getProfileImageId()))
								.doOnSuccess(v -> log.info("Profile image confirmed: imageId={}, userId={}", req.getProfileImageId(), userId))
								.doOnError(e -> log.error("Failed to confirm profile image: imageId={}, userId={}, error={}", req.getProfileImageId(), userId, e.getMessage()))
								: Mono.empty();
						
						// 캐시 무효화와 이미지 확정을 병렬로 실행하고 모두 완료될 때까지 대기
						return Mono.when(cacheEviction, imageConfirmation)
								.thenReturn(responseFactory.ok(true, request, HttpStatus.OK));
					}
					return Mono.just(responseFactory.error("update failed", HttpStatus.BAD_REQUEST, request));
				});
	}
}
