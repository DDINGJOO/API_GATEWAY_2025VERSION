package com.study.api_gateway.api.support.dto.report.response;

import com.study.api_gateway.api.support.dto.report.ReferenceType;
import com.study.api_gateway.api.support.dto.report.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
	private String reportId;
	private String reporterId;
	private String reportedId;
	private ReferenceType referenceType;
	private String reportCategory;
	private String reason;
	private LocalDateTime reportedAt;
	private ReportStatus status;
}
