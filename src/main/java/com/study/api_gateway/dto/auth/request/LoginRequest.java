package com.study.api_gateway.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(name = "LoginRequest", description = "로그인 요청 바디",
		example = "{\n  \"email\": \"user@example.com\",\n  \"password\": \"P@ssw0rd!\"\n}")
public class LoginRequest {
	@Email
	@NotBlank
	@Schema(description = "이메일", example = "user@example.com")
	private String email;
	
	@NotBlank
	@Size(min = 8)
	@Schema(description = "비밀번호(8자 이상)", example = "P@ssw0rd!")
	private String password;
}
