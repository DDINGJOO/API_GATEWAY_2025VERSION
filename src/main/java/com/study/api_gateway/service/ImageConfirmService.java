package com.study.api_gateway.service;


import com.study.api_gateway.client.ImageClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ImageConfirmService {

    private ImageClient imageClient;

    public ImageConfirmService(ImageClient imageClient) {
        this.imageClient = imageClient;
    }

    public Mono<Void> confirmImage(String imageId)
    {
        return imageClient.confirmImage(imageId);
    }


}
