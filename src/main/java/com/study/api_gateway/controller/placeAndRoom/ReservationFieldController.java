package com.study.api_gateway.controller.placeAndRoom;

import com.study.api_gateway.client.RoomClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.room.request.ReservationFieldRequest;
import com.study.api_gateway.dto.room.response.ReservationFieldResponse;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bff/v1/rooms/{roomId}/reservation-fields")
@RequiredArgsConstructor
@Tag(name = "ReservationField", description = "예약 시 추가 정보 필드 관리 API")
public class ReservationFieldController {

    private final RoomClient roomClient;
    private final ResponseFactory responseFactory;

    @GetMapping
    @Operation(summary = "예약 필드 목록 조회", description = "특정 룸의 예약 시 추가 정보 필드 목록을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ReservationFieldsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"fieldId\": 1,\n      \"title\": \"이용 목적\",\n      \"inputType\": \"SELECT\",\n      \"required\": true,\n      \"maxLength\": null,\n      \"sequence\": 1\n    },\n    {\n      \"fieldId\": 2,\n      \"title\": \"이용 인원\",\n      \"inputType\": \"NUMBER\",\n      \"required\": true,\n      \"maxLength\": null,\n      \"sequence\": 2\n    },\n    {\n      \"fieldId\": 3,\n      \"title\": \"요청 사항\",\n      \"inputType\": \"TEXT\",\n      \"required\": false,\n      \"maxLength\": 500,\n      \"sequence\": 3\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/rooms/101/reservation-fields\"\n  }\n}")))
    })
    public Mono<ResponseEntity<BaseResponse>> getReservationFields(
            @Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
            ServerHttpRequest req
    ) {
        log.info("예약 필드 목록 조회: roomId={}", roomId);

        return roomClient.getReservationFields(roomId)
                .map(response -> responseFactory.ok(response, req));
    }

    @PutMapping
    @Operation(summary = "예약 필드 전체 교체", description = "특정 룸의 예약 시 추가 정보 필드를 전체 교체합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "교체 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ReservationFieldsReplaceSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"fieldId\": 4,\n      \"title\": \"이용 목적\",\n      \"inputType\": \"SELECT\",\n      \"required\": true,\n      \"maxLength\": null,\n      \"sequence\": 1\n    },\n    {\n      \"fieldId\": 5,\n      \"title\": \"이용 인원\",\n      \"inputType\": \"NUMBER\",\n      \"required\": true,\n      \"maxLength\": null,\n      \"sequence\": 2\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/rooms/101/reservation-fields\"\n  }\n}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public Mono<ResponseEntity<BaseResponse>> replaceReservationFields(
            @Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
            @Valid @RequestBody List<ReservationFieldRequest> requests,
            ServerHttpRequest req
    ) {
        log.info("예약 필드 전체 교체: roomId={}, fieldCount={}", roomId, requests.size());

        return roomClient.replaceReservationFields(roomId, requests)
                .map(response -> responseFactory.ok(response, req));
    }
}