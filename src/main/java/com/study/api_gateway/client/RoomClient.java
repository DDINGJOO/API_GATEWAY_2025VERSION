package com.study.api_gateway.client;

import com.study.api_gateway.dto.room.response.RoomDetailResponse;
import com.study.api_gateway.dto.room.response.RoomSimpleResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Room Server와 통신하는 WebClient 기반 클라이언트
 * 조회 전용 API 제공
 */
@Component
public class RoomClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/rooms";
	
	public RoomClient(@Qualifier("roomWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 룸 상세 조회 API
	 * GET /api/rooms/{roomId}
	 */
	public Mono<RoomDetailResponse> getRoomById(Long roomId) {
		String uriString = PREFIX + "/" + roomId;
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(RoomDetailResponse.class);
	}
	
	/**
	 * 룸 검색 API
	 * GET /api/rooms/search
	 */
	public Mono<List<RoomSimpleResponse>> searchRooms(
			String roomName,
			List<Long> keywordIds,
			Long placeId
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/search");
		
		if (roomName != null) builder.queryParam("roomName", roomName);
		if (keywordIds != null && !keywordIds.isEmpty()) {
			keywordIds.forEach(id -> builder.queryParam("keywordIds", id));
		}
		if (placeId != null) builder.queryParam("placeId", placeId);
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(RoomSimpleResponse.class)
				.collectList();
	}
	
	/**
	 * 특정 장소의 룸 목록 조회 API
	 * GET /api/rooms/place/{placeId}
	 */
	public Mono<List<RoomSimpleResponse>> getRoomsByPlaceId(Long placeId) {
		String uriString = PREFIX + "/place/" + placeId;
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(RoomSimpleResponse.class)
				.collectList();
	}
	
	/**
	 * 여러 룸 일괄 조회 API
	 * GET /api/rooms/batch?ids=1,2,3
	 */
	public Mono<List<RoomDetailResponse>> getRoomsByIds(List<Long> ids) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/batch");

		if (ids != null && !ids.isEmpty()) {
			ids.forEach(id -> builder.queryParam("ids", id));
		}
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(RoomDetailResponse.class)
				.collectList();
	}
	
	/**
	 * 룸 키워드 맵 조회 API
	 * GET /api/rooms/keywords
	 */
	public Mono<java.util.Map<Long, com.study.api_gateway.dto.room.response.RoomKeywordResponse>> getRoomKeywordMap() {
		String uriString = PREFIX + "/keywords";
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<Long, com.study.api_gateway.dto.room.response.RoomKeywordResponse>>() {
				});
	}
}
