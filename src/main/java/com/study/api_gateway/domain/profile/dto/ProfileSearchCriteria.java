package com.study.api_gateway.domain.profile.dto;


import com.study.api_gateway.domain.profile.enums.City;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;


/**
 * @param genres      장르 ID 목록
 * @param instruments 악기 ID 목록
 */
@AllArgsConstructor
@Builder
public record ProfileSearchCriteria(City city, String nickName, List<Integer> genres, List<Integer> instruments,
                                    Character sex) {
}
