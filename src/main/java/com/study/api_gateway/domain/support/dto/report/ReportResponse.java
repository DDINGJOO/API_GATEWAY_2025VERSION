package com.study.api_gateway.domain.support.dto.report;

import com.study.api_gateway.domain.support.enums.ReferenceType;
import com.study.api_gateway.domain.support.enums.ReportStatus;
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
