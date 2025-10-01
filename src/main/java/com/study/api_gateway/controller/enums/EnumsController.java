package com.study.api_gateway.controller.enums;


import com.study.api_gateway.client.ArticleClient;
import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.client.ImageClient;
import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.response.ConsentsTable;
import com.study.api_gateway.util.ResponseFactory;
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

    @GetMapping("/genres")
    public Mono<ResponseEntity<BaseResponse>> genres(ServerHttpRequest request){
        return profileClient.fetchGenres()
                .map(result -> responseFactory.ok(result, request));
    }

    @GetMapping("/instruments")
    public Mono<ResponseEntity<BaseResponse>> instruments(ServerHttpRequest request){
        return profileClient.fetchInstruments()
                .map(result -> responseFactory.ok(result, request));
    }

    @GetMapping("/locations")
    public Mono<ResponseEntity<BaseResponse>> locations(ServerHttpRequest request){
        return profileClient.fetchLocations()
                .map(result -> responseFactory.ok(result, request));
    }

    @GetMapping("/consents")
    public Mono<ResponseEntity<BaseResponse>> consents(@RequestParam(name = "all") Boolean all, ServerHttpRequest request){
        return authClient.fetchAllConsents(all)
                .map(result -> responseFactory.ok(result, request));
    }

    @GetMapping("/extensions")
    public Mono<ResponseEntity<BaseResponse>> extensions(ServerHttpRequest request){
        return imageClient.getExtensions()
                .map(result -> responseFactory.ok(result, request));
    }

    @GetMapping("/reference-types")
    public Mono<ResponseEntity<BaseResponse>> referenceType(ServerHttpRequest request){
        return imageClient.getReferenceType()
                .map(result -> responseFactory.ok(result, request));
    }
	
	@GetMapping("/articles/boards")
	public Mono<ResponseEntity<BaseResponse>> boards(ServerHttpRequest request){
		return articleClient.getBoards()
				.map(result -> responseFactory.ok(result, request));
	}
	
	@GetMapping("/articles/keywords")
	public Mono<ResponseEntity<BaseResponse>> articleKeywords(ServerHttpRequest request){
		return articleClient.getKeywords()
				.map(result -> responseFactory.ok(result, request));
	}
}
