package com.study.api_gateway.config;

import com.study.api_gateway.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<BaseResponse> handleWebClientException(WebClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return BaseResponse.error(body != null && !body.isBlank() ? body : ex.getMessage(), status, Map.of("source", "downstream"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return BaseResponse.error(message, status, Map.of("cause", "internal"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGeneric(Exception ex) {
        return BaseResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, Map.of());
    }
}
