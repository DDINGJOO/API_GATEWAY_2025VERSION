package com.study.api_gateway.config;

import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.util.ResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ResponseFactory responseFactory;

    public GlobalExceptionHandler(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<BaseResponse> handleWebClientException(WebClientResponseException ex, ServerHttpRequest request) {
        String body = ex.getResponseBodyAsString();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = body != null && !body.isBlank() ? body : ex.getMessage();
        return responseFactory.error(message, status, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse> handleResponseStatus(ResponseStatusException ex, ServerHttpRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return responseFactory.error(message, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGeneric(Exception ex, ServerHttpRequest request) {
        return responseFactory.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
