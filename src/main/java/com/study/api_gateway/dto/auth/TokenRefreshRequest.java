package com.study.api_gateway.dto.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenRefreshRequest {
    private String refreshToken;
    private String deviceId;
}
