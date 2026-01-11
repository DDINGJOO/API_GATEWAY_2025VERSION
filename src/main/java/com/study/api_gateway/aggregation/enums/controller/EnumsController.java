package com.study.api_gateway.aggregation.enums.controller;


import com.study.api_gateway.api.profile.client.ProfileClient;
import com.study.api_gateway.api.auth.client.AuthClient;
import com.study.api_gateway.api.image.client.ImageClient;
import com.study.api_gateway.api.article.client.ArticleClient;
import com.study.api_gateway.api.support.client.FaqClient;
import com.study.api_gateway.api.place.client.PlaceClient;
import com.study.api_gateway.api.room.client.RoomClient;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.support.dto.faq.FaqCategory;
import com.study.api_gateway.common.response.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/enums")
@RequiredArgsConstructor
public class EnumsController implements EnumsApi {
	private final ProfileClient profileClient;
	private final AuthClient authClient;
	private final ImageClient imageClient;
	private final ResponseFactory responseFactory;
	private final ArticleClient articleClient;
	private final FaqClient faqClient;
	private final PlaceClient placeClient;
	private final RoomClient roomClient;
	
	@Override
	@GetMapping("/genres")
	public Mono<ResponseEntity<BaseResponse>> genres(ServerHttpRequest request) {
		return profileClient.fetchGenres()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/instruments")
	public Mono<ResponseEntity<BaseResponse>> instruments(ServerHttpRequest request) {
		return profileClient.fetchInstruments()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/locations")
	public Mono<ResponseEntity<BaseResponse>> locations(ServerHttpRequest request) {
		return profileClient.fetchLocations()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/consents")
	public Mono<ResponseEntity<BaseResponse>> consents(@RequestParam(name = "all") Boolean all, ServerHttpRequest request) {
		return authClient.fetchAllConsents(all)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/extensions")
	public Mono<ResponseEntity<BaseResponse>> extensions(ServerHttpRequest request) {
		return imageClient.getExtensions()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/reference-types")
	public Mono<ResponseEntity<BaseResponse>> referenceType(ServerHttpRequest request) {
		return imageClient.getReferenceType()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/articles/boards")
	public Mono<ResponseEntity<BaseResponse>> boards(ServerHttpRequest request) {
		return articleClient.getBoards()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/articles/keywords")
	public Mono<ResponseEntity<BaseResponse>> articleKeywords(ServerHttpRequest request) {
		return articleClient.getKeywords()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@GetMapping("/faqs")
	public Mono<ResponseEntity<BaseResponse>> getFaqs(
			@RequestParam(required = false) FaqCategory category,
			ServerHttpRequest req) {
		return faqClient.getFaqs(category)
				.collectList()
				.map(list -> responseFactory.ok(list, req));
	}
	
	@Override
	@GetMapping("/place-keywords")
	public Mono<ResponseEntity<BaseResponse>> getPlaceKeywords(
			@RequestParam(required = false) String type,
			ServerHttpRequest req) {
		return placeClient.getKeywords(type)
				.map(result -> responseFactory.ok(result, req));
	}
	
	@Override
	@GetMapping("/room-keywords")
	public Mono<ResponseEntity<BaseResponse>> getRoomKeywords(ServerHttpRequest req) {
		return roomClient.getRoomKeywordMap()
				.map(result -> responseFactory.ok(result, req));
	}
}
