package com.study.api_gateway.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(name = "SmsCodeRequest", description = "SMS 인증 코드 요청/재발신 바디",
		example = "{\n  \"phoneNumber\": \"01012345678\"\n}")
public class SmsCodeRequest {
	@NotBlank
	@Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
	@Schema(description = "휴대폰 번호 (01012345678 형식)", example = "01012345678")
	private String phoneNumber;
}
