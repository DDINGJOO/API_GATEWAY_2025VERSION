package com.study.api_gateway.dto.support.report.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportWithdrawRequest {
	private String reporterId;
	private String reason;
}
