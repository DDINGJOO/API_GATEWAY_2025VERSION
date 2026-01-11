package com.study.api_gateway.common.util;

import lombok.Getter;

@Getter
public enum TokenValidationResult {
	VALID("유효한 토큰입니다"),
	EXPIRED("토큰이 만료되었습니다"),
	INVALID_SIGNATURE("토큰 서명이 유효하지 않습니다"),
	MALFORMED("토큰 형식이 올바르지 않습니다"),
	MISSING_CLAIMS("필수 클레임이 누락되었습니다"),
	INVALID_FORMAT("JWT 형식이 아닙니다");
	
	private final String message;
	
	TokenValidationResult(String message) {
		this.message = message;
	}
	
	public boolean isValid() {
		return this == VALID;
	}
}
