package com.study.api_gateway.dto.image.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ImageConfirmRequest", description = "이미지 확정 요청 바디")
public class ImageConfirmRequest {
	
	@Schema(description = "확정할 이미지 ID 목록", example = "[\"img_100\", \"img_101\"]", required = true)
	private List<String> imageIds;
	
	@Schema(description = "참조 ID (사용자 ID 등)", example = "user_123", required = true)
	private String referenceId;
}
