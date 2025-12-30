package com.study.api_gateway.dto.notification.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConsentResponse {
	private String userId;
	private Boolean serviceConsent;
	private Boolean marketingConsent;
	private Boolean nightAdConsent;
	private LocalDateTime updatedAt;
}
