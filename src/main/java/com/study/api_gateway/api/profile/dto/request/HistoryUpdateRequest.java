package com.study.api_gateway.api.profile.dto.request;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryUpdateRequest {
	private String columnName;
	private String oldValue;
	private String newValue;
}
