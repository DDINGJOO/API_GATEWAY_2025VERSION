package com.study.api_gateway.api.profile.service;

import com.study.api_gateway.api.profile.client.ProfileClient;
import com.study.api_gateway.api.profile.dto.request.ProfileUpdateRequest;
import com.study.api_gateway.api.profile.dto.response.BatchUserSummaryResponse;
import com.study.api_gateway.api.profile.dto.response.UserPageResponse;
import com.study.api_gateway.api.profile.dto.response.UserResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Profile 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ProfileFacadeService {

	private final ProfileClient profileClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "profile-service";

	public Mono<Map<Integer, String>> fetchGenres() {
		return profileClient.fetchGenres()
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Map<Integer, String>> fetchInstruments() {
		return profileClient.fetchInstruments()
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Map<String, String>> fetchLocations() {
		return profileClient.fetchLocations()
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Boolean> updateProfile(String userId, ProfileUpdateRequest req) {
		return profileClient.updateProfile(userId, req)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<UserResponse> fetchProfile(String userId) {
		return profileClient.fetchProfile(userId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Flux<UserResponse> fetchProfiles(String city, String nickname, List<Integer> genres, List<Integer> instruments, Character sex, String cursor, Integer size) {
		return profileClient.fetchProfiles(city, nickname, genres, instruments, sex, cursor, size)
				.transform(resilience.protectFlux(SERVICE_NAME));
	}

	public Mono<UserPageResponse> fetchProfilesWithPage(String city, String nickname, List<Integer> genres, List<Integer> instruments, Character sex, String cursor, Integer size) {
		return profileClient.fetchProfilesWithPage(city, nickname, genres, instruments, sex, cursor, size)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Boolean> validateProfile(String type, String value) {
		return profileClient.validateProfile(type, value)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<List<BatchUserSummaryResponse>> fetchUserSummariesBatch(List<String> userIds) {
		return profileClient.fetchUserSummariesBatch(userIds)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
