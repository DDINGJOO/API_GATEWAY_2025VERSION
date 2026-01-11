package com.study.api_gateway.api.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
	
	@Email
	@NotBlank
	private String email;
	
	@NotBlank
	@Size(min = 8)
	private String password;
	
	@NotBlank
	@Size(min = 8)
	private String passwordConfirm;
	
	@NotEmpty
	@Valid
	private List<ConsentRequest> consentReqs;
}
