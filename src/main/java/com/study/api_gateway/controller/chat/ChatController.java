package com.study.api_gateway.controller.chat;

import com.study.api_gateway.client.ChatClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.chat.request.PlaceInquiryRequest;
import com.study.api_gateway.dto.chat.request.ReadMessageRequest;
import com.study.api_gateway.dto.chat.request.SendMessageRequest;
import com.study.api_gateway.dto.chat.request.SupportRequest;
import com.study.api_gateway.service.ChatEnrichmentService;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/bff/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

	private final ChatClient chatClient;
	private final ChatEnrichmentService chatEnrichmentService;
	private final ResponseFactory responseFactory;

	// ==================== 대화 API ====================

	@Operation(summary = "대화 목록 조회", description = "사용자의 대화 목록을 조회합니다. 참여자 프로필 정보가 포함됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/conversations")
	public Mono<ResponseEntity<BaseResponse>> getConversations(
			@RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest request
	) {
		log.debug("getConversations: userId={}", userId);

		return chatEnrichmentService.getChatRoomsWithProfiles(userId)
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "대화 상세 조회", description = "특정 대화의 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "404", description = "대화를 찾을 수 없음")
	})
	@GetMapping("/conversations/{conversationId}")
	public Mono<ResponseEntity<BaseResponse>> getConversation(
			@PathVariable String conversationId,
			@RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest request
	) {
		log.debug("getConversation: conversationId={}, userId={}", conversationId, userId);

		return chatClient.getChatRoom(conversationId, userId)
				.map(result -> responseFactory.ok(result, request));
	}

	// ==================== 메시지 API ====================

	@Operation(summary = "메시지 목록 조회", description = "대화의 메시지 목록을 조회합니다. 발신자 프로필 정보가 포함됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "403", description = "대화 접근 권한 없음")
	})
	@GetMapping("/conversations/{conversationId}/messages")
	public Mono<ResponseEntity<BaseResponse>> getMessages(
			@PathVariable String conversationId,
			@RequestHeader("X-User-Id") Long userId,
			@Parameter(description = "페이징 커서 (마지막 messageId)")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "50") Integer limit,
			ServerHttpRequest request
	) {
		log.debug("getMessages: conversationId={}, userId={}, cursor={}, limit={}", conversationId, userId, cursor, limit);

		return chatEnrichmentService.getMessagesWithProfiles(conversationId, userId, cursor, limit)
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "메시지 전송", description = "대화에 메시지를 전송합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "메시지 전송 성공"),
			@ApiResponse(responseCode = "403", description = "대화 접근 권한 없음")
	})
	@PostMapping("/conversations/{conversationId}/messages")
	public Mono<ResponseEntity<BaseResponse>> sendMessage(
			@PathVariable String conversationId,
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody SendMessageRequest messageRequest,
			ServerHttpRequest request
	) {
		log.debug("sendMessage: conversationId={}, userId={}", conversationId, userId);

		return chatClient.sendMessage(conversationId, userId, messageRequest)
				.map(result -> responseFactory.ok(result, request, HttpStatus.CREATED));
	}

	@Operation(summary = "메시지 읽음 처리", description = "대화의 메시지를 읽음 처리합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@PostMapping("/conversations/{conversationId}/messages/read")
	public Mono<ResponseEntity<BaseResponse>> markAsRead(
			@PathVariable String conversationId,
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody(required = false) ReadMessageRequest readRequest,
			ServerHttpRequest request
	) {
		log.debug("markAsRead: conversationId={}, userId={}", conversationId, userId);

		return chatClient.markAsRead(conversationId, userId, readRequest)
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "메시지 삭제", description = "메시지를 삭제합니다. 본인이 보낸 메시지만 삭제 가능합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "403", description = "메시지 삭제 권한 없음")
	})
	@DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
	public Mono<ResponseEntity<BaseResponse>> deleteMessage(
			@PathVariable String conversationId,
			@PathVariable String messageId,
			@RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest request
	) {
		log.debug("deleteMessage: conversationId={}, messageId={}, userId={}", conversationId, messageId, userId);

		return chatClient.deleteMessage(conversationId, messageId, userId)
				.map(result -> responseFactory.ok(result, request));
	}

	// ==================== 공간 문의 API ====================

	@Operation(summary = "공간 문의 생성", description = "호스트에게 공간 문의 채팅을 시작합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "문의 생성 성공"),
			@ApiResponse(responseCode = "409", description = "동일 공간에 이미 문의 채팅방 존재")
	})
	@PostMapping("/inquiry")
	public Mono<ResponseEntity<BaseResponse>> createPlaceInquiry(
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody PlaceInquiryRequest inquiryRequest,
			ServerHttpRequest request
	) {
		log.debug("createPlaceInquiry: userId={}, placeId={}", userId, inquiryRequest.getPlaceId());

		return chatClient.createPlaceInquiry(userId, inquiryRequest)
				.map(result -> responseFactory.ok(result, request, HttpStatus.CREATED));
	}

	@Operation(summary = "호스트 문의 목록 조회", description = "호스트로서 받은 문의 목록을 조회합니다. 게스트 프로필 정보가 포함됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/inquiry/host")
	public Mono<ResponseEntity<BaseResponse>> getHostInquiries(
			@RequestHeader("X-User-Id") Long userId,
			@Parameter(description = "공간 ID (특정 공간의 문의만 조회)")
			@RequestParam(required = false) Long placeId,
			@Parameter(description = "페이징 커서")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "20") Integer limit,
			ServerHttpRequest request
	) {
		log.debug("getHostInquiries: userId={}, placeId={}, cursor={}, limit={}", userId, placeId, cursor, limit);

		return chatEnrichmentService.getHostInquiriesWithProfiles(userId, placeId, cursor, limit)
				.map(result -> responseFactory.ok(result, request));
	}

	// ==================== 고객 상담 API ====================

	@Operation(summary = "상담 요청 생성", description = "고객 상담을 요청합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "상담 요청 성공"),
			@ApiResponse(responseCode = "409", description = "이미 진행 중인 상담 있음")
	})
	@PostMapping("/support")
	public Mono<ResponseEntity<BaseResponse>> createSupportRequest(
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody(required = false) SupportRequest supportRequest,
			ServerHttpRequest request
	) {
		log.debug("createSupportRequest: userId={}", userId);

		return chatClient.createSupport(userId, supportRequest)
				.map(result -> responseFactory.ok(result, request, HttpStatus.CREATED));
	}

	@Operation(summary = "상담 대기열 조회", description = "상담 대기열을 조회합니다. (관리자/상담원용)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/support/queue")
	public Mono<ResponseEntity<BaseResponse>> getSupportQueue(
			@Parameter(description = "페이징 커서")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "20") Integer limit,
			ServerHttpRequest request
	) {
		log.debug("getSupportQueue: cursor={}, limit={}", cursor, limit);

		return chatClient.getSupportQueue(cursor, limit)
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "상담원 배정", description = "상담원을 대화에 배정합니다. (관리자/상담원용)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "409", description = "이미 상담원이 배정됨")
	})
	@PostMapping("/support/{conversationId}/assign")
	public Mono<ResponseEntity<BaseResponse>> assignAgent(
			@PathVariable String conversationId,
			@RequestHeader("X-Agent-Id") Long agentId,
			ServerHttpRequest request
	) {
		log.debug("assignAgent: conversationId={}, agentId={}", conversationId, agentId);

		return chatClient.assignAgent(conversationId, agentId)
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "상담 종료", description = "상담을 종료합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "412", description = "이미 종료된 대화")
	})
	@PostMapping("/support/{conversationId}/close")
	public Mono<ResponseEntity<BaseResponse>> closeSupportChat(
			@PathVariable String conversationId,
			@RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest request
	) {
		log.debug("closeSupportChat: conversationId={}, userId={}", conversationId, userId);

		return chatClient.closeSupport(conversationId, userId)
				.map(result -> responseFactory.ok(result, request));
	}
}
