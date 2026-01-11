package com.study.api_gateway.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 (Swagger) 설정
 *
 * <p>API Gateway의 모든 BFF 엔드포인트에 대한 API 문서를 자동 생성합니다.
 *
 * <h2>주요 변경사항 (v2.0.0)</h2>
 * <ul>
 *   <li>JWT 토큰 기반 사용자 인증으로 전환</li>
 *   <li>X-User-Id 헤더를 통한 서버 측 사용자 ID 추출</li>
 *   <li>클라이언트 요청 본문에서 writerId 제거 (보안 강화)</li>
 *   <li>프로필 정보 및 카운트 정보 자동 enrichment 추가</li>
 * </ul>
 *
 * @version 2.0.0
 * @since 2025-01-16
 */
@OpenAPIDefinition(
		info = @Info(
				title = "Bander API Gateway",
				version = "2.0.0",
				description = """
						# Bander API Gateway
						
						Bander 서비스의 Backend for Frontend (BFF) 패턴을 구현한 API Gateway입니다.
						
						## 주요 기능
						
						### 인증 및 보안
						- **JWT 토큰 기반 인증**: 모든 API 요청에 Bearer 토큰 필수
						- **서버 측 사용자 식별**: X-User-Id 헤더를 통해 서버에서 사용자 ID 추출
						- **권한 검증**: 작성자 본인만 수정/삭제 가능한 리소스에 대한 자동 검증
						
						### 데이터 Enrichment
						- **프로필 정보 자동 추가**: 게시글, 댓글 등에 작성자 프로필 정보 자동 포함
						- **카운트 정보 제공**: 좋아요 수, 댓글 수 등 집계 정보 자동 계산
						- **캐시 최적화**: Redis를 활용한 프로필 정보 캐싱으로 성능 최적화
						
						### 도메인
						- **Article**: 게시글 관리 (일반, 이벤트, 공지사항)
						- **Comment**: 댓글 및 대댓글 관리
						- **Feed**: 사용자 맞춤 피드
						- **Support**: 문의 및 신고 관리
						
						## 인증 방법
						
						1. Authorization 헤더에 Bearer 토큰 포함
						2. 서버가 토큰 검증 후 X-User-Id 헤더 생성
						3. 모든 API 요청에서 X-User-Id를 통해 사용자 식별
						
						## 참고사항
						
						- **writerId 필드**: API 문서에 표시되나 클라이언트는 전송하지 않음 (READ_ONLY)
						- **응답 형식**: 모든 응답은 BaseResponse 형식으로 래핑됨
						- **에러 처리**: 표준 HTTP 상태 코드 및 상세 메시지 제공
						
						## 버전 히스토리
						
						- **v2.0.0** (2025-01-16): JWT 토큰 기반 인증 전환, 프로필/카운트 enrichment 추가
						- **v1.0.0** (2024-12-01): 초기 BFF API Gateway 구축
						""",
				contact = @Contact(
						name = "Bander Development Team",
						email = "dev@bander.com",
						url = "https://github.com/bander-project"
				),
				license = @License(
						name = "MIT License",
						url = "https://opensource.org/licenses/MIT"
				)
		),
		servers = {
				@Server(
						description = "Local Development Server",
						url = "http://localhost:9001"
				),
				@Server(
						description = "Development Server",
						url = "http://teambind.co.kr:9000"
				),
				@Server(
						description = "Production Server",
						url = "http://teambind.co.kr:9000"
				)
		},
		security = {
				@SecurityRequirement(name = "Bearer Authentication")
		}
)
@SecurityScheme(
		name = "Bearer Authentication",
		description = """
				JWT 토큰 기반 인증
				
				### 사용 방법
				1. 로그인 API를 통해 JWT 토큰 발급
				2. Authorization 헤더에 'Bearer {token}' 형식으로 포함
				3. 서버가 토큰 검증 후 X-User-Id 헤더 자동 생성
				
				### 토큰 검증
				- 서버에서 JWT 토큰의 유효성 검증
				- 만료된 토큰은 401 Unauthorized 응답
				- 토큰에서 추출한 사용자 ID는 X-User-Id 헤더로 전달
				
				### 보안 정책
				- 클라이언트는 절대 사용자 ID를 직접 전송하지 않음
				- 모든 사용자 식별은 서버 측 토큰 검증을 통해 이루어짐
				- writerId 등의 필드는 서버에서 자동으로 설정됨
				""",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER
)
@Configuration
public class OpenApiConfig {
	
	/**
	 * OpenAPI 커스터마이징
	 *
	 * <p>전역 보안 요구사항, 공통 응답 스키마, 파라미터 등을 설정합니다.
	 *
	 * @return 커스터마이징된 OpenAPI 객체
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.components(new Components()
						// 공통 응답 스키마
						.addResponses("UnauthorizedError", new ApiResponse()
								.description("인증 실패 - 유효하지 않거나 만료된 토큰")
								.content(new Content()
										.addMediaType("application/json", new MediaType()
												.example("""
														{
														  "isSuccess": false,
														  "code": 401,
														  "message": "Unauthorized",
														  "data": "유효하지 않은 토큰입니다"
														}
														""")
										)
								)
						)
						.addResponses("ForbiddenError", new ApiResponse()
								.description("권한 없음 - 리소스에 접근할 권한이 없음")
								.content(new Content()
										.addMediaType("application/json", new MediaType()
												.example("""
														{
														  "isSuccess": false,
														  "code": 403,
														  "message": "Forbidden",
														  "data": "본인만 수정/삭제할 수 있습니다"
														}
														""")
										)
								)
						)
						.addResponses("NotFoundError", new ApiResponse()
								.description("리소스를 찾을 수 없음")
								.content(new Content()
										.addMediaType("application/json", new MediaType()
												.example("""
														{
														  "isSuccess": false,
														  "code": 404,
														  "message": "Not Found",
														  "data": "요청한 리소스를 찾을 수 없습니다"
														}
														""")
										)
								)
						)
						.addResponses("BadRequestError", new ApiResponse()
								.description("잘못된 요청 - 유효성 검증 실패")
								.content(new Content()
										.addMediaType("application/json", new MediaType()
												.example("""
														{
														  "isSuccess": false,
														  "code": 400,
														  "message": "Bad Request",
														  "data": "필수 필드가 누락되었습니다"
														}
														""")
										)
								)
						)
						.addResponses("InternalServerError", new ApiResponse()
								.description("서버 내부 오류")
								.content(new Content()
										.addMediaType("application/json", new MediaType()
												.example("""
														{
														  "isSuccess": false,
														  "code": 500,
														  "message": "Internal Server Error",
														  "data": "서버 오류가 발생했습니다"
														}
														""")
										)
								)
						)
						// 공통 파라미터
						.addParameters("X-User-Id", new Parameter()
								.in(ParameterIn.HEADER.toString())
								.name("X-User-Id")
								.description("""
										사용자 식별 헤더 (서버에서 자동 생성)
										
										이 헤더는 서버에서 JWT 토큰을 검증한 후 자동으로 생성됩니다.
										클라이언트는 이 헤더를 직접 전송할 필요가 없으며, 전송하더라도 무시됩니다.
										""")
								.required(false)
								.schema(new StringSchema())
								.example("user_12345")
						)
				)
				.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
						.addList("Bearer Authentication")
				);
	}
}
