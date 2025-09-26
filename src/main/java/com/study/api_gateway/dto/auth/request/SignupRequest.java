package com.study.api_gateway.dto.auth.request;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest
{

    private String email;
    private String password;
    private String passwordConfirm;

    private List<ConsentRequest> consentReqs;
}
