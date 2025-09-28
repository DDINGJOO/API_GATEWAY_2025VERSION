package com.study.api_gateway.dto.auth.request;

import com.study.api_gateway.dto.auth.enums.ConsentType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ConsentRequest {
    private String consent;
    private String version;
    private boolean consented;
}
