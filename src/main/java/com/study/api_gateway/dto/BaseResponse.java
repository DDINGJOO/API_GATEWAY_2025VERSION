package com.study.api_gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * 공통 응답 포맷
 *
 * 상위 필드:
 * - isSuccess : 요청 성공 여부
 * - code      : 비즈니스/상태 코드 (HTTP 상태 코드 또는 커스텀 코드)
 * - data      : 실제 처리된 응답(정상 응답 데이터 또는 에러 메시지 String)
 * - request   : 이 요청을 처리하기 위해 어떤 요청을 했는지에 대한 정보(추가 메타데이터)
 *
 * 사용법 예:
 *  ResponseEntity<BaseResponse> res = BaseResponse.success(dataObject, Map.of("calledService", "user-service", "path", "/users/1"));
 *  ResponseEntity<BaseResponse> res = BaseResponse.error("유효하지 않은 요청", HttpStatus.BAD_REQUEST, Map.of("attempt", 1));
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "BaseResponse", description = "BFF 공통 응답 래퍼",
        example = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"example\": \"실제 엔드포인트별 응답 데이터가 위치합니다.\"\n  },\n  \"request\": {\n    \"method\": \"GET\",\n    \"path\": \"/bff/v1/...\"\n  }\n}")
public class BaseResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    private boolean isSuccess;

    @Schema(description = "비즈니스/상태 코드 (대개 HTTP 상태 코드)", example = "200")
    private int code;

    @Schema(description = "실제 응답 데이터. 엔드포인트에 따라 객체/배열/불리언/문자열이 될 수 있습니다.",
            anyOf = {Object.class, Map.class, List.class, String.class, Boolean.class})
    private Object data;

    @Schema(description = "요청 관련 메타데이터", implementation = Map.class,
            example = "{\n  \"method\": \"GET\",\n  \"path\": \"/bff/v1/...\"\n}")
    private Map<String, Object> request;

    // 성공 응답 생성 (기본 HTTP 200)
    public static ResponseEntity<BaseResponse> success(Object data, Map<String, Object> requestInfo) {
        BaseResponse body = BaseResponse.builder()
                .isSuccess(true)
                .code(HttpStatus.OK.value())
                .data(data)
                .request(requestInfo)
                .build();
        return ResponseEntity.ok(body);
    }

    // 성공 응답 생성 (커스텀 HTTP 상태)
    public static ResponseEntity<BaseResponse> success(Object data, Map<String, Object> requestInfo, HttpStatus status) {
        BaseResponse body = BaseResponse.builder()
                .isSuccess(true)
                .code(status.value())
                .data(data)
                .request(requestInfo)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    // 에러 응답 생성: data 필드에 String 타입의 에러 메시지를 넣음
    public static ResponseEntity<BaseResponse> error(String errorMessage, HttpStatus status, Map<String, Object> requestInfo) {
        BaseResponse body = BaseResponse.builder()
                .isSuccess(false)
                .code(status.value())
                .data(errorMessage) // 에러는 문자열로 반환
                .request(requestInfo)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    // 에러 응답 생성 (기본 HTTP 500)
    public static ResponseEntity<BaseResponse> error(String errorMessage, Map<String, Object> requestInfo) {
        return error(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR, requestInfo);
    }
}
