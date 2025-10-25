package com.study.api_gateway.dto.image.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "ReferenceTypeDto", description = "이미지 참조 타입 정보")
public class ReferenceTypeDto {
	@Schema(description = "참조 타입 코드", example = "PRODUCT")
	private String code;
	
	@Schema(description = "참조 타입 이름", example = "상품")
	private String name;
	
	@Schema(description = "다중 이미지 허용 여부", example = "true")
	private Boolean allowsMultiple;
	
	@Schema(description = "최대 이미지 개수 (null이면 무제한)", example = "10")
	private Integer maxImages;
	
	@Schema(description = "참조 타입 설명", example = "상품 이미지 (최대 10개)")
	private String description;
}
