package com.study.api_gateway.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PasswordChangeRequest {
	@Email
	@NotBlank
	private String email;
	
	@NotBlank
	@Size(min = 8)
	private String newPassword;
	
	@NotBlank
	@Size(min = 8)
	private String newPasswordConfirm;
}
