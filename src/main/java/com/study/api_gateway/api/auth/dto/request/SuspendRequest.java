package com.study.api_gateway.api.auth.dto.request;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspendRequest {
	private String suspendReason;
	private String suspenderUserId;
	private String suspendedUserId;
	private long suspendDay;
}
