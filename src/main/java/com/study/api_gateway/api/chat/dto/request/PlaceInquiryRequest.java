package com.study.api_gateway.api.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceInquiryRequest {
	private Long placeId;
	private String placeName;
	private Long hostId;
	private String initialMessage;
}
