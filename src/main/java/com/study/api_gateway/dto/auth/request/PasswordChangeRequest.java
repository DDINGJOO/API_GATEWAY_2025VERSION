package com.study.api_gateway.dto.auth.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PasswordChangeRequest {
    private String email;
    private String newPassword;
    private String newPasswordConfirm;
}
