package com.study.api_gateway.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;
    @NotBlank
    private String deviceId;
}
