package com.study.api_gateway.api.profile.controller;

import com.study.api_gateway.api.profile.dto.request.ProfileUpdateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 프로필 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Profile", description = "프로필 관련 API")
public interface ProfileApi {

	@Operation(summary = "프로필 목록 조회",
			description = "조건에 맞는 프로필 목록을 페이지네이션 메타데이터와 함께 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": 200,
									  "data": {
									    "content": [{"userId": "u1", "nickname": "닉네임"}],
									    "size": 10,
									    "numberOfElements": 5
									  }
									}
									""")))
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> fetchProfiles(
			@Parameter(description = "도시") @RequestParam(required = false) String city,
			@Parameter(description = "닉네임") @RequestParam(required = false, name = "nickName") String nickname,
			@Parameter(description = "장르 ID 목록") @RequestParam(required = false) List<Integer> genres,
			@Parameter(description = "악기 ID 목록") @RequestParam(required = false) List<Integer> instruments,
			@Parameter(description = "성별") @RequestParam(required = false) Character sex,
			@Parameter(description = "커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(required = false) Integer size,
			ServerHttpRequest request);

	@Operation(summary = "내 프로필 조회",
			description = "토큰에서 추출한 사용자 ID로 자신의 프로필을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": 200,
									  "data": {
									    "profile": {"userId": "u1", "nickname": "닉"},
									    "liked": 10
									  }
									}
									"""))),
			@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@GetMapping("/me")
	Mono<ResponseEntity<BaseResponse>> fetchMyProfile(ServerHttpRequest request);

	@Operation(summary = "프로필 단건 조회",
			description = "특정 사용자의 프로필을 userId로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
	})
	@GetMapping("/{userId}")
	Mono<ResponseEntity<BaseResponse>> fetchProfile(
			@Parameter(description = "사용자 ID") @PathVariable String userId,
			ServerHttpRequest request);

	@Operation(summary = "내 프로필 수정",
			description = "토큰에서 추출한 사용자 ID로 자신의 프로필을 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패"),
			@ApiResponse(responseCode = "400", description = "수정 실패")
	})
	@PutMapping("/me")
	Mono<ResponseEntity<BaseResponse>> updateMyProfile(
			@RequestBody ProfileUpdateRequest req,
			ServerHttpRequest request);

	@Operation(summary = "프로필 필드 검증",
			description = "닉네임 등 프로필 필드의 중복 여부를 검증합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": 200,
									  "data": false
									}
									""")))
	})
	@GetMapping("/validate")
	Mono<ResponseEntity<BaseResponse>> validateProfile(
			@Parameter(description = "검증 타입 (예: nickname)") @RequestParam("type") String type,
			@Parameter(description = "검증할 값") @RequestParam("value") String value,
			ServerHttpRequest request);
}
