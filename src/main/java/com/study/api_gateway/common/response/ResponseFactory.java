package com.study.api_gateway.common.response;

import com.study.api_gateway.common.dto.BaseResponse;
import com.study.api_gateway.common.util.RequestPathHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResponseFactory {
	
	private final RequestPathHelper pathHelper;
	
	public ResponseFactory(RequestPathHelper pathHelper) {
		this.pathHelper = pathHelper;
	}
	
	public ResponseEntity<BaseResponse> ok(Object data, ServerHttpRequest request) {
		String path = pathHelper.extractClientPath(request);
		String url = pathHelper.extractClientUrl(request);
		return BaseResponse.success(data, Map.of("path", path, "url", url));
	}
	
	public ResponseEntity<BaseResponse> ok(Object data, ServerHttpRequest request, HttpStatus status) {
		String path = pathHelper.extractClientPath(request);
		String url = pathHelper.extractClientUrl(request);
		return BaseResponse.success(data, Map.of("path", path, "url", url), status);
	}
	
	public ResponseEntity<BaseResponse> error(String message, HttpStatus status, ServerHttpRequest request) {
		String path = pathHelper.extractClientPath(request);
		String url = pathHelper.extractClientUrl(request);
		return BaseResponse.error(message, status, Map.of("path", path, "url", url));
	}
}
