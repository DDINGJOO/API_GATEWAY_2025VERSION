package com.study.api_gateway.domain.image;


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
		return imageClient.confirmImage(referenceId, imageIds);
	}
	
	
}
