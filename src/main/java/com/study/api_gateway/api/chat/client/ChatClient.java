package com.study.api_gateway.api.chat.client;

import com.study.api_gateway.api.chat.dto.request.*;
import com.study.api_gateway.api.chat.dto.response.*;
import com.study.api_gateway.api.chat.dto.enums.ChatRoomType;
import com.study.api_gateway.api.chat.dto.enums.SupportStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class ChatClient {
	private final WebClient webClient;
	private static final String ROOMS_PREFIX = "/api/v1/rooms";
	private static final String CHAT_PREFIX = "/api/v1/chat";
	private static final String X_USER_ID = "X-User-Id";
	private static final String X_AGENT_ID = "X-Agent-Id";

	public ChatClient(@Qualifier(value = "chatWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	// ==================== 채팅방 API ====================

	/**
	 * 채팅방 목록 조회
	 * GET /api/v1/rooms?type={type}
	 * @param type 채팅방 타입 필터 (DM, GROUP, PLACE_INQUIRY, SUPPORT) - optional
	 */
	public Mono<Map<String, Object>> getChatRooms(Long userId, String type) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(ROOMS_PREFIX);
		
		if (type != null && !type.isBlank()) {
			builder.queryParam("type", type);
		}
		
		String uriString = builder.toUriString();
		
		log.debug("getChatRooms: userId={}, type={}, uri={}", userId, type, uriString);

		return webClient.get()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
	}

	/**
	 * 채팅방 상세 조회
	 * GET /api/v1/rooms/{roomId}
	 */
	public Mono<Map<String, Object>> getChatRoom(String roomId, Long userId) {
		String uriString = UriComponentsBuilder.fromPath(ROOMS_PREFIX + "/{roomId}")
				.buildAndExpand(roomId)
				.toUriString();

		log.debug("getChatRoom: roomId={}, userId={}", roomId, userId);

		return webClient.get()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
	}
	
	/**
	 * 1:1 DM 채팅방 생성
	 * POST /api/v1/rooms/dm
	 */
	public Mono<CreateDmRoomResponse> createDmRoom(Long userId, CreateDmRoomRequest request) {
		String uriString = UriComponentsBuilder.fromPath(ROOMS_PREFIX + "/dm")
				.toUriString();
		
		log.debug("createDmRoom: userId={}, recipientId={}", userId, request.getRecipientId());
		
		return webClient.post()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.map(this::extractCreateDmRoomResponse);
	}

	// ==================== 메시지 API ====================

	/**
	 * 메시지 목록 조회
	 * GET /api/v1/rooms/{roomId}/messages
	 */
	public Mono<Map<String, Object>> getMessages(String roomId, Long userId, String cursor, Integer limit) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(ROOMS_PREFIX + "/{roomId}/messages");

		if (cursor != null && !cursor.isBlank()) {
			builder.queryParam("cursor", cursor);
		}
		if (limit != null) {
			builder.queryParam("limit", limit);
		}

		String uriString = builder.buildAndExpand(roomId).toUriString();

		log.debug("getMessages: roomId={}, userId={}, cursor={}, limit={}", roomId, userId, cursor, limit);

		return webClient.get()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
	}

	/**
	 * 메시지 전송
	 * POST /api/v1/rooms/{roomId}/messages
	 */
	public Mono<SendMessageResponse> sendMessage(String roomId, Long userId, SendMessageRequest request) {
		String uriString = UriComponentsBuilder.fromPath(ROOMS_PREFIX + "/{roomId}/messages")
				.buildAndExpand(roomId)
				.toUriString();

		log.debug("sendMessage: roomId={}, userId={}", roomId, userId);

		return webClient.post()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractSendMessageResponse);
	}

	/**
	 * 읽음 처리
	 * POST /api/v1/rooms/{roomId}/messages/read
	 */
	public Mono<ReadMessageResponse> markAsRead(String roomId, Long userId, ReadMessageRequest request) {
		String uriString = UriComponentsBuilder.fromPath(ROOMS_PREFIX + "/{roomId}/messages/read")
				.buildAndExpand(roomId)
				.toUriString();

		log.debug("markAsRead: roomId={}, userId={}", roomId, userId);

		return webClient.post()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.bodyValue(request != null ? request : Map.of())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractReadMessageResponse);
	}

	/**
	 * 메시지 삭제
	 * DELETE /api/v1/rooms/{roomId}/messages/{messageId}
	 */
	public Mono<DeleteMessageResponse> deleteMessage(String roomId, String messageId, Long userId) {
		String uriString = UriComponentsBuilder.fromPath(ROOMS_PREFIX + "/{roomId}/messages/{messageId}")
				.buildAndExpand(roomId, messageId)
				.toUriString();

		log.debug("deleteMessage: roomId={}, messageId={}, userId={}", roomId, messageId, userId);

		return webClient.delete()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractDeleteMessageResponse);
	}

	// ==================== 공간 문의 API ====================

	/**
	 * 공간 문의 생성
	 * POST /api/v1/chat/inquiry
	 */
	public Mono<PlaceInquiryResponse> createPlaceInquiry(Long userId, PlaceInquiryRequest request) {
		String uriString = UriComponentsBuilder.fromPath(CHAT_PREFIX + "/inquiry")
				.toUriString();

		log.debug("createPlaceInquiry: userId={}, placeId={}", userId, request.getPlaceId());

		return webClient.post()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractPlaceInquiryResponse);
	}

	/**
	 * 호스트 문의 목록 조회
	 * GET /api/v1/chat/inquiry/host
	 */
	public Mono<Map<String, Object>> getHostInquiries(Long userId, Long placeId, String cursor, Integer limit) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(CHAT_PREFIX + "/inquiry/host");

		if (placeId != null) {
			builder.queryParam("placeId", placeId);
		}
		if (cursor != null && !cursor.isBlank()) {
			builder.queryParam("cursor", cursor);
		}
		if (limit != null) {
			builder.queryParam("limit", limit);
		}

		String uriString = builder.toUriString();

		log.debug("getHostInquiries: userId={}, placeId={}, cursor={}, limit={}", userId, placeId, cursor, limit);

		return webClient.get()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
	}

	// ==================== 고객 상담 API ====================

	/**
	 * 상담 요청 생성
	 * POST /api/v1/chat/support
	 */
	public Mono<SupportResponse> createSupport(Long userId, SupportRequest request) {
		String uriString = UriComponentsBuilder.fromPath(CHAT_PREFIX + "/support")
				.toUriString();

		log.debug("createSupport: userId={}", userId);

		return webClient.post()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.bodyValue(request != null ? request : Map.of())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractSupportResponse);
	}

	/**
	 * 상담 대기열 조회
	 * GET /api/v1/chat/support/queue
	 */
	public Mono<Map<String, Object>> getSupportQueue(String cursor, Integer limit) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(CHAT_PREFIX + "/support/queue");

		if (cursor != null && !cursor.isBlank()) {
			builder.queryParam("cursor", cursor);
		}
		if (limit != null) {
			builder.queryParam("limit", limit);
		}

		String uriString = builder.toUriString();

		log.debug("getSupportQueue: cursor={}, limit={}", cursor, limit);

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
	}

	/**
	 * 상담원 배정
	 * POST /api/v1/chat/support/{roomId}/assign
	 */
	public Mono<SupportResponse> assignAgent(String roomId, Long agentId) {
		String uriString = UriComponentsBuilder.fromPath(CHAT_PREFIX + "/support/{roomId}/assign")
				.buildAndExpand(roomId)
				.toUriString();

		log.debug("assignAgent: roomId={}, agentId={}", roomId, agentId);

		return webClient.post()
				.uri(uriString)
				.header(X_AGENT_ID, String.valueOf(agentId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractSupportResponse);
	}

	/**
	 * 상담 종료
	 * POST /api/v1/chat/support/{roomId}/close
	 */
	public Mono<SupportResponse> closeSupport(String roomId, Long userId) {
		String uriString = UriComponentsBuilder.fromPath(CHAT_PREFIX + "/support/{roomId}/close")
				.buildAndExpand(roomId)
				.toUriString();

		log.debug("closeSupport: roomId={}, userId={}", roomId, userId);

		return webClient.post()
				.uri(uriString)
				.header(X_USER_ID, String.valueOf(userId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractSupportResponse);
	}

	// ==================== Response 변환 헬퍼 ====================
	
	@SuppressWarnings("unchecked")
	private CreateDmRoomResponse extractCreateDmRoomResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}
		
		java.util.List<Long> participantIds = null;
		Object participantIdsObj = data.get("participantIds");
		if (participantIdsObj instanceof java.util.List) {
			participantIds = ((java.util.List<?>) participantIdsObj).stream()
					.map(this::toLong)
					.filter(java.util.Objects::nonNull)
					.toList();
		}
		
		return CreateDmRoomResponse.builder()
				.roomId((String) data.get("roomId"))
				.type(parseRoomType(data.get("type")))
				.participantIds(participantIds)
				.createdAt(parseDateTime(data.get("createdAt")))
				.isNewRoom((Boolean) data.get("isNewRoom"))
				.build();
	}

	@SuppressWarnings("unchecked")
	private SendMessageResponse extractSendMessageResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}

		return SendMessageResponse.builder()
				.messageId((String) data.get("messageId"))
				.roomId((String) data.get("roomId"))
				.senderId(toLong(data.get("senderId")))
				.content((String) data.get("content"))
				.createdAt(parseDateTime(data.get("createdAt")))
				.build();
	}

	@SuppressWarnings("unchecked")
	private ReadMessageResponse extractReadMessageResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}

		return ReadMessageResponse.builder()
				.roomId((String) data.get("roomId"))
				.lastReadAt(parseDateTime(data.get("lastReadAt")))
				.unreadCount(toLong(data.get("unreadCount")))
				.build();
	}

	@SuppressWarnings("unchecked")
	private DeleteMessageResponse extractDeleteMessageResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}

		return DeleteMessageResponse.builder()
				.messageId((String) data.get("messageId"))
				.hardDeleted((Boolean) data.get("hardDeleted"))
				.deletedAt(parseDateTime(data.get("deletedAt")))
				.build();
	}

	@SuppressWarnings("unchecked")
	private PlaceInquiryResponse extractPlaceInquiryResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}

		PlaceInquiryResponse.ContextInfo contextInfo = null;
		Map<String, Object> context = (Map<String, Object>) data.get("context");
		if (context != null) {
			contextInfo = PlaceInquiryResponse.ContextInfo.builder()
					.contextType((String) context.get("contextType"))
					.contextId(toLong(context.get("contextId")))
					.contextName((String) context.get("contextName"))
					.build();
		}

		return PlaceInquiryResponse.builder()
				.roomId((String) data.get("roomId"))
				.type(parseRoomType(data.get("type")))
				.context(contextInfo)
				.createdAt(parseDateTime(data.get("createdAt")))
				.build();
	}

	@SuppressWarnings("unchecked")
	private SupportResponse extractSupportResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}

		return SupportResponse.builder()
				.roomId((String) data.get("roomId"))
				.agentId(toLong(data.get("agentId")))
				.status(parseSupportStatus(data.get("status")))
				.createdAt(parseDateTime(data.get("createdAt")))
				.build();
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Long) return (Long) value;
		if (value instanceof Integer) return ((Integer) value).longValue();
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private java.time.LocalDateTime parseDateTime(Object value) {
		if (value == null) return null;
		if (value instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) value;
		if (value instanceof String) {
			try {
				return java.time.LocalDateTime.parse((String) value);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	private ChatRoomType parseRoomType(Object value) {
		if (value == null) return null;
		try {
			return ChatRoomType.valueOf(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	private SupportStatus parseSupportStatus(Object value) {
		if (value == null) return null;
		try {
			return SupportStatus.valueOf(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}
}
