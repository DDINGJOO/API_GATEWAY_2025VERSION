package com.study.api_gateway.dto.support.faq.response;

import com.study.api_gateway.dto.support.faq.FaqCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqResponse {
	private Long id;
	private FaqCategory category;
	private String title;
	private String question;
	private String answer;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
