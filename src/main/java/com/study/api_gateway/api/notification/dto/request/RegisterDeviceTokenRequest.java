package com.study.api_gateway.api.notification.dto.request;

import com.study.api_gateway.api.notification.dto.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDeviceTokenRequest {
	private Long userId;
	private String deviceToken;
	private Platform platform;
}
