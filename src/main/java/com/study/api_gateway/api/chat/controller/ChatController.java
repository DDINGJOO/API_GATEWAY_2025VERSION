package com.study.api_gateway.api.chat.controller;

import com.study.api_gateway.api.chat.service.ChatFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.chat.dto.request.*;
import com.study.api_gateway.enrichment.ChatEnrichmentService;
import com.study.api_gateway.common.response.ResponseFactory;
import io.swagger.v3.oas.annotations.Parameter;
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
public class ChatController implements ChatApi {

	private final ChatFacadeService chatFacadeService;
	private final ChatEnrichmentService chatEnrichmentService;
	private final ResponseFactory responseFactory;
	
	/**
	 * JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
	 */
	private Long extractUserId(ServerHttpRequest request) {
		String userIdStr = request.getHeaders().getFirst("X-User-Id");
		if (userIdStr == null || userIdStr.isEmpty()) {
			return null;
		}
		return Long.parseLong(userIdStr);
	}
	
	private Mono<ResponseEntity<BaseResponse>> unauthorizedResponse(ServerHttpRequest request) {
		return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
	}
	
	@Override
	@PostMapping("/rooms/dm")
	public Mono<ResponseEntity<BaseResponse>> createDmRoom(
			@RequestBody CreateDmRoomRequest dmRequest,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("createDmRoom: userId={}, recipientId={}", userId, dmRequest.getRecipientId());
		
		return chatEnrichmentService.createDmRoomWithNickname(userId, dmRequest)
				.map(result -> {
					HttpStatus status = Boolean.TRUE.equals(result.getIsNewRoom())
							? HttpStatus.CREATED
							: HttpStatus.OK;
					return responseFactory.ok(result, request, status);
				});
	}

	@Override
	@GetMapping("/conversations")
	public Mono<ResponseEntity<BaseResponse>> getConversations(
			@Parameter(description = "채팅방 타입 필터 (DM, GROUP, PLACE_INQUIRY, SUPPORT)")
			@RequestParam(required = false) String type,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("getConversations: userId={}, type={}", userId, type);
		
		return chatEnrichmentService.getChatRoomsWithProfiles(userId, type)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@GetMapping("/conversations/{conversationId}")
	public Mono<ResponseEntity<BaseResponse>> getConversation(
			@PathVariable String conversationId,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("getConversation: conversationId={}, userId={}", conversationId, userId);

		return chatFacadeService.getChatRoom(conversationId, userId)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@GetMapping("/conversations/{conversationId}/messages")
	public Mono<ResponseEntity<BaseResponse>> getMessages(
			@PathVariable String conversationId,
			@Parameter(description = "페이징 커서 (마지막 messageId)")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "50") Integer limit,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("getMessages: conversationId={}, userId={}, cursor={}, limit={}", conversationId, userId, cursor, limit);

		return chatEnrichmentService.getMessagesWithProfiles(conversationId, userId, cursor, limit)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/conversations/{conversationId}/messages")
	public Mono<ResponseEntity<BaseResponse>> sendMessage(
			@PathVariable String conversationId,
			@RequestBody SendMessageRequest messageRequest,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("sendMessage: conversationId={}, userId={}", conversationId, userId);

		return chatFacadeService.sendMessage(conversationId, userId, messageRequest)
				.map(result -> responseFactory.ok(result, request, HttpStatus.CREATED));
	}

	@Override
	@PostMapping("/conversations/{conversationId}/messages/read")
	public Mono<ResponseEntity<BaseResponse>> markAsRead(
			@PathVariable String conversationId,
			@RequestBody(required = false) ReadMessageRequest readRequest,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("markAsRead: conversationId={}, userId={}", conversationId, userId);

		return chatFacadeService.markAsRead(conversationId, userId, readRequest)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
	public Mono<ResponseEntity<BaseResponse>> deleteMessage(
			@PathVariable String conversationId,
			@PathVariable String messageId,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("deleteMessage: conversationId={}, messageId={}, userId={}", conversationId, messageId, userId);

		return chatFacadeService.deleteMessage(conversationId, messageId, userId)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/inquiry")
	public Mono<ResponseEntity<BaseResponse>> createPlaceInquiry(
			@RequestBody PlaceInquiryRequest inquiryRequest,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("createPlaceInquiry: userId={}, placeId={}", userId, inquiryRequest.getPlaceId());

		return chatFacadeService.createPlaceInquiry(userId, inquiryRequest)
				.map(result -> responseFactory.ok(result, request, HttpStatus.CREATED));
	}

	@Override
	@GetMapping("/inquiry/host")
	public Mono<ResponseEntity<BaseResponse>> getHostInquiries(
			@Parameter(description = "공간 ID (특정 공간의 문의만 조회)")
			@RequestParam(required = false) Long placeId,
			@Parameter(description = "페이징 커서")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "20") Integer limit,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("getHostInquiries: userId={}, placeId={}, cursor={}, limit={}", userId, placeId, cursor, limit);

		return chatEnrichmentService.getHostInquiriesWithProfiles(userId, placeId, cursor, limit)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/support")
	public Mono<ResponseEntity<BaseResponse>> createSupportRequest(
			@RequestBody(required = false) SupportRequest supportRequest,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("createSupportRequest: userId={}", userId);

		return chatFacadeService.createSupport(userId, supportRequest)
				.map(result -> responseFactory.ok(result, request, HttpStatus.CREATED));
	}

	@Override
	@GetMapping("/support/queue")
	public Mono<ResponseEntity<BaseResponse>> getSupportQueue(
			@Parameter(description = "페이징 커서")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "조회 개수")
			@RequestParam(required = false, defaultValue = "20") Integer limit,
			ServerHttpRequest request
	) {
		log.debug("getSupportQueue: cursor={}, limit={}", cursor, limit);

		return chatFacadeService.getSupportQueue(cursor, limit)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/support/{conversationId}/assign")
	public Mono<ResponseEntity<BaseResponse>> assignAgent(
			@PathVariable String conversationId,
			ServerHttpRequest request
	) {
		String agentIdStr = request.getHeaders().getFirst("X-Agent-Id");
		if (agentIdStr == null || agentIdStr.isEmpty()) {
			return Mono.just(responseFactory.error("상담원 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		Long agentId = Long.parseLong(agentIdStr);
		log.debug("assignAgent: conversationId={}, agentId={}", conversationId, agentId);

		return chatFacadeService.assignAgent(conversationId, agentId)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/support/{conversationId}/close")
	public Mono<ResponseEntity<BaseResponse>> closeSupportChat(
			@PathVariable String conversationId,
			ServerHttpRequest request
	) {
		Long userId = extractUserId(request);
		if (userId == null) {
			return unauthorizedResponse(request);
		}
		log.debug("closeSupportChat: conversationId={}, userId={}", conversationId, userId);

		return chatFacadeService.closeSupport(conversationId, userId)
				.map(result -> responseFactory.ok(result, request));
	}
}
