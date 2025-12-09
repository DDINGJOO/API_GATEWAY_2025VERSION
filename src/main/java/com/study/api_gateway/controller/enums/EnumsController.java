package com.study.api_gateway.controller.enums;


import com.study.api_gateway.client.*;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.support.faq.FaqCategory;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class EnumsController {
	private final ProfileClient profileClient;
	private final AuthClient authClient;
	private final ImageClient imageClient;
	private final ResponseFactory responseFactory;
	private final ArticleClient articleClient;
	private final FaqClient faqClient;
	private final PlaceClient placeClient;
	private final RoomClient roomClient;
	
	@Operation(summary = "장르 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "GenresExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"ROCK\": \"록\" },\n  \"request\": { \"path\": \"/bff/v1/enums/genres\" }\n}")))
	})
	@GetMapping("/genres")
	public Mono<ResponseEntity<BaseResponse>> genres(ServerHttpRequest request) {
		return profileClient.fetchGenres()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "악기 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "InstrumentsExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"GUITAR\": \"기타\" },\n  \"request\": { \"path\": \"/bff/v1/enums/instruments\" }\n}")))
	})
	@GetMapping("/instruments")
	public Mono<ResponseEntity<BaseResponse>> instruments(ServerHttpRequest request) {
		return profileClient.fetchInstruments()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "활동지역 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "LocationsExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"SEOUL\": \"서울\" },\n  \"request\": { \"path\": \"/bff/v1/enums/locations\" }\n}")))
	})
	@GetMapping("/locations")
	public Mono<ResponseEntity<BaseResponse>> locations(ServerHttpRequest request) {
		return profileClient.fetchLocations()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "동의항목 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ConsentsExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"MANDATORY\": { \"title\": \"필수 동의\" } },\n  \"request\": { \"path\": \"/bff/v1/enums/consents\" }\n}")))
	})
	@GetMapping("/consents")
	public Mono<ResponseEntity<BaseResponse>> consents(@RequestParam(name = "all") Boolean all, ServerHttpRequest request) {
		return authClient.fetchAllConsents(all)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "이미지 확장자 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ExtensionsExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\"png\", \"jpg\"],\n  \"request\": { \"path\": \"/bff/v1/enums/extensions\" }\n}")))
	})
	@GetMapping("/extensions")
	public Mono<ResponseEntity<BaseResponse>> extensions(ServerHttpRequest request) {
		return imageClient.getExtensions()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "이미지 레퍼런스 타입")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ReferenceTypesExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\"ARTICLE\", \"PROFILE\"],\n  \"request\": { \"path\": \"/bff/v1/enums/reference-types\" }\n}")))
	})
	@GetMapping("/reference-types")
	public Mono<ResponseEntity<BaseResponse>> referenceType(ServerHttpRequest request) {
		return imageClient.getReferenceType()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "게시글 보드 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "BoardsExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"1\": \"FREE\" },\n  \"request\": { \"path\": \"/bff/v1/enums/articles/boards\" }\n}")))
	})
	@GetMapping("/articles/boards")
	public Mono<ResponseEntity<BaseResponse>> boards(ServerHttpRequest request) {
		return articleClient.getBoards()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "게시글 키워드 목록")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "KeywordsExample", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"1\": \"MUSIC\" },\n  \"request\": { \"path\": \"/bff/v1/enums/articles/keywords\" }\n}")))
	})
	@GetMapping("/articles/keywords")
	public Mono<ResponseEntity<BaseResponse>> articleKeywords(ServerHttpRequest request) {
		return articleClient.getKeywords()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "FAQ 목록 조회", description = "카테고리별 FAQ 목록을 조회합니다. category가 없으면 전체 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/faqs")
	public Mono<ResponseEntity<BaseResponse>> getFaqs(
			@RequestParam(required = false) FaqCategory category,
			ServerHttpRequest req) {
		return faqClient.getFaqs(category)
				.collectList()
				.map(list -> responseFactory.ok(list, req));
	}
	
	@Operation(summary = "플레이스 키워드 목록 조회", description = "활성화된 플레이스 키워드 목록을 조회합니다. 타입을 지정하면 해당 타입의 키워드만 조회됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "PlaceKeywordsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"id\": 1,\n      \"name\": \"연습실\",\n      \"type\": \"SPACE_TYPE\",\n      \"description\": \"음악 연습 공간\",\n      \"displayOrder\": 1\n    },\n    {\n      \"id\": 2,\n      \"name\": \"드럼\",\n      \"type\": \"INSTRUMENT_EQUIPMENT\",\n      \"description\": \"드럼 악기\",\n      \"displayOrder\": 2\n    },\n    {\n      \"id\": 3,\n      \"name\": \"에어컨\",\n      \"type\": \"AMENITY\",\n      \"description\": \"냉방 시설\",\n      \"displayOrder\": 3\n    },\n    {\n      \"id\": 4,\n      \"name\": \"24시간\",\n      \"type\": \"OTHER_FEATURE\",\n      \"description\": \"24시간 이용 가능\",\n      \"displayOrder\": 4\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/enums/place-keywords\"\n  }\n}")))
	})
	@GetMapping("/place-keywords")
	public Mono<ResponseEntity<BaseResponse>> getPlaceKeywords(
			@RequestParam(required = false) String type,
			ServerHttpRequest req) {
		return placeClient.getKeywords(type)
				.map(result -> responseFactory.ok(result, req));
	}
	
	@Operation(summary = "룸 키워드 맵 조회", description = "룸에서 사용 가능한 모든 키워드를 Map 형태로 조회합니다. Key는 키워드 ID입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RoomKeywordsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"1\": {\n      \"keywordId\": 1,\n      \"keyword\": \"조용한\"\n    },\n    \"2\": {\n      \"keywordId\": 2,\n      \"keyword\": \"밝은\"\n    },\n    \"3\": {\n      \"keywordId\": 3,\n      \"keyword\": \"넓은\"\n    },\n    \"4\": {\n      \"keywordId\": 4,\n      \"keyword\": \"깔끔한\"\n    }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/enums/room-keywords\"\n  }\n}")))
	})
	@GetMapping("/room-keywords")
	public Mono<ResponseEntity<BaseResponse>> getRoomKeywords(ServerHttpRequest req) {
		return roomClient.getRoomKeywordMap()
				.map(result -> responseFactory.ok(result, req));
	}
}
