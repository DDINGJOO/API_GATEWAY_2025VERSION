package com.study.api_gateway.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(name = "SmsVerifyRequest", description = "SMS 인증 확인 요청 바디",
		example = "{\n  \"phoneNumber\": \"01012345678\",\n  \"code\": \"123456\"\n}")
public class SmsVerifyRequest {
	@NotBlank
	@Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
	@Schema(description = "휴대폰 번호 (01012345678 형식)", example = "01012345678")
	private String phoneNumber;

	@NotBlank
	@Size(min = 6, max = 6, message = "인증 코드는 6자리입니다")
	@Schema(description = "인증 코드 (6자리)", example = "123456")
	private String code;
}
