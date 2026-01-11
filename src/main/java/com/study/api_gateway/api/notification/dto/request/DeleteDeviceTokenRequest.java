package com.study.api_gateway.api.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteDeviceTokenRequest {
	private Long userId;
	private String deviceToken;
}
