package com.study.api_gateway.api.chat.controller;

import com.study.api_gateway.api.chat.dto.request.*;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 채팅 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Chat", description = "채팅 API")
public interface ChatApi {

	// ==================== 채팅방 생성 API ====================

	@Operation(summary = "1:1 DM 채팅방 생성", description = "1:1 채팅방을 생성합니다. 동일한 참여자 조합의 DM이 이미 있으면 기존 방을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "새 채팅방 생성 성공"),
			@ApiResponse(responseCode = "200", description = "기존 채팅방 반환")
	})
	@PostMapping("/rooms/dm")
	Mono<ResponseEntity<BaseResponse>> createDmRoom(
			@RequestBody CreateDmRoomRequest dmRequest,
			ServerHttpRequest request);

	// ==================== 대화 API ====================

	@Operation(summary = "대화 목록 조회", description = "사용자의 대화 목록을 조회합니다. 참여자 프로필 정보가 포함됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/conversations")
	Mono<ResponseEntity<BaseResponse>> getConversations(
			@Parameter(description = "채팅방 타입 필터 (DM, GROUP, PLACE_INQUIRY, SUPPORT)")
			@RequestParam(required = false) String type,
			ServerHttpRequest request);

	@Operation(summary = "대화 상세 조회", description = "특정 대화의 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "404", description = "대화를 찾을 수 없음")
	})
	@GetMapping("/conversations/{conversationId}")
	Mono<ResponseEntity<BaseResponse>> getConversation(
			@PathVariable String conversationId,
			ServerHttpRequest request);

	// ==================== 메시지 API ====================

	@Operation(summary = "메시지 목록 조회", description = "대화의 메시지 목록을 조회합니다. 발신자 프로필 정보가 포함됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "403", description = "대화 접근 권한 없음")
	})
	@GetMapping("/conversations/{conversationId}/messages")
	Mono<ResponseEntity<BaseResponse>> getMessages(
			@PathVariable String conversationId,
			@Parameter(description = "페이징 커서 (마지막 messageId)")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "50") Integer limit,
			ServerHttpRequest request);

	@Operation(summary = "메시지 전송", description = "대화에 메시지를 전송합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "메시지 전송 성공"),
			@ApiResponse(responseCode = "403", description = "대화 접근 권한 없음")
	})
	@PostMapping("/conversations/{conversationId}/messages")
	Mono<ResponseEntity<BaseResponse>> sendMessage(
			@PathVariable String conversationId,
			@RequestBody SendMessageRequest messageRequest,
			ServerHttpRequest request);

	@Operation(summary = "메시지 읽음 처리", description = "대화의 메시지를 읽음 처리합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@PostMapping("/conversations/{conversationId}/messages/read")
	Mono<ResponseEntity<BaseResponse>> markAsRead(
			@PathVariable String conversationId,
			@RequestBody(required = false) ReadMessageRequest readRequest,
			ServerHttpRequest request);

	@Operation(summary = "메시지 삭제", description = "메시지를 삭제합니다. 본인이 보낸 메시지만 삭제 가능합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "403", description = "메시지 삭제 권한 없음")
	})
	@DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
	Mono<ResponseEntity<BaseResponse>> deleteMessage(
			@PathVariable String conversationId,
			@PathVariable String messageId,
			ServerHttpRequest request);

	// ==================== 공간 문의 API ====================

	@Operation(summary = "공간 문의 생성", description = "호스트에게 공간 문의 채팅을 시작합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "문의 생성 성공"),
			@ApiResponse(responseCode = "409", description = "동일 공간에 이미 문의 채팅방 존재")
	})
	@PostMapping("/inquiry")
	Mono<ResponseEntity<BaseResponse>> createPlaceInquiry(
			@RequestBody PlaceInquiryRequest inquiryRequest,
			ServerHttpRequest request);

	@Operation(summary = "호스트 문의 목록 조회", description = "호스트로서 받은 문의 목록을 조회합니다. 게스트 프로필 정보가 포함됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/inquiry/host")
	Mono<ResponseEntity<BaseResponse>> getHostInquiries(
			@Parameter(description = "공간 ID (특정 공간의 문의만 조회)")
			@RequestParam(required = false) Long placeId,
			@Parameter(description = "페이징 커서")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "20") Integer limit,
			ServerHttpRequest request);

	// ==================== 고객 상담 API ====================

	@Operation(summary = "상담 요청 생성", description = "고객 상담을 요청합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "상담 요청 성공"),
			@ApiResponse(responseCode = "409", description = "이미 진행 중인 상담 있음")
	})
	@PostMapping("/support")
	Mono<ResponseEntity<BaseResponse>> createSupportRequest(
			@RequestBody(required = false) SupportRequest supportRequest,
			ServerHttpRequest request);

	@Operation(summary = "상담 대기열 조회", description = "상담 대기열을 조회합니다. (관리자/상담원용)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/support/queue")
	Mono<ResponseEntity<BaseResponse>> getSupportQueue(
			@Parameter(description = "페이징 커서")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "20") Integer limit,
			ServerHttpRequest request);

	@Operation(summary = "상담원 배정", description = "상담원을 대화에 배정합니다. (관리자/상담원용)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "409", description = "이미 상담원이 배정됨")
	})
	@PostMapping("/support/{conversationId}/assign")
	Mono<ResponseEntity<BaseResponse>> assignAgent(
			@PathVariable String conversationId,
			ServerHttpRequest request);

	@Operation(summary = "상담 종료", description = "상담을 종료합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "412", description = "이미 종료된 대화")
	})
	@PostMapping("/support/{conversationId}/close")
	Mono<ResponseEntity<BaseResponse>> closeSupportChat(
			@PathVariable String conversationId,
			ServerHttpRequest request);
}
