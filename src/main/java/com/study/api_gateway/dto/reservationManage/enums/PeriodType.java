package com.study.api_gateway.dto.reservationManage.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기간 타입")
public enum PeriodType {
	@Schema(description = "일간")
	DAILY,
	
	@Schema(description = "주간")
	WEEKLY,
	
	@Schema(description = "월간")
	MONTHLY
}
