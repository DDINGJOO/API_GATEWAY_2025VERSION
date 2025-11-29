package com.study.api_gateway.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이미지 정보를 나타내는 공통 DTO
 * Room, Place, Article 등 여러 도메인에서 재사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "이미지 정보")
public class ImageInfo {

	@Schema(description = "이미지 ID", example = "IMG_001")
	private String imageId;

	@Schema(description = "이미지 URL", example = "https://cdn.example.com/images/sample.jpg")
	private String imageUrl;

	@Schema(description = "이미지 순서", example = "1")
	private Integer sequence;
}