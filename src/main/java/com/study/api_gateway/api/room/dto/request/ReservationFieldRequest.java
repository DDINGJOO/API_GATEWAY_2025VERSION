package com.study.api_gateway.api.room.dto.request;

import com.study.api_gateway.api.room.dto.enums.FieldType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationFieldRequest {
	
	@NotBlank(message = "필드 타이틀은 필수입니다.")
	@Size(max = 100, message = "필드 타이틀은 최대 100자까지 가능합니다.")
	private String title;
	
	private FieldType inputType;
	
	private Boolean required;
	
	private Integer maxLength;
	
	@NotNull(message = "순서는 필수입니다.")
	@Min(value = 1, message = "순서는 1 이상이어야 합니다.")
	private Integer sequence;
}
