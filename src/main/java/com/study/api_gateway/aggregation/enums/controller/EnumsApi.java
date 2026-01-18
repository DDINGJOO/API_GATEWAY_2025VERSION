package com.study.api_gateway.aggregation.enums.controller;

import com.study.api_gateway.api.support.dto.faq.FaqCategory;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * 열거형 데이터 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Enums", description = "열거형 데이터 API")
public interface EnumsApi {
	
	@Operation(summary = "장르 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/genres")
	Mono<ResponseEntity<BaseResponse>> genres(ServerHttpRequest request);
	
	@Operation(summary = "악기 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/instruments")
	Mono<ResponseEntity<BaseResponse>> instruments(ServerHttpRequest request);
	
	@Operation(summary = "활동지역 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/locations")
	Mono<ResponseEntity<BaseResponse>> locations(ServerHttpRequest request);
	
	@Operation(summary = "동의항목 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/consents")
	Mono<ResponseEntity<BaseResponse>> consents(
			@RequestParam(name = "all") Boolean all,
			ServerHttpRequest request);
	
	@Operation(summary = "이미지 확장자 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/extensions")
	Mono<ResponseEntity<BaseResponse>> extensions(ServerHttpRequest request);
	
	@Operation(summary = "이미지 레퍼런스 타입")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/reference-types")
	Mono<ResponseEntity<BaseResponse>> referenceType(ServerHttpRequest request);
	
	@Operation(summary = "게시글 보드 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/articles/boards")
	Mono<ResponseEntity<BaseResponse>> boards(ServerHttpRequest request);
	
	@Operation(summary = "게시글 키워드 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/articles/keywords")
	Mono<ResponseEntity<BaseResponse>> articleKeywords(ServerHttpRequest request);
	
	@Operation(summary = "FAQ 목록 조회", description = "카테고리별 FAQ 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/faqs")
	Mono<ResponseEntity<BaseResponse>> getFaqs(
			@RequestParam(required = false) FaqCategory category,
			ServerHttpRequest req);
	
	@Operation(summary = "플레이스 키워드 목록 조회", description = "활성화된 플레이스 키워드 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/place-keywords")
	Mono<ResponseEntity<BaseResponse>> getPlaceKeywords(
			@RequestParam(required = false) String type,
			ServerHttpRequest req);
	
	@Operation(summary = "룸 키워드 맵 조회", description = "룸에서 사용 가능한 모든 키워드를 Map 형태로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/room-keywords")
	Mono<ResponseEntity<BaseResponse>> getRoomKeywords(ServerHttpRequest req);
}
