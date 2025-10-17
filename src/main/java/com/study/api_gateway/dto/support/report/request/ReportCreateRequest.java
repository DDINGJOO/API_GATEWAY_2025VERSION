package com.study.api_gateway.dto.support.report.request;

import com.study.api_gateway.dto.support.report.ReferenceType;
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
