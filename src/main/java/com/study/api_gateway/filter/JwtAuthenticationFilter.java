package com.study.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api_gateway.config.AuthorizationConfig;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.enums.Role;
import com.study.api_gateway.util.JwtTokenValidator;
import com.study.api_gateway.util.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
	
	// 인증이 필요없는 경로 목록 (로그인, 회원가입 등)
	private static final List<String> PUBLIC_PATHS = List.of(
			"/bff/v1/auth",
			"/actuator/health",
			"/swagger-ui.html",
			"/swagger-ui",
			"/v3/api-docs",
			"/webjars",
			"/swagger-resources"
	);
	// GET 요청만 Public으로 허용하는 경로 (조회는 누구나 가능)
	private static final List<String> PUBLIC_READ_PATHS = List.of(
			"/bff/v1/profiles",
			"/bff/v1/enums"
	);
	private final JwtTokenValidator jwtTokenValidator;
	private final ObjectMapper objectMapper;
	private final AuthorizationConfig authorizationConfig;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getPath().value();
		String method = request.getMethod().name();
		
		// Public 경로는 인증 스킵
		if (isPublicPath(path)) {
			log.debug("Public path accessed: {}", path);
			return chain.filter(exchange);
		}
		
		// GET 요청에 대한 Public Read 경로는 인증 스킵
		if ("GET".equals(method) && isPublicReadPath(path)) {
			log.debug("Public read path accessed: {} {}", method, path);
			return chain.filter(exchange);
		}
		
		// Authorization 헤더에서 토큰 추출
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.warn("Missing or invalid Authorization header for path: {}", path);
			return handleUnauthorized(exchange, "Missing or invalid Authorization header");
		}
		
		String token = authHeader.substring(7); // "Bearer " 제거
		
		// 토큰 검증 (상세한 에러 타입 확인)
		TokenValidationResult validationResult = jwtTokenValidator.validate(token);
		if (!validationResult.isValid()) {
			log.warn("Token validation failed for path: {}. Reason: {}", path, validationResult.getMessage());
			return handleUnauthorized(exchange, validationResult.getMessage());
		}
		
		// 토큰에서 사용자 정보 추출
		String userId = jwtTokenValidator.extractUserId(token);
		String role = jwtTokenValidator.extractRole(token);
		String deviceId = jwtTokenValidator.extractDeviceId(token);
		
		// 사용자 정보 검증 (추가 안전장치)
		if (userId == null || userId.isEmpty()) {
			log.warn("Token validated but userId is missing for path: {}", path);
			return handleUnauthorized(exchange, "토큰에 사용자 정보가 없습니다");
		}
		
		log.debug("Authenticated request - UserId: {}, Role: {}, DeviceId: {}, Path: {}",
				userId, role, deviceId, path);
		
		
		// 요청 헤더에 사용자 정보 추가 (다운스트림 서비스에서 사용할 수 있도록)
		ServerHttpRequest mutatedRequest = request.mutate()
				.header("X-User-Id", userId)
				.header("X-User-Role", role)
				.header("X-Device-Id", deviceId)
				.build();
		
		ServerWebExchange mutatedExchange = exchange.mutate()
				.request(mutatedRequest)
				.build();
		
		return chain.filter(mutatedExchange);
	}
	
	private boolean isPublicPath(String path) {
		return PUBLIC_PATHS.stream()
				.anyMatch(path::startsWith);
	}
	
	private boolean isPublicReadPath(String path) {
		// /me 경로는 인증이 필요하므로 제외
		if (path.endsWith("/me")) {
			return false;
		}
		return PUBLIC_READ_PATHS.stream()
				.anyMatch(path::startsWith);
	}
	
	/**
	 * 역할 문자열을 Role enum으로 변환
	 */
	private Role parseRole(String roleString) {
		if (roleString == null || roleString.isEmpty()) {
			return null;
		}
		try {
			return Role.valueOf(roleString.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.warn("Invalid role string: {}", roleString);
			return null;
		}
	}
	
	
	private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
		return handleError(exchange, HttpStatus.UNAUTHORIZED, message);
	}
	
	
	private Mono<Void> handleError(ServerWebExchange exchange, HttpStatus status, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		BaseResponse errorResponse = BaseResponse.builder()
				.isSuccess(false)
				.code(status.value())
				.data(message)
				.request(null)
				.build();
		
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
			DataBuffer buffer = response.bufferFactory().wrap(bytes);
			return response.writeWith(Mono.just(buffer));
		} catch (Exception e) {
			log.error("Error writing error response", e);
			byte[] fallbackBytes = String.format(
					"{\"isSuccess\":false,\"code\":%d,\"data\":\"%s\",\"request\":null}",
					status.value(), message
			).getBytes(StandardCharsets.UTF_8);
			DataBuffer buffer = response.bufferFactory().wrap(fallbackBytes);
			return response.writeWith(Mono.just(buffer));
		}
	}
}
