package com.study.api_gateway.api.chat.service;

import com.study.api_gateway.api.chat.client.ChatClient;
import com.study.api_gateway.api.chat.dto.request.*;
import com.study.api_gateway.api.chat.dto.response.*;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Chat 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ChatFacadeService {
	
	private static final String SERVICE_NAME = "chat-service";
	private final ChatClient chatClient;
	private final ResilienceOperator resilience;
	
	// ==================== 채팅방 API ====================
	
	public Mono<Map<String, Object>> getChatRooms(Long userId, String type) {
		return chatClient.getChatRooms(userId, type)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Object>> getChatRoom(String roomId, Long userId) {
		return chatClient.getChatRoom(roomId, userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CreateDmRoomResponse> createDmRoom(Long userId, CreateDmRoomRequest request) {
		return chatClient.createDmRoom(userId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 메시지 API ====================
	
	public Mono<Map<String, Object>> getMessages(String roomId, Long userId, String cursor, Integer limit) {
		return chatClient.getMessages(roomId, userId, cursor, limit)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<SendMessageResponse> sendMessage(String roomId, Long userId, SendMessageRequest request) {
		return chatClient.sendMessage(roomId, userId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ReadMessageResponse> markAsRead(String roomId, Long userId, ReadMessageRequest request) {
		return chatClient.markAsRead(roomId, userId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<DeleteMessageResponse> deleteMessage(String roomId, String messageId, Long userId) {
		return chatClient.deleteMessage(roomId, messageId, userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 공간 문의 API ====================
	
	public Mono<PlaceInquiryResponse> createPlaceInquiry(Long userId, PlaceInquiryRequest request) {
		return chatClient.createPlaceInquiry(userId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Object>> getHostInquiries(Long userId, Long placeId, String cursor, Integer limit) {
		return chatClient.getHostInquiries(userId, placeId, cursor, limit)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 고객 상담 API ====================
	
	public Mono<SupportResponse> createSupport(Long userId, SupportRequest request) {
		return chatClient.createSupport(userId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Object>> getSupportQueue(String cursor, Integer limit) {
		return chatClient.getSupportQueue(cursor, limit)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<SupportResponse> assignAgent(String roomId, Long agentId) {
		return chatClient.assignAgent(roomId, agentId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<SupportResponse> closeSupport(String roomId, Long userId) {
		return chatClient.closeSupport(roomId, userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
