package com.study.api_gateway.api.room.controller;

import com.study.api_gateway.api.room.dto.request.ReservationFieldRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 예약 필드 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "ReservationField", description = "예약 시 추가 정보 필드 관리 API")
public interface ReservationFieldApi {
	
	@Operation(summary = "예약 필드 목록 조회", description = "특정 룸의 예약 시 추가 정보 필드 목록을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> getReservationFields(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req);
	
	@Operation(summary = "예약 필드 전체 교체", description = "특정 룸의 예약 시 추가 정보 필드를 전체 교체합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "교체 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PutMapping
	Mono<ResponseEntity<BaseResponse>> replaceReservationFields(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@Valid @RequestBody List<ReservationFieldRequest> requests,
			ServerHttpRequest req);
}
