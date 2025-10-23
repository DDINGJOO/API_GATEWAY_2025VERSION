package com.study.api_gateway.domain.support.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportPageResponse {
	private List<ReportResponse> content;
	private String nextCursor;
	private Integer size;
	private Boolean hasNext;
}