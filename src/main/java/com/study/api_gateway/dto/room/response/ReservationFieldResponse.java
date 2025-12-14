package com.study.api_gateway.dto.room.response;

import com.study.api_gateway.dto.room.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationFieldResponse {

    private Long fieldId;
    private String title;
    private FieldType inputType;
    private Boolean required;
    private Integer maxLength;
    private Integer sequence;
}