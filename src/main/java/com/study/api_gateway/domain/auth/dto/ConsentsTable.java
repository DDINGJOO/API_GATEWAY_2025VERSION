package com.study.api_gateway.domain.auth.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class ConsentsTable {
	
	private String id;
	
	private String consentName;
	
	private String version;
	
	private String consentUrl;
	
	private boolean required;
	
}
