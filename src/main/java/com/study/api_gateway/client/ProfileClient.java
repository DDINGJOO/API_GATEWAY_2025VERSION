package com.study.api_gateway.client;


import com.study.api_gateway.dto.profile.ProfileSearchCriteria;
import com.study.api_gateway.dto.profile.request.ProfileUpdateRequest;
import com.study.api_gateway.dto.profile.response.BatchUserSummaryResponse;
import com.study.api_gateway.dto.profile.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
/**
 * 프로필 서버와 통신하는 WebClient 기반 클라이언트
 * - 프로필 검색/수정, Enums 조회, 배치 프로필 요약 조회 등을 제공합니다.
 */
public class ProfileClient {
    private final WebClient webClient;
    private final String PREFIX = "/api/profiles";

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

        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/genres")
                .toUriString();

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {});
    }
	
	/**
	 * 악기 Enum 목록 조회
	 * GET /api/profiles/enums/instruments
	 * @return key: 악기 ID, value: 악기명
	 */
    public Mono<Map<Integer, String>> fetchInstruments() {
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/instruments")
                .toUriString();
        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {});
    }
	
	/**
	 * 지역(도시) Enum 목록 조회
	 * GET /api/profiles/enums/locations
	 * @return key: 지역 코드, value: 지역명
	 */
    public Mono<Map<String, String>> fetchLocations() {
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/locations")
                .toUriString();
        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }

//
//    public Mono<Boolean> updateProfileVer1(String userId, ProfileUpdateRequest req)
//    {
//
//        String uriString = UriComponentsBuilder.fromPath(PREFIX +"/profiles/"+userId +"/ver1")
//                .toUriString();
//
//        return webClient.put()
//                .uri(uriString)
//                .bodyValue(req)
//                .retrieve()
//                .bodyToMono(Boolean.class);
//    }

    public Mono<Boolean> updateProfileVer2(String userId, ProfileUpdateRequest req)
    {

        String uriString = UriComponentsBuilder.fromPath(PREFIX +"/profiles/"+ userId +"/ver2")
                .toUriString();

        return webClient.put()
                .uri(uriString)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<UserResponse> fetchProfile(String userId){
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/profiles/" + userId)
                .toUriString();

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }
	
	public Flux<UserResponse> fetchProfiles(ProfileSearchCriteria req, String cursor, int size) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/profiles")
                .queryParam("city", req.getCity())
                .queryParam("genres", req.getGenres())
                .queryParam("instruments", req.getInstruments())
                .queryParam("sex", req.getSex())
                .queryParam("cursor", cursor)
                .queryParam("size", size)
                .toUriString();
		
		log.info("fetchProfiles uriString : {}", uriString);

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToFlux(UserResponse.class);
    }

    public Mono<Boolean> validateProfile(String type, String value ){
        String uriString = UriComponentsBuilder.fromPath(PREFIX + "/validate")
                .queryParam("type", type)
                .queryParam("value", value)
                .toUriString();


        return webClient.post()
                .uri(uriString)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
	
	public Mono<java.util.List<BatchUserSummaryResponse>> fetchUserSummariesBatch(java.util.List<String> userIds) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/profiles/batch")
				.toUriString();
		return webClient.post()
				.uri(uriString)
				.bodyValue(userIds)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<java.util.List<BatchUserSummaryResponse>>() {
				});
    }
}
