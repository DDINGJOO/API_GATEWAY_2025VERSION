package com.study.api_gateway.service;

import com.study.api_gateway.client.ChatClient;
import com.study.api_gateway.dto.chat.enums.ChatRoomType;
import com.study.api_gateway.dto.chat.response.*;
import com.study.api_gateway.dto.profile.response.BatchUserSummaryResponse;
import com.study.api_gateway.util.ProfileEnrichmentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chat 응답에 프로필 정보를 병합하는 서비스
 * - 채팅방 목록: participantIds -> 참여자 프로필
 * - 메시지 목록: senderId -> 발신자 프로필
 * - 호스트 문의 목록: guestId -> 게스트 프로필
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEnrichmentService {

	private final ChatClient chatClient;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;

	private static final String DEFAULT_NICKNAME = "알 수 없음";
	private static final String DEFAULT_PROFILE_IMAGE = null;
	
	// ==================== DM 채팅방 생성 ====================
	
	/**
	 * DM 채팅방 생성 및 상대방 닉네임으로 roomName 설정
	 * - 요청한 사용자에게는 상대방의 닉네임이 채팅방 이름으로 보임
	 */
	public Mono<CreateDmRoomResponse> createDmRoomWithNickname(Long userId, com.study.api_gateway.dto.chat.request.CreateDmRoomRequest request) {
		return chatClient.createDmRoom(userId, request)
				.flatMap(response -> enrichDmRoomResponse(response, userId));
	}
	
	/**
	 * DM 응답에 상대방 닉네임 설정
	 */
	private Mono<CreateDmRoomResponse> enrichDmRoomResponse(CreateDmRoomResponse response, Long requestUserId) {
		if (response == null || response.getParticipantIds() == null || response.getParticipantIds().isEmpty()) {
			return Mono.just(response);
		}
		
		// 상대방 ID 찾기 (요청한 사용자를 제외한 참여자)
		Long recipientId = response.getParticipantIds().stream()
				.filter(id -> !id.equals(requestUserId))
				.findFirst()
				.orElse(null);
		
		if (recipientId == null) {
			return Mono.just(response);
		}
		
		// 상대방 프로필 조회
		Set<Long> userIds = Set.of(recipientId);
		return loadProfiles(userIds)
				.map(profileMap -> {
					BatchUserSummaryResponse profile = profileMap.get(recipientId);
					String name = (profile != null && profile.getNickname() != null)
							? profile.getNickname()
							: DEFAULT_NICKNAME;
					String profileImage = (profile != null) ? profile.getProfileImageUrl() : DEFAULT_PROFILE_IMAGE;
					
					return CreateDmRoomResponse.builder()
							.roomId(response.getRoomId())
							.type(response.getType())
							.name(name)
							.profileImage(profileImage)
							.participantIds(response.getParticipantIds())
							.createdAt(response.getCreatedAt())
							.isNewRoom(response.getIsNewRoom())
							.build();
				});
	}

	// ==================== 채팅방 목록 ====================

	/**
	 * 채팅방 목록 조회 및 프로필 병합
	 * - DM 타입인 경우 상대방의 닉네임을 채팅방 이름으로 설정
	 * @param type 채팅방 타입 필터 (DM, GROUP, PLACE_INQUIRY, SUPPORT) - optional
	 */
	public Mono<ChatRoomListResponse> getChatRoomsWithProfiles(Long userId, String type) {
		return chatClient.getChatRooms(userId, type)
				.flatMap(response -> enrichChatRooms(response, userId));
	}

	@SuppressWarnings("unchecked")
	private Mono<ChatRoomListResponse> enrichChatRooms(Map<String, Object> response, Long requestUserId) {
		Map<String, Object> data = extractData(response);
		List<Map<String, Object>> chatRooms = (List<Map<String, Object>>) data.get("chatRooms");

		if (chatRooms == null || chatRooms.isEmpty()) {
			return Mono.just(ChatRoomListResponse.builder().chatRooms(List.of()).build());
		}

		// 모든 participantIds 수집
		Set<Long> allParticipantIds = new LinkedHashSet<>();
		for (Map<String, Object> room : chatRooms) {
			List<Number> participantIds = (List<Number>) room.get("participantIds");
			if (participantIds != null) {
				participantIds.forEach(id -> allParticipantIds.add(id.longValue()));
			}
		}

		return loadProfiles(allParticipantIds)
				.map(profileMap -> {
					List<ChatRoomResponse> enrichedRooms = chatRooms.stream()
							.map(room -> buildChatRoomResponse(room, profileMap, requestUserId))
							.collect(Collectors.toList());

					return ChatRoomListResponse.builder()
							.chatRooms(enrichedRooms)
							.build();
				});
	}

	@SuppressWarnings("unchecked")
	private ChatRoomResponse buildChatRoomResponse(Map<String, Object> room, Map<Long, BatchUserSummaryResponse> profileMap, Long requestUserId) {
		List<Number> participantIds = (List<Number>) room.get("participantIds");
		List<ParticipantInfo> participants = participantIds != null
				? participantIds.stream()
				.map(id -> buildParticipantInfo(id.longValue(), profileMap))
				.collect(Collectors.toList())
				: List.of();
		
		// DM 타입인 경우 상대방의 닉네임과 프로필 이미지를 채팅방 정보로 설정
		ChatRoomType roomType = parseRoomType(room.get("type"));
		String roomName = (String) room.get("name");
		String profileImage = DEFAULT_PROFILE_IMAGE;
		
		if (roomType == ChatRoomType.DM && participantIds != null) {
			// 상대방 ID 찾기 (요청한 사용자를 제외한 참여자)
			Long recipientId = participantIds.stream()
					.map(Number::longValue)
					.filter(id -> !id.equals(requestUserId))
					.findFirst()
					.orElse(null);
			
			if (recipientId != null) {
				BatchUserSummaryResponse profile = profileMap.get(recipientId);
				roomName = (profile != null && profile.getNickname() != null)
						? profile.getNickname()
						: DEFAULT_NICKNAME;
				profileImage = (profile != null) ? profile.getProfileImageUrl() : DEFAULT_PROFILE_IMAGE;
			}
		}
		
		// context 정보 추출 (PLACE_INQUIRY 타입일 때 존재)
		ChatRoomResponse.ContextInfo contextInfo = null;
		Map<String, Object> context = (Map<String, Object>) room.get("context");
		if (context != null) {
			contextInfo = ChatRoomResponse.ContextInfo.builder()
					.contextType((String) context.get("contextType"))
					.contextId(toLong(context.get("contextId")))
					.contextName((String) context.get("contextName"))
					.build();
		}

		return ChatRoomResponse.builder()
				.roomId((String) room.get("roomId"))
				.type(roomType)
				.name(roomName)
				.profileImage(profileImage)
				.participants(participants)
				.lastMessage((String) room.get("lastMessage"))
				.lastMessageAt(parseDateTime(room.get("lastMessageAt")))
				.unreadCount(toLong(room.get("unreadCount")))
				.context(contextInfo)
				.build();
	}

	// ==================== 메시지 목록 ====================

	/**
	 * 메시지 목록 조회 및 프로필 병합
	 */
	public Mono<MessageListResponse> getMessagesWithProfiles(String roomId, Long userId, String cursor, Integer limit) {
		return chatClient.getMessages(roomId, userId, cursor, limit)
				.flatMap(this::enrichMessages);
	}

	@SuppressWarnings("unchecked")
	private Mono<MessageListResponse> enrichMessages(Map<String, Object> response) {
		Map<String, Object> data = extractData(response);
		List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("messages");

		if (messages == null || messages.isEmpty()) {
			return Mono.just(MessageListResponse.builder()
					.messages(List.of())
					.nextCursor((String) data.get("nextCursor"))
					.hasMore((Boolean) data.get("hasMore"))
					.build());
		}

		// 모든 senderId 수집
		Set<Long> senderIds = messages.stream()
				.map(m -> toLong(m.get("senderId")))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return loadProfiles(senderIds)
				.map(profileMap -> {
					List<MessageResponse> enrichedMessages = messages.stream()
							.map(msg -> buildMessageResponse(msg, profileMap))
							.collect(Collectors.toList());

					return MessageListResponse.builder()
							.messages(enrichedMessages)
							.nextCursor((String) data.get("nextCursor"))
							.hasMore((Boolean) data.get("hasMore"))
							.build();
				});
	}

	private MessageResponse buildMessageResponse(Map<String, Object> msg, Map<Long, BatchUserSummaryResponse> profileMap) {
		Long senderId = toLong(msg.get("senderId"));
		SenderInfo sender = senderId != null
				? buildSenderInfo(senderId, profileMap)
				: null;

		return MessageResponse.builder()
				.messageId((String) msg.get("messageId"))
				.roomId((String) msg.get("roomId"))
				.sender(sender)
				.content((String) msg.get("content"))
				.readCount(toInt(msg.get("readCount")))
				.deleted((Boolean) msg.get("deleted"))
				.createdAt(parseDateTime(msg.get("createdAt")))
				.build();
	}

	// ==================== 호스트 문의 목록 ====================

	/**
	 * 호스트 문의 목록 조회 및 프로필 병합
	 */
	public Mono<HostInquiryListResponse> getHostInquiriesWithProfiles(Long userId, Long placeId, String cursor, Integer limit) {
		return chatClient.getHostInquiries(userId, placeId, cursor, limit)
				.flatMap(this::enrichHostInquiries);
	}

	@SuppressWarnings("unchecked")
	private Mono<HostInquiryListResponse> enrichHostInquiries(Map<String, Object> response) {
		Map<String, Object> data = extractData(response);
		List<Map<String, Object>> inquiries = (List<Map<String, Object>>) data.get("inquiries");

		if (inquiries == null || inquiries.isEmpty()) {
			return Mono.just(HostInquiryListResponse.builder().inquiries(List.of()).build());
		}

		// 모든 guestId 수집
		Set<Long> guestIds = inquiries.stream()
				.map(inq -> toLong(inq.get("guestId")))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return loadProfiles(guestIds)
				.map(profileMap -> {
					List<HostInquiryResponse> enrichedInquiries = inquiries.stream()
							.map(inq -> buildHostInquiryResponse(inq, profileMap))
							.collect(Collectors.toList());

					return HostInquiryListResponse.builder()
							.inquiries(enrichedInquiries)
							.build();
				});
	}

	@SuppressWarnings("unchecked")
	private HostInquiryResponse buildHostInquiryResponse(Map<String, Object> inq, Map<Long, BatchUserSummaryResponse> profileMap) {
		Long guestId = toLong(inq.get("guestId"));
		ParticipantInfo guest = guestId != null
				? buildParticipantInfo(guestId, profileMap)
				: null;

		// context 정보 추출
		Map<String, Object> context = (Map<String, Object>) inq.get("context");
		Long placeId = context != null ? toLong(context.get("contextId")) : toLong(inq.get("placeId"));
		String placeName = context != null ? (String) context.get("contextName") : (String) inq.get("placeName");

		return HostInquiryResponse.builder()
				.roomId((String) inq.get("roomId"))
				.guest(guest)
				.placeId(placeId)
				.placeName(placeName)
				.lastMessage((String) inq.get("lastMessage"))
				.lastMessageAt(parseDateTime(inq.get("lastMessageAt")))
				.unreadCount(toLong(inq.get("unreadCount")))
				.build();
	}

	// ==================== 프로필 로딩 ====================
	
	/**
	 * 프로필 로딩 (ProfileEnrichmentUtil의 캐시 어사이드 패턴 사용)
	 */
	private Mono<Map<Long, BatchUserSummaryResponse>> loadProfiles(Set<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Mono.just(Map.of());
		}
		
		Set<String> stringIds = userIds.stream()
				.map(String::valueOf)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		log.debug("Loading profiles for {} userIds", stringIds.size());
		
		return profileEnrichmentUtil.loadProfiles(stringIds)
				.map(this::convertToLongKeyMap);
	}

	private Map<Long, BatchUserSummaryResponse> convertToLongKeyMap(Map<String, BatchUserSummaryResponse> stringKeyMap) {
		return stringKeyMap.entrySet().stream()
				.collect(Collectors.toMap(
						e -> Long.parseLong(e.getKey()),
						Map.Entry::getValue,
						(a, b) -> a
				));
	}

	// ==================== 헬퍼 메서드 ====================

	private ParticipantInfo buildParticipantInfo(Long userId, Map<Long, BatchUserSummaryResponse> profileMap) {
		BatchUserSummaryResponse profile = profileMap.get(userId);
		return ParticipantInfo.builder()
				.userId(userId)
				.nickname(profile != null && profile.getNickname() != null ? profile.getNickname() : DEFAULT_NICKNAME)
				.profileImage(profile != null ? profile.getProfileImageUrl() : DEFAULT_PROFILE_IMAGE)
				.build();
	}

	private SenderInfo buildSenderInfo(Long userId, Map<Long, BatchUserSummaryResponse> profileMap) {
		BatchUserSummaryResponse profile = profileMap.get(userId);
		return SenderInfo.builder()
				.userId(userId)
				.nickname(profile != null && profile.getNickname() != null ? profile.getNickname() : DEFAULT_NICKNAME)
				.profileImage(profile != null ? profile.getProfileImageUrl() : DEFAULT_PROFILE_IMAGE)
				.build();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractData(Map<String, Object> response) {
		Object data = response.get("data");
		if (data instanceof Map) {
			return (Map<String, Object>) data;
		}
		return response;
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Long) return (Long) value;
		if (value instanceof Integer) return ((Integer) value).longValue();
		if (value instanceof Number) return ((Number) value).longValue();
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private Integer toInt(Object value) {
		if (value == null) return null;
		if (value instanceof Integer) return (Integer) value;
		if (value instanceof Number) return ((Number) value).intValue();
		return null;
	}

	private LocalDateTime parseDateTime(Object value) {
		if (value == null) return null;
		if (value instanceof LocalDateTime) return (LocalDateTime) value;
		if (value instanceof String) {
			try {
				return LocalDateTime.parse((String) value);
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
}
