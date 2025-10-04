package com.study.api_gateway.controller.auth;

import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.request.LoginRequest;
import com.study.api_gateway.dto.auth.request.PasswordChangeRequest;
import com.study.api_gateway.dto.auth.request.SignupRequest;
import com.study.api_gateway.dto.auth.request.TokenRefreshRequest;
import com.study.api_gateway.dto.auth.response.LoginResponse;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/bff/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthClient authClient;
    private final ResponseFactory responseFactory;

    @Operation(summary = "비밀번호 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "PasswordChangeSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/password\"\n  }\n}")))
    })
    @PutMapping("/password")
    public Mono<ResponseEntity<BaseResponse>> changePassword(@RequestBody @Valid PasswordChangeRequest req, ServerHttpRequest request) {
        return authClient.changePassword(req)
                .map(result -> responseFactory.ok(result, request));
    }

    @Operation(summary = "로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "LoginSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/login\"\n  }\n}")))
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<BaseResponse>> login(@RequestBody @Valid LoginRequest req, ServerHttpRequest request) {
        return authClient.login(req.getEmail(), req.getPassword())
                .map(loginResp -> responseFactory.ok(loginResp, request));
    }

    @Operation(summary = "토큰 리프레시")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "RefreshTokenSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT_NEW>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/refreshToken\"\n  }\n}")))
    })
    @PostMapping("/refreshToken")
    public Mono<ResponseEntity<BaseResponse>> refreshToken(@RequestBody @Valid TokenRefreshRequest req, ServerHttpRequest request) {
        return authClient.refreshToken(req)
                .map(resp -> responseFactory.ok(resp, request));
    }

    @Operation(summary = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "SignupSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/signup\"\n  }\n}")))
    })
    @PostMapping("/signup")
    public Mono<ResponseEntity<BaseResponse>> signup(@RequestBody @Valid SignupRequest req, ServerHttpRequest request) {
        return authClient.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm(), req.getConsentReqs())
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return authClient.login(req.getEmail(), req.getPassword())
                                .map(loginResp -> responseFactory.ok(loginResp, request, HttpStatus.OK));
                    }
                    return Mono.just(responseFactory.error("signup failed", HttpStatus.BAD_REQUEST, request));
                });
    }

    @Operation(summary = "이메일 인증 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ConfirmEmailSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/emails/{email}\"\n  }\n}")))
    })
    @GetMapping("/emails/{email}")
    public Mono<ResponseEntity<BaseResponse>> confirmEmail(@PathVariable(name = "email") String email, @RequestParam String code, ServerHttpRequest request) {
        return authClient.confirmEmail(email, code)
                .map(result -> responseFactory.ok(result, request));
    }

    @Operation(summary = "이메일 인증코드 발송")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "SendCodeSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"code sent\",\n  \"request\": {\n    \"path\": \"/bff/v1/auth/emails/{email}\"\n  }\n}")))
    })
    @PostMapping("/emails/{email}")
    public Mono<ResponseEntity<BaseResponse>> sendCode(@PathVariable(name = "email") String email, ServerHttpRequest request) {
        return authClient.sendCode(email)
                .then(Mono.just(responseFactory.ok("code sent", request)));
    }

    @Operation(summary = "회원 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "WithdrawSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/withdraw/{userId}\"\n  }\n}")))
    })
    @PostMapping("/withdraw/{userId}")
    public Mono<ResponseEntity<BaseResponse>> withdraw(@PathVariable String userId, @RequestParam String withdrawReason, ServerHttpRequest request) {
        return authClient.withdraw(userId, withdrawReason)
                .map(result -> responseFactory.ok(result, request));
    }
}
