package com.study.api_gateway.dto.profile.response;

import com.study.api_gateway.dto.profile.enums.City;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String userId;
    private Character sex;
    private String profileImageUrl;
    private List<String> genres;
    private List<String> instruments;
    private City city;
    private String nickname;
    private Boolean isChattable;
    private Boolean isPublic;


}
