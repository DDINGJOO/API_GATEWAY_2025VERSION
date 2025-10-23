package com.study.api_gateway.domain.profile.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ProfileUpdateRequest", description = "프로필 수정 요청 바디",
		example = "{\n  \"profileImageId\": \"img_100\",\n  \"nickname\": \"딩주\",\n  \"city\": \"SEOUL\",\n  \"chattable\": true,\n  \"publicProfile\": true,\n  \"sex\": \"M\",\n  \"genres\": { \"1\": \"ROCK\", \"2\": \"JAZZ\" },\n  \"instruments\": { \"1\": \"GUITAR\", \"3\": \"DRUM\" }\n}")
public class ProfileUpdateRequest {
	
	@Schema(description = "프로필 이미지 ID", example = "img_100")
	private String profileImageId;
	@Schema(description = "닉네임", example = "띵주")
	private String nickname;
	@Schema(description = "활동 도시(코드)", example = "SEOUL")
	private String city;
	
	@Schema(description = "채팅 가능 여부", example = "true")
	private boolean chattable;
	@Schema(description = "프로필 공개 여부", example = "true")
	private boolean publicProfile;
	@Schema(description = "성별", example = "M")
	private Character sex;
	
	@Schema(description = "장르 ID-이름 매핑", example = "{\n  \"1\": \"ROCK\", \"2\": \"JAZZ\"\n}")
	private List<Integer> genres;
	@Schema(description = "악기 ID-이름 매핑", example = "{\n  \"1\": \"GUITAR\", \"3\": \"DRUM\"\n}")
	private List<Integer> instruments;
}
