package com.study.api_gateway.dto.auth.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String deviceId;
}
