package com.study.api_gateway.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SocialLoginRequest", description = "소셜 로그인 요청")
public class SocialLoginRequest {
	@NotBlank(message = "accessToken은 필수입니다")
	@Schema(description = "소셜 서비스에서 발급받은 액세스 토큰", example = "5sn2Mdl27NLeXHdy2mqXbK6eXYO_...")
	private String accessToken;
}