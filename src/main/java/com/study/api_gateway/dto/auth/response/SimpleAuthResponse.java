package com.study.api_gateway.dto.auth.response;


import com.study.api_gateway.dto.auth.enums.Provider;
import com.study.api_gateway.dto.auth.enums.Status;
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
