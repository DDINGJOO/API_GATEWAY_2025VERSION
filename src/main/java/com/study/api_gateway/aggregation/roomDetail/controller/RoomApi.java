package com.study.api_gateway.aggregation.roomDetail.controller;

import com.study.api_gateway.api.room.dto.request.RoomCreateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
 * 룸 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Room", description = "룸 관리 API")
public interface RoomApi {

	@Operation(summary = "방 생성", description = "새로운 방을 생성합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> createRoom(
			@RequestBody RoomCreateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "방 삭제", description = "방을 삭제합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "삭제 성공"),
			@ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
	})
	@DeleteMapping("/{roomId}")
	Mono<ResponseEntity<BaseResponse>> deleteRoom(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req);

	@Operation(summary = "룸 상세 조회", description = "룸 상세 정보, 장소 정보, 가격 정책, 이용 가능한 상품을 함께 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/{roomId}")
	Mono<ResponseEntity<BaseResponse>> getRoomById(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req);

	@Operation(summary = "룸 검색", description = "다양한 조건으로 룸을 검색하고 Place 정보와 가격을 함께 제공합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/search")
	Mono<ResponseEntity<BaseResponse>> searchRooms(
			@Parameter(description = "룸 이름") @RequestParam(required = false) String roomName,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "장소 ID") @RequestParam(required = false) Long placeId,
			@Parameter(description = "최소 수용 인원") @RequestParam(required = false) Integer minOccupancy,
			ServerHttpRequest req);

	@Operation(summary = "장소별 룸 목록 조회", description = "특정 장소에 속한 모든 룸을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/place/{placeId}")
	Mono<ResponseEntity<BaseResponse>> getRoomsByPlaceId(
			@Parameter(description = "장소 ID", required = true) @PathVariable Long placeId,
			ServerHttpRequest req);

	@Operation(summary = "여러 룸 일괄 조회", description = "여러 룸의 상세 정보와 가격 정책을 한 번에 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/batch")
	Mono<ResponseEntity<BaseResponse>> getRoomsByIds(
			@Parameter(description = "룸 ID 목록", required = true) @RequestParam List<Long> ids,
			ServerHttpRequest req);

	@Operation(summary = "키워드 맵 조회", description = "전체 키워드 ID-이름 매핑 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/keywords")
	Mono<ResponseEntity<BaseResponse>> getRoomKeywordMap(ServerHttpRequest req);
}
