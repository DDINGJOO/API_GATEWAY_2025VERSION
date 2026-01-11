package com.study.api_gateway.api.room.controller;

import com.study.api_gateway.api.room.controller.ReservationFieldApi;
import com.study.api_gateway.api.room.service.RoomFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.room.dto.request.ReservationFieldRequest;
import com.study.api_gateway.api.room.dto.response.ReservationFieldResponse;
import com.study.api_gateway.common.response.ResponseFactory;
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
public class ReservationFieldController implements ReservationFieldApi {

    private final RoomFacadeService roomFacadeService;
    private final ResponseFactory responseFactory;

    @Override
    @GetMapping
    public Mono<ResponseEntity<BaseResponse>> getReservationFields(
            @PathVariable Long roomId,
            ServerHttpRequest req
    ) {
        log.info("예약 필드 목록 조회: roomId={}", roomId);

        return roomFacadeService.getReservationFields(roomId)
                .map(response -> responseFactory.ok(response, req));
    }

    @Override
    @PutMapping
    public Mono<ResponseEntity<BaseResponse>> replaceReservationFields(
            @PathVariable Long roomId,
            @Valid @RequestBody List<ReservationFieldRequest> requests,
            ServerHttpRequest req
    ) {
        log.info("예약 필드 전체 교체: roomId={}, fieldCount={}", roomId, requests.size());

        return roomFacadeService.replaceReservationFields(roomId, requests)
                .map(response -> responseFactory.ok(response, req));
    }
}
