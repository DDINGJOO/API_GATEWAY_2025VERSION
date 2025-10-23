package com.study.api_gateway.domain.image;


import com.study.api_gateway.domain.image.dto.request.ImageBatchConfirmRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ImageConfirmService {
	
	private final ImageClient imageClient;
	
	public ImageConfirmService(ImageClient imageClient) {
		this.imageClient = imageClient;
	}
	
	public Mono<Void> confirmImage(String referenceId, List<String> imageIds) {
		if (imageIds.size() == 1) {
			return imageClient.confirmImage(referenceId, imageIds.get(0));
		} else {
			return imageClient.confirmImages(ImageBatchConfirmRequest.builder()
					.referenceId(referenceId)
					.imageIds(imageIds)
					.build());
		}
	}
	
	
}
