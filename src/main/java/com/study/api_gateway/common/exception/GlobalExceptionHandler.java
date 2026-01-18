package com.study.api_gateway.common.exception;

import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
	
	private final ResponseFactory responseFactory;
	
	public GlobalExceptionHandler(ResponseFactory responseFactory) {
		this.responseFactory = responseFactory;
	}
	
	/**
	 * GatewayException 처리 - ErrorCode 기반 에러 응답
	 */
	@ExceptionHandler(GatewayException.class)
	public ResponseEntity<BaseResponse> handleGatewayException(GatewayException ex, ServerHttpRequest request) {
		ErrorCode errorCode = ex.getErrorCode();
		String detail = ex.getDetail();
		
		log.error("GatewayException: [{}] {} - {}",
				errorCode.getCode(), errorCode.getMessage(), detail, ex);
		
		return responseFactory.error(errorCode, detail, request);
	}
	
	/**
	 * JWT 인증 예외 처리
	 */
	@ExceptionHandler(JwtAuthenticationException.class)
	public ResponseEntity<BaseResponse> handleJwtAuthException(JwtAuthenticationException ex, ServerHttpRequest request) {
		log.warn("JWT Authentication failed: {}", ex.getMessage());
		return responseFactory.error(ErrorCode.INVALID_TOKEN, ex.getMessage(), request);
	}
	
	@ExceptionHandler(WebClientResponseException.class)
	public ResponseEntity<BaseResponse> handleWebClientException(WebClientResponseException ex, ServerHttpRequest request) {
		String body = ex.getResponseBodyAsString();
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		String message = body != null && !body.isBlank() ? body : ex.getMessage();
		
		log.error("WebClient error: status={}, message={}", status, message);
		
		return responseFactory.error(message, status, request);
	}
	
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<BaseResponse> handleResponseStatus(ResponseStatusException ex, ServerHttpRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
		
		log.warn("ResponseStatusException: status={}, reason={}", status, message);
		
		return responseFactory.error(message, status, request);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseResponse> handleGeneric(Exception ex, ServerHttpRequest request) {
		log.error("Unhandled exception: ", ex);
		return responseFactory.error(ErrorCode.INTERNAL_ERROR, ex.getMessage(), request);
	}
}
