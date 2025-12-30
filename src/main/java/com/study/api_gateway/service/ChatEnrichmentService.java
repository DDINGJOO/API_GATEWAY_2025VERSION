package com.study.api_gateway.service;

import com.study.api_gateway.client.ChatClient;
import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.chat.enums.ChatRoomType;
import com.study.api_gateway.dto.chat.response.*;
import com.study.api_gateway.dto.profile.response.BatchUserSummaryResponse;
import com.study.api_gateway.util.cache.ProfileCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
	private final ProfileClient profileClient;
	private final ProfileCache profileCache;

	private static final String DEFAULT_NICKNAME = "알 수 없음";
	private static final String DEFAULT_PROFILE_IMAGE = null;
	private static final int BATCH_SIZE = 200;

	// ==================== 채팅방 목록 ====================

	/**
	 * 채팅방 목록 조회 및 프로필 병합
	 */
	public Mono<ChatRoomListResponse> getChatRoomsWithProfiles(Long userId) {
		return chatClient.getChatRooms(userId)
				.flatMap(this::enrichChatRooms);
	}

	@SuppressWarnings("unchecked")
	private Mono<ChatRoomListResponse> enrichChatRooms(Map<String, Object> response) {
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
							.map(room -> buildChatRoomResponse(room, profileMap))
							.collect(Collectors.toList());

					return ChatRoomListResponse.builder()
							.chatRooms(enrichedRooms)
							.build();
				});
	}

	@SuppressWarnings("unchecked")
	private ChatRoomResponse buildChatRoomResponse(Map<String, Object> room, Map<Long, BatchUserSummaryResponse> profileMap) {
		List<Number> participantIds = (List<Number>) room.get("participantIds");
		List<ParticipantInfo> participants = participantIds != null
				? participantIds.stream()
				.map(id -> buildParticipantInfo(id.longValue(), profileMap))
				.collect(Collectors.toList())
				: List.of();

		return ChatRoomResponse.builder()
				.roomId((String) room.get("roomId"))
				.type(parseRoomType(room.get("type")))
				.name((String) room.get("name"))
				.participants(participants)
				.lastMessage((String) room.get("lastMessage"))
				.lastMessageAt(parseDateTime(room.get("lastMessageAt")))
				.unreadCount(toLong(room.get("unreadCount")))
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

	private Mono<Map<Long, BatchUserSummaryResponse>> loadProfiles(Set<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Mono.just(Map.of());
		}

		List<String> stringIds = userIds.stream()
				.map(String::valueOf)
				.collect(Collectors.toList());

		log.debug("Loading profiles for {} userIds", stringIds.size());

		return profileCache.getAll(new LinkedHashSet<>(stringIds))
				.onErrorResume(e -> {
					log.warn("Profile cache getAll failed: {}", e.getMessage());
					return Mono.just(Map.of());
				})
				.defaultIfEmpty(Map.of())
				.flatMap(cached -> {
					Set<String> missing = new LinkedHashSet<>(stringIds);
					missing.removeAll(cached.keySet());

					if (missing.isEmpty()) {
						return Mono.just(convertToLongKeyMap(cached));
					}

					log.debug("Cache miss for {} userIds, fetching from API", missing.size());

					return fetchProfilesInBatches(new ArrayList<>(missing))
							.map(fetched -> {
								// 캐시에 저장 (fire-and-forget)
								if (!fetched.isEmpty()) {
									profileCache.putAll(fetched)
											.doOnError(e -> log.warn("Failed to cache profiles: {}", e.getMessage()))
											.subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
											.subscribe();
								}

								Map<String, BatchUserSummaryResponse> merged = new LinkedHashMap<>(cached);
								merged.putAll(fetched);
								return convertToLongKeyMap(merged);
							});
				});
	}

	private Mono<Map<String, BatchUserSummaryResponse>> fetchProfilesInBatches(List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return Mono.just(Map.of());
		}

		List<List<String>> batches = new ArrayList<>();
		for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
			batches.add(ids.subList(i, Math.min(i + BATCH_SIZE, ids.size())));
		}

		return reactor.core.publisher.Flux.fromIterable(batches)
				.concatMap(batch -> profileClient.fetchUserSummariesBatch(batch)
						.onErrorResume(e -> {
							log.warn("Failed to fetch profiles batch: {}", e.getMessage());
							return Mono.just(Collections.emptyList());
						}))
				.collectList()
				.map(lists -> lists.stream()
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.filter(Objects::nonNull)
						.collect(Collectors.toMap(
								BatchUserSummaryResponse::getUserId,
								Function.identity(),
								(a, b) -> a
						)));
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
