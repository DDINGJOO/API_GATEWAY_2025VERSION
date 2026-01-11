package com.study.api_gateway.common.exception;

import lombok.Getter;

/**
 * Gateway 커스텀 예외
 */
@Getter
public class GatewayException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String detail;

	public GatewayException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.detail = null;
	}

	public GatewayException(ErrorCode errorCode, String detail) {
		super(detail != null ? detail : errorCode.getMessage());
		this.errorCode = errorCode;
		this.detail = detail;
	}

	public GatewayException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
		this.detail = cause.getMessage();
	}

	public GatewayException(ErrorCode errorCode, String detail, Throwable cause) {
		super(detail != null ? detail : errorCode.getMessage(), cause);
		this.errorCode = errorCode;
		this.detail = detail;
	}
}
