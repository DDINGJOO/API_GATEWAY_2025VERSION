package com.study.api_gateway.controller;

import com.study.api_gateway.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class HealthCheckController {

    @Operation(summary = "헬스 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "HealthOk", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"ok\",\n  \"request\": { \"path\": \"/health\" }\n}")))
    })
    @GetMapping("/health")
    public Mono<ResponseEntity<BaseResponse>> health() {
        return Mono.just(BaseResponse.success("ok", Map.of("path", "/health")));
    }
}
