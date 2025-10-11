package com.study.api_gateway.dto.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
@Schema(name = "LoginResponse", description = "로그인 성공 시 토큰 응답",
		example = "{\n  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"deviceId\": \"device-123\"\n}")
public class LoginResponse {
	@Schema(description = "액세스 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
	@Schema(description = "리프레시 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
	@Schema(description = "디바이스 식별자", example = "device-123")
    private String deviceId;
}
