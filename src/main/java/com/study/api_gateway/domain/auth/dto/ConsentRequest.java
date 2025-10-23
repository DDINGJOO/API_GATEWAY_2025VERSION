package com.study.api_gateway.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ConsentRequest {
	@NotBlank
	private String consentId;
	
	private boolean consented;
}
