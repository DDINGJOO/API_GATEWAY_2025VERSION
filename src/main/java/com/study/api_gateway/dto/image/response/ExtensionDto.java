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
@Schema(name = "ExtensionDto", description = "이미지 파일 확장자 정보")
public class ExtensionDto {
	@Schema(description = "확장자 코드 (대문자)", example = "JPG")
	private String code;
	
	@Schema(description = "확장자 이름", example = "JPEG Image")
	private String name;
}
