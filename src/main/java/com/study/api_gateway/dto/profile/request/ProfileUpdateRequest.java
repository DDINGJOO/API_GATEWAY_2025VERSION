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

    private String profileImageId;
    private String nickname;
    private String city;

    private boolean chattable;
    private boolean publicProfile;
    private Character sex;

    private Map<Integer,String> genres;
    private Map<Integer,String> instruments;
}
