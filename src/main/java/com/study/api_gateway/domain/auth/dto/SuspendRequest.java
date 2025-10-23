package com.study.api_gateway.domain.auth.dto;

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
