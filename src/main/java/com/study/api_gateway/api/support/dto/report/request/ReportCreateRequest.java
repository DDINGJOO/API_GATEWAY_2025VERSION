package com.study.api_gateway.api.support.dto.report.request;

import com.study.api_gateway.api.support.dto.report.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {
	private String reporterId;
	private String reportedId;
	private ReferenceType referenceType;
	private String reportCategory;
	private String reason;
}
