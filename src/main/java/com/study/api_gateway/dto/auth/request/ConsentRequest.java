package com.study.api_gateway.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ConsentRequest {
    @NotBlank
    private String consentName;
    @NotBlank
    private String version;
    private boolean consented;
}
