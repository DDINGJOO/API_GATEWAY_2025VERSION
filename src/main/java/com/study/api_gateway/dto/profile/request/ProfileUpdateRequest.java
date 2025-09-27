package com.study.api_gateway.dto.profile.request;


import com.study.api_gateway.dto.profile.enums.City;
import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private String nickname;
    private City city;
    private String imageId;
    private boolean chattable;
    private boolean publicProfile;

    private Map<Integer,String> genres;
    private Map<Integer,String> instruments;
}
