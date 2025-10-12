package com.study.api_gateway.dto.profile.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UserResponse", description = "프로필 사용자 응답",
		example = "{\n  \"userId\": \"u1\",\n  \"sex\": \"M\",\n  \"profileImageUrl\": \"https://cdn.example.com/profiles/u1.png\",\n  \"genres\": [\"ROCK\", \"JAZZ\"],\n  \"instruments\": [\"GUITAR\", \"BASS\"],\n  \"city\": \"SEOUL\",\n  \"nickname\": \"딩주\",\n  \"isChattable\": true,\n  \"isPublic\": true\n}")
public class UserResponse {
	@Schema(description = "사용자 ID", example = "u1")
    private String userId;
	@Schema(description = "성별(M/F)", example = "M")
    private Character sex;
	@Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/u1.png")
    private String profileImageUrl;
	@Schema(description = "선호 장르 목록", example = "[\"ROCK\", \"JAZZ\"]")
    private List<String> genres;
	@Schema(description = "연주 가능한 악기 목록", example = "[\"GUITAR\", \"BASS\"]")
    private List<String> instruments;
	@Schema(description = "활동 지역", example = "SEOUL")
	private String city;
	@Schema(description = "닉네임", example = "딩주")
    private String nickname;
	@Schema(description = "채팅 가능 여부", example = "true")
    private Boolean isChattable;
	@Schema(description = "프로필 공개 여부", example = "true")
    private Boolean isPublic;


}
