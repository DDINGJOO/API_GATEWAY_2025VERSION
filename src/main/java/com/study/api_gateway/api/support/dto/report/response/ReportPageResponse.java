package com.study.api_gateway.api.support.dto.report.response;

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