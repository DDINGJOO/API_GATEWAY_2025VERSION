package com.study.api_gateway.api.profile.controller;


import com.study.api_gateway.api.gaechu.service.GaechuFacadeService;
import com.study.api_gateway.api.profile.dto.request.ProfileUpdateRequest;
import com.study.api_gateway.api.profile.service.ProfileFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.enrichment.ImageConfirmService;
import com.study.api_gateway.enrichment.cache.ProfileCache;
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
public class ProfileController implements ProfileApi {
	private final ProfileFacadeService profileFacadeService;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final GaechuFacadeService gaechuFacadeService;
	private final ProfileCache profileCache; // 프로필 수정 성공 시 캐시 무효화에 사용
	private final String categoryId = "PROFILE";
	
	@Override
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> fetchProfiles(@RequestParam(required = false) String city,
	                                                        @RequestParam(required = false, name = "nickName") String nickname,
	                                                        @RequestParam(required = false) List<Integer> genres,
	                                                        @RequestParam(required = false) List<Integer> instruments,
	                                                        @RequestParam(required = false) Character sex,
	                                                        @RequestParam(required = false) String cursor,
	                                                        @RequestParam(required = false) Integer size,
	                                                        ServerHttpRequest request) {
		return profileFacadeService.fetchProfilesWithPage(city, nickname, genres, instruments, sex, cursor, size)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/me")
	public Mono<ResponseEntity<BaseResponse>> fetchMyProfile(ServerHttpRequest request) {
		log.info("=== /profiles/me endpoint called === path: {}", request.getPath());
		
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userId = request.getHeaders().getFirst("X-User-Id");
		
		if (userId == null || userId.isEmpty()) {
			log.warn("X-User-Id header is missing or empty in /profiles/me request");
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		
		log.info("Fetching profile for authenticated user: {}", userId);
		
		// 프로필 조회 (필수)
		Mono<Object> profileMono = profileFacadeService.fetchProfile(userId)
				.map(profile -> (Object) profile);
		
		// 좋아요 수 조회 (선택 - 실패 시 빈 리스트 반환)
		Mono<Object> likedMono = gaechuFacadeService.getUserLikedCounts(categoryId, userId)
				.map(liked -> (Object) liked)
				.doOnError(e -> log.warn("Failed to fetch liked counts for userId={}: {}", userId, e.getMessage()))
				.onErrorReturn(java.util.Collections.emptyList());
		
		return Mono.zip(profileMono, likedMono)
				.map(tuple2 -> responseFactory.ok(java.util.Map.of(
						"profile", tuple2.getT1(),
						"liked", tuple2.getT2()
				), request));
	}
	
	@Override
	@GetMapping("/{userId}")
	public Mono<ResponseEntity<BaseResponse>> fetchProfile(@PathVariable String userId, ServerHttpRequest request) {
		log.info("=== /profiles/{} endpoint called === path: {}", userId, request.getPath());
		
		// 프로필 조회 (필수)
		Mono<Object> profileMono = profileFacadeService.fetchProfile(userId)
				.map(profile -> (Object) profile);
		
		// 좋아요 수 조회 (선택 - 실패 시 빈 리스트 반환)
		Mono<Object> likedMono = gaechuFacadeService.getUserLikedCounts(categoryId, userId)
				.map(liked -> (Object) liked)
				.doOnError(e -> log.warn("Failed to fetch liked counts for userId={}: {}", userId, e.getMessage()))
				.onErrorReturn(java.util.Collections.emptyList());
		
		return Mono.zip(profileMono, likedMono)
				.map(tuple2 -> responseFactory.ok(java.util.Map.of(
						"profile", tuple2.getT1(),
						"liked", tuple2.getT2()
				), request));
	}
	
	
	@Override
	@PutMapping("/me")
	public Mono<ResponseEntity<BaseResponse>> updateMyProfile(@RequestBody ProfileUpdateRequest req, ServerHttpRequest request) {
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userId = request.getHeaders().getFirst("X-User-Id");
		
		if (userId == null || userId.isEmpty()) {
			log.warn("X-User-Id header is missing or empty in /profiles/me PUT request");
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		
		log.debug("Updating profile for authenticated user: {}", userId);
		
		return profileFacadeService.updateProfile(userId, req)
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
	
	@Override
	@GetMapping("/validate")
	public Mono<ResponseEntity<BaseResponse>> validateProfile(
			@RequestParam("type") String type,
			@RequestParam("value") String value,
			ServerHttpRequest request) {
		return profileFacadeService.validateProfile(type, value)
				.map(result -> responseFactory.ok(result, request));
	}
}
