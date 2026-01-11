package com.study.api_gateway.api.auth.dto.response;


import com.study.api_gateway.api.auth.dto.enums.Provider;
import com.study.api_gateway.api.auth.dto.enums.Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleAuthResponse {
	private String userId;
	private Status status;
	private Provider provider;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	
}
