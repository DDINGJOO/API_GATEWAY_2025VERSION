package com.study.api_gateway.dto.profile.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO to deserialize paginated responses from Profile service.
 * The service returns a Spring Slice-like JSON with pagination metadata.
 * Includes all Slice fields as per the API specification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "UserPageResponse", description = "프로필 사용자 페이지 응답 (Slice 기반)")
public class UserPageResponse {
	@Schema(description = "페이지 콘텐츠 목록")
	private List<UserResponse> content;
	
	@Schema(description = "페이지 정보")
	private Pageable pageable;
	
	@Schema(description = "페이지 크기")
	private Integer size;
	
	@Schema(description = "현재 페이지 번호")
	private Integer number;
	
	@Schema(description = "정렬 정보")
	private Sort sort;
	
	@Schema(description = "현재 페이지의 요소 개수")
	private Integer numberOfElements;
	
	@Schema(description = "첫 페이지 여부")
	private Boolean first;
	
	@Schema(description = "마지막 페이지 여부")
	private Boolean last;
	
	@Schema(description = "빈 페이지 여부")
	private Boolean empty;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Pageable {
		private Integer pageNumber;
		private Integer pageSize;
		private Sort sort;
		private Long offset;
		private Boolean paged;
		private Boolean unpaged;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Sort {
		private Boolean empty;
		private Boolean sorted;
		private Boolean unsorted;
	}
}
