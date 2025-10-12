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
 * The service returns a Spring Page-like JSON with a top-level "content" array.
 * We ignore any unknown fields to be resilient to backend changes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "UserPageResponse", description = "프로필 사용자 페이지 응답(상위 content 래퍼)")
public class UserPageResponse {
	@Schema(description = "페이지 콘텐츠 목록")
	private List<UserResponse> content;
}
