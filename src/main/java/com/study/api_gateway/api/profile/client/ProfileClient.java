package com.study.api_gateway.api.profile.client;


import com.study.api_gateway.api.profile.dto.request.ProfileUpdateRequest;
import com.study.api_gateway.api.profile.dto.response.BatchUserSummaryResponse;
import com.study.api_gateway.api.profile.dto.response.UserPageResponse;
import com.study.api_gateway.api.profile.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
/**
 * 프로필 서버와 통신하는 WebClient 기반 클라이언트
 * - 프로필 검색/수정, Enums 조회, 배치 프로필 요약 조회 등을 제공합니다.
 */
public class ProfileClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1/profiles";
	
	public ProfileClient(@Qualifier(value = "profileWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	
	/**
	 * 장르 Enum 목록 조회
	 * GET /api/profiles/enums/genres
	 *
	 * @return key: 장르 ID, value: 장르명
	 */
	public Mono<Map<Integer, String>> fetchGenres() {
		
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/genres")
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {
				});
	}
	
	/**
	 * 악기 Enum 목록 조회
	 * GET /api/profiles/enums/instruments
	 *
	 * @return key: 악기 ID, value: 악기명
	 */
	public Mono<Map<Integer, String>> fetchInstruments() {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/instruments")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {
				});
	}
	
	/**
	 * 지역(도시) Enum 목록 조회
	 * GET /api/profiles/enums/locations
	 *
	 * @return key: 지역 코드, value: 지역명
	 */
	public Mono<Map<String, String>> fetchLocations() {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/locations")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
				});
	}
	
	
	public Mono<Boolean> updateProfile(String userId, ProfileUpdateRequest req) {
		
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/" + userId)
				.toUriString();
		
		return webClient.put()
				.uri(uriString)
				.bodyValue(req)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<UserResponse> fetchProfile(String userId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/" + userId)
				.toUriString();
		
		log.info("=== ProfileClient.fetchProfile === requesting: {}", uriString);

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(UserResponse.class)
				.doOnError(e -> log.error("=== ProfileClient.fetchProfile ERROR === {}", e.getMessage()));
	}
	
	public Flux<UserResponse> fetchProfiles(String city, String nickname, List<Integer> genres, List<Integer> instruments, Character sex, String cursor, Integer size) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX);
		// 프로필 서버는 제공된 필터만 전달해야 하며, null/빈 값은 쿼리에 포함하지 않습니다.
		if (city != null && !city.isBlank()) builder.queryParam("city", city);
		// nickname 파라미터명은 백엔드 스펙에 맞춰 nickName 사용
		if (nickname != null && !nickname.isBlank()) builder.queryParam("nickName", nickname);
		if (genres != null && !genres.isEmpty())
			builder.queryParam("genres", String.join(",", genres.stream().map(String::valueOf).toList()));
		if (instruments != null && !instruments.isEmpty())
			builder.queryParam("instruments", String.join(",", instruments.stream().map(String::valueOf).toList()));
		if (sex != null) builder.queryParam("sex", sex);
		if (cursor != null && !cursor.isBlank()) builder.queryParam("cursor", cursor);
		if (size != null) builder.queryParam("size", size);
		String uriString = builder.toUriString();
		
		log.info("fetchProfiles uriString : {}", uriString);
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(UserPageResponse.class)
				.flatMapMany(page -> Flux.fromIterable(page == null || page.getContent() == null ? List.of() : page.getContent()));
	}
	
	/**
	 * 프로필 목록 조회 (페이지네이션 메타데이터 포함)
	 * 스펙에 맞춰 Slice 전체 정보를 반환합니다.
	 */
	public Mono<UserPageResponse> fetchProfilesWithPage(String city, String nickname, List<Integer> genres, List<Integer> instruments, Character sex, String cursor, Integer size) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX);
		// 프로필 서버는 제공된 필터만 전달해야 하며, null/빈 값은 쿼리에 포함하지 않습니다.
		if (city != null && !city.isBlank()) builder.queryParam("city", city);
		// nickname 파라미터명은 백엔드 스펙에 맞춰 nickName 사용
		if (nickname != null && !nickname.isBlank()) builder.queryParam("nickName", nickname);
		if (genres != null && !genres.isEmpty())
			builder.queryParam("genres", String.join(",", genres.stream().map(String::valueOf).toList()));
		if (instruments != null && !instruments.isEmpty())
			builder.queryParam("instruments", String.join(",", instruments.stream().map(String::valueOf).toList()));
		if (sex != null) builder.queryParam("sex", sex);
		if (cursor != null && !cursor.isBlank()) builder.queryParam("cursor", cursor);
		if (size != null) builder.queryParam("size", size);
		String uriString = builder.toUriString();
		
		log.info("fetchProfilesWithPage uriString : {}", uriString);
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(UserPageResponse.class);
	}
	
	public Mono<Boolean> validateProfile(String type, String value) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/validate")
				.queryParam("type", type)
				.queryParam("value", value)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<List<BatchUserSummaryResponse>> fetchUserSummariesBatch(List<String> userIds) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/batch")
				.queryParam("detail", false)
				.toUriString();
		return webClient.post()
				.uri(uriString)
				.bodyValue(userIds)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<BatchUserSummaryResponse>>() {
				})
				.timeout(java.time.Duration.ofSeconds(2))
				.retryWhen(Retry.backoff(2, java.time.Duration.ofMillis(200))
						.filter(this::isTransient))
				.onErrorResume(e -> {
					log.warn("fetchUserSummariesBatch failed for ids.size={} : {}", userIds == null ? 0 : userIds.size(), e.toString());
					return Mono.just(Collections.emptyList());
				});
	}
	
	
	private boolean isTransient(Throwable t) {
		// 네트워크 지연, 일시적 장애로 추정되는 경우에만 재시도
		if (t instanceof TimeoutException) return true;
		if (t instanceof WebClientRequestException) return true;
		if (t instanceof WebClientResponseException ex) {
			int status = ex.getRawStatusCode();
			return status >= 500 && status < 600; // 서버 오류만 재시도
		}
		return false;
	}
}
