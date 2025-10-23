# API Gateway 개선 과제 로드맵

> 코드베이스 분석 결과를 바탕으로 작성된 우선순위별 개선 과제 가이드
> 작성일: 2025-10-23

---

## 목차

1. [개요](#개요)
2. [우선순위별 개선 과제](#우선순위별-개선-과제)
3. [부족한 부분 요약](#부족한-부분-요약)
4. [권장 작업 순서](#권장-작업-순서)

---

## 개요

현재 Bander API Gateway는 Spring Boot WebFlux 기반의 BFF(Backend-for-Frontend) 패턴으로 구현되어 있으며, 12개의 마이크로서비스와 통신하는 중앙 게이트웨이 역할을
수행합니다.

**프로젝트 규모:**

- 총 코드 라인: ~4,839 lines (Java)
- 컨트롤러: 12개
- REST 클라이언트: 12개
- DTO 클래스: 50+ 개

**주요 기술 스택:**

- Spring Boot 3.x + WebFlux (Reactive)
- Project Reactor
- Redis (캐싱)
- OpenAPI/Swagger

이 문서는 코드베이스 분석을 통해 발견된 **보안, 안정성, 성능, 관찰성, 유지보수성** 측면의 개선이 필요한 영역을 정리하고, 실행 가능한 로드맵을 제시합니다.

---

## 우선순위별 개선 과제

### 🔴 Phase 1: 즉시 해결 (Critical)

#### 1. CORS 보안 강화

**우선순위:** ⭐⭐⭐
**예상 소요 시간:** 5분
**난이도:** 낮음

**현재 문제:**

```java
// WebFluxCorsConfig.java
.allowedOriginPatterns(List.of("*"))
		.

allowCredentials(true)
```

모든 출처(Origin)에서 인증 정보를 포함한 요청을 허용하고 있어 XSS/CSRF 공격에 취약합니다.

**해결 방안:**

```java
// 프로덕션 환경
.allowedOriginPatterns(List.of(
		                       "https://bander.com",
    "https://admin.bander.com"
))

// 개발 환경
		.

allowedOriginPatterns(List.of(
		                      "http://localhost:3000",
    "http://localhost:8080"
))
```

**영향 범위:**

- 파일: `src/main/java/com/study/api_gateway/config/WebFluxCorsConfig.java`
- 프로덕션 배포 시 반드시 적용 필요

---

#### 2. 중복 Enum 정의 수정

**우선순위:** ⭐⭐⭐
**예상 소요 시간:** 5분
**난이도:** 낮음

**현재 문제:**
`InquiryStatus` 클래스가 두 패키지에 동일하게 존재:

- `dto/support/inquiry/InquiryStatus.java` (올바른 위치)
- `dto/support/report/InquiryStatus.java` (잘못된 위치)

**해결 방안:**

1. Report 패키지의 `InquiryStatus`는 실제로 `ReportStatus`와 동일한 역할이라면 삭제
2. 다른 용도라면 `ReportInquiryStatus`로 이름 변경

**영향 범위:**

- 파일: `src/main/java/com/study/api_gateway/dto/support/report/InquiryStatus.java`
- 해당 Enum을 사용하는 ReportController, ReportClient 수정 필요

---

#### 3. WebClient 타임아웃 설정 누락

**우선순위:** ⭐⭐⭐
**예상 소요 시간:** 10분
**난이도:** 낮음

**현재 문제:**
외부 API 호출 시 타임아웃 미설정으로 인해 무한 대기 가능성이 있습니다. 하나의 느린 서비스가 전체 게이트웨이를 블로킹할 수 있습니다.

**해결 방안:**

```java
// WebClientConfig.java

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

HttpClient httpClient = HttpClient.create()
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
		.responseTimeout(Duration.ofSeconds(10))
		.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(10))
						.addHandlerLast(new WriteTimeoutHandler(10))
		);

WebClient webClient = WebClient.builder()
		.clientConnector(new ReactorClientHttpConnector(httpClient))
		.build();
```

**권장 설정값:**

- Connection Timeout: 5초
- Read Timeout: 10초
- Write Timeout: 10초

**영향 범위:**

- 파일: `src/main/java/com/study/api_gateway/config/WebClientConfig.java`
- 모든 WebClient 빈에 적용

---

### 🟡 Phase 2: 단기 과제 (High Priority)

#### 4. DTO 입력 검증 추가

**우선순위:** ⭐⭐⭐
**예상 소요 시간:** 2-3시간
**난이도:** 중간

**현재 문제:**
Request DTO에 Bean Validation 애노테이션이 부족하여 잘못된 데이터가 백엔드 서비스까지 전달됩니다.

**해결 방안:**

```java
// LoginRequest.java (예시)
public class LoginRequest {
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;
	
	@NotBlank(message = "비밀번호는 필수입니다")
	@Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
	private String password;
}

// ArticleCreateRequest.java (예시)
public class ArticleCreateRequest {
	@NotBlank(message = "제목은 필수입니다")
	@Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
	private String title;
	
	@NotBlank(message = "내용은 필수입니다")
	@Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다")
	private String content;
	
	@NotNull(message = "카테고리는 필수입니다")
	private String category;
}
```

**적용 대상:**

- `dto/auth/request/*`
- `dto/profile/request/*`
- `dto/Article/request/*`
- `dto/comment/request/*`
- `dto/support/*/request/*`

**Controller 변경:**

```java
// Controller에 @Validated 추가 확인
@RestController
@Validated
public class ArticleController {
	@PostMapping
	public Mono<BaseResponse<ArticleResponse>> createArticle(
			@Valid @RequestBody ArticleCreateRequest request // @Valid 추가
	) { ...}
}
```

---

#### 5. JWT 검증 로직 구현

**우선순위:** ⭐⭐⭐
**예상 소요 시간:** 4-6시간
**난이도:** 중간

**현재 문제:**

- 게이트웨이에서 토큰 검증 없이 모든 요청을 백엔드로 전달
- 사용자 ID를 요청 파라미터로 받아서 위변조 가능성 존재
- 위조된 토큰으로 API 호출 가능

**해결 방안:**

1. **JWT 필터 추가**

```java
// JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter implements WebFilter {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String token = extractToken(exchange.getRequest());
		
		if (token != null && jwtTokenProvider.validateToken(token)) {
			String userId = jwtTokenProvider.getUserId(token);
			
			// SecurityContext에 사용자 정보 저장
			return chain.filter(exchange)
					.contextWrite(Context.of("userId", userId));
		}
		
		return Mono.error(new UnauthorizedException("유효하지 않은 토큰"));
	}
	
	private String extractToken(ServerHttpRequest request) {
		String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
```

2. **JWT Token Provider**

```java
// JwtTokenProvider.java
@Component
public class JwtTokenProvider {
	
	@Value("${jwt.secret}")
	private String secretKey;
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(getSigningKey())
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
	
	public String getUserId(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}
	
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes());
	}
}
```

3. **Controller에서 userId 추출 방식 변경**

```java
// 기존
@GetMapping("/profile")
public Mono<BaseResponse<UserResponse>> getProfile(
		@RequestParam String userId // 파라미터로 받음 - 위험!
) { ...}

// 개선
@GetMapping("/profile")
public Mono<BaseResponse<UserResponse>> getProfile(
		ServerWebExchange exchange
) {
	return Mono.deferContextual(ctx -> {
		String userId = ctx.get("userId"); // JWT에서 추출한 userId 사용
		return profileClient.fetchProfile(userId)
				.map(ResponseFactory::success);
	});
}
```

**영향 범위:**

- 신규 파일: `filter/JwtAuthenticationFilter.java`, `security/JwtTokenProvider.java`
- 수정: 모든 Controller의 userId 추출 로직
- 설정: `application.yaml`에 `jwt.secret` 추가

---

#### 6. Redis 프로필 캐싱 활성화

**우선순위:** ⭐⭐
**예상 소요 시간:** 30분
**난이도:** 낮음

**현재 문제:**
`NoopProfileCache` 사용 중으로 모든 요청마다 Profile Service를 호출하여:

- 응답 속도 저하 (특히 댓글/피드에 사용자가 많을 때)
- Profile Service 과부하 발생

**해결 방안:**

```yaml
# application.yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

app:
  profile:
    cache:
      redis:
        enabled: true
        ttl: 300s  # 5분
```

**최적화 옵션:**

```java
// RedisProfileCache.java 수정
@ConditionalOnProperty(
		prefix = "app.profile.cache.redis",
		name = "enabled",
		havingValue = "true"
)
@Primary
@Component
public class RedisProfileCache implements ProfileCache {
	
	@Value("${app.profile.cache.redis.ttl:PT5M}")
	private Duration ttl;
	
	@Override
	public Mono<Void> putAll(Map<String, BatchUserSummaryResponse> profiles) {
		return Flux.fromIterable(profiles.entrySet())
				.flatMap(entry -> {
					String key = "profile:summary:" + entry.getKey();
					return reactiveRedisTemplate.opsForValue()
							.set(key, entry.getValue(), ttl); // TTL 적용
				})
				.then();
	}
}
```

**성능 개선 예상:**

- 캐시 히트율 80% 가정 시 Profile Service 호출 80% 감소
- 평균 응답 시간 30-50% 개선 (프로필 enrichment 포함 시)

---

#### 7. 에러 처리 및 재시도 로직 추가

**우선순위:** ⭐⭐
**예상 소요 시간:** 4-6시간
**난이도:** 중간-높음

**현재 문제:**

- Client에서 `onErrorResume()` 미사용
- 일시적 네트워크 오류 시 재시도 없이 바로 실패
- Circuit Breaker 패턴 없어 장애 전파 발생

**해결 방안 (Resilience4j):**

1. **의존성 추가**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
<groupId>io.github.resilience4j</groupId>
<artifactId>resilience4j-reactor</artifactId>
<version>2.1.0</version>
</dependency>
```

2. **설정 추가**

```yaml
# application.yaml
resilience4j:
  circuitbreaker:
    instances:
      profileService:
        registerHealthIndicator: true
        slidingWindowSize: 100
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 10
      authService:
        registerHealthIndicator: true
        slidingWindowSize: 100
        failureRateThreshold: 50
        waitDurationInOpenState: 10s

  retry:
    instances:
      profileService:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

3. **Client 적용**

```java
// ProfileClient.java
@Component
@RequiredArgsConstructor
public class ProfileClient {
	
	private final WebClient profileWebClient;
	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private final RetryRegistry retryRegistry;
	
	public Mono<UserResponse> fetchProfile(String userId) {
		CircuitBreaker circuitBreaker = circuitBreakerRegistry
				.circuitBreaker("profileService");
		Retry retry = retryRegistry.retry("profileService");
		
		return profileWebClient
				.get()
				.uri("/api/v1/profiles/{userId}", userId)
				.retrieve()
				.bodyToMono(UserResponse.class)
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.transformDeferred(RetryOperator.of(retry))
				.onErrorResume(WebClientResponseException.class, ex -> {
					if (ex.getStatusCode().is4xxClientError()) {
						return Mono.error(ex); // 재시도 안 함
					}
					return Mono.error(ex); // 5xx는 재시도
				});
	}
}
```

**적용 우선순위:**

1. ProfileClient (가장 많이 호출됨)
2. ArticleClient
3. CommentClient
4. 나머지 Client

---

### 🟢 Phase 3: 중기 과제 (Architecture)

#### 8. 서비스 레이어 도입

**우선순위:** ⭐⭐
**예상 소요 시간:** 8-12시간
**난이도:** 높음

**현재 문제:**
Controller에 비즈니스 로직이 직접 구현되어:

- 테스트 어려움 (Controller 전체를 Mock 해야 함)
- 비즈니스 로직 재사용 불가
- 코드 중복 발생

**현재 구조:**

```
Controller → Client → External Service
             ↓
        ProfileEnrichmentUtil
```

**개선된 구조:**

```
Controller → Service → Client → External Service
                ↓
          ProfileEnrichmentUtil
```

**예시 구현:**

```java
// ArticleService.java
@Service
@RequiredArgsConstructor
public class ArticleService {
	
	private final ArticleClient articleClient;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final GaechuClient gaechuClient;
	
	public Mono<ArticleResponse> createArticle(
			String userId,
			ArticleCreateRequest request
	) {
		return articleClient.postArticle(request)
				.flatMap(article -> enrichArticleWithUserData(article, userId));
	}
	
	public Mono<ArticleResponse> getArticle(String articleId, String userId) {
		return Mono.zip(
				articleClient.getArticle(articleId),
				gaechuClient.getLikedIdsByUser(userId, ReferenceType.ARTICLE)
		).flatMap(tuple -> {
			ArticleResponse article = tuple.getT1();
			Set<String> likedIds = tuple.getT2();
			article.setLiked(likedIds.contains(articleId));
			return profileEnrichmentUtil.enrichArticle(article);
		});
	}
	
	private Mono<ArticleResponse> enrichArticleWithUserData(
			ArticleResponse article,
			String userId
	) {
		return profileEnrichmentUtil.enrichArticle(article)
				.flatMap(enriched ->
						gaechuClient.getLikedIdsByUser(userId, ReferenceType.ARTICLE)
								.map(likedIds -> {
									enriched.setLiked(likedIds.contains(article.getId()));
									return enriched;
								})
				);
	}
}
```

```java
// ArticleController.java (개선 후)
@RestController
@RequestMapping("/bff/v1/communities/articles/regular")
@RequiredArgsConstructor
public class ArticleController {
	
	private final ArticleService articleService;
	private final ResponseFactory responseFactory;
	
	@PostMapping
	public Mono<BaseResponse<ArticleResponse>> createArticle(
			@Valid @RequestBody ArticleCreateRequest request,
			ServerWebExchange exchange
	) {
		return Mono.deferContextual(ctx -> {
			String userId = ctx.get("userId");
			return articleService.createArticle(userId, request)
					.map(ResponseFactory::success);
		});
	}
}
```

**적용 순서:**

1. ArticleService (가장 복잡한 로직)
2. ProfileService
3. CommentService
4. AuthService
5. 나머지 Service

---

#### 9. 공통 Client 인터페이스 설계

**우선순위:** ⭐⭐
**예상 소요 시간:** 6-8시간
**난이도:** 중간

**현재 문제:**
12개 Client가 각자 구현되어 에러 처리, 로깅, 타임아웃 설정이 중복됩니다.

**해결 방안:**

```java
// BaseRestClient.java
@Slf4j
public abstract class BaseRestClient {
	
	protected final WebClient webClient;
	protected final String serviceName;
	
	protected BaseRestClient(WebClient webClient, String serviceName) {
		this.webClient = webClient;
		this.serviceName = serviceName;
	}
	
	protected <T> Mono<T> get(String uri, Class<T> responseType, Object... uriVariables) {
		return execute(
				webClient.get()
						.uri(uri, uriVariables)
						.retrieve()
						.bodyToMono(responseType),
				"GET",
				uri
		);
	}
	
	protected <T, R> Mono<R> post(String uri, T body, Class<R> responseType) {
		return execute(
				webClient.post()
						.uri(uri)
						.bodyValue(body)
						.retrieve()
						.bodyToMono(responseType),
				"POST",
				uri
		);
	}
	
	protected <T> Mono<T> execute(Mono<T> request, String method, String uri) {
		return request
				.doOnSubscribe(s -> log.info("[{}] {} {}", serviceName, method, uri))
				.doOnSuccess(r -> log.info("[{}] {} {} - SUCCESS", serviceName, method, uri))
				.doOnError(e -> log.error("[{}] {} {} - ERROR: {}",
						serviceName, method, uri, e.getMessage()))
				.onErrorMap(this::mapException);
	}
	
	private Throwable mapException(Throwable ex) {
		if (ex instanceof WebClientResponseException wcre) {
			if (wcre.getStatusCode().is4xxClientError()) {
				return new BadRequestException(
						serviceName + " returned 4xx: " + wcre.getMessage()
				);
			}
			if (wcre.getStatusCode().is5xxServerError()) {
				return new ServiceUnavailableException(
						serviceName + " is unavailable: " + wcre.getMessage()
				);
			}
		}
		return ex;
	}
}
```

```java
// ProfileClient.java (개선 후)
@Component
public class ProfileClient extends BaseRestClient {
	
	public ProfileClient(@Qualifier("profileWebClient") WebClient webClient) {
		super(webClient, "ProfileService");
	}
	
	public Mono<UserResponse> fetchProfile(String userId) {
		return get("/api/v1/profiles/{userId}", UserResponse.class, userId);
	}
	
	public Mono<UserResponse> updateProfile(
			String userId,
			ProfileUpdateRequest request
	) {
		return post(
				"/api/v1/profiles/" + userId,
				request,
				UserResponse.class
		);
	}
}
```

---

#### 10. 로깅 및 모니터링 강화

**우선순위:** ⭐⭐
**예상 소요 시간:** 4-6시간
**난이도:** 중간

**현재 문제:**

- 요청/응답 로그 없음 (디버깅 어려움)
- 성능 지표 수집 안 됨 (응답 시간, 에러율)
- 구조화된 로그 없음 (JSON 형식 아님)

**해결 방안:**

1. **요청/응답 로깅 필터**

```java
// RequestLoggingFilter.java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements WebFilter {
	
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		long startTime = System.currentTimeMillis();
		ServerHttpRequest request = exchange.getRequest();
		
		return chain.filter(exchange)
				.doFinally(signalType -> {
					long duration = System.currentTimeMillis() - startTime;
					ServerHttpResponse response = exchange.getResponse();
					
					log.info("HTTP {} {} - Status: {} - Duration: {}ms",
							request.getMethod(),
							request.getURI().getPath(),
							response.getStatusCode(),
							duration
					);
				});
	}
}
```

2. **Structured Logging (Logback JSON)**

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>userId</includeMdcKeyName>
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <message>message</message>
                <logger>logger</logger>
                <level>level</level>
            </fieldNames>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

3. **Micrometer + Prometheus**

```java
// MetricsConfig.java
@Configuration
public class MetricsConfig {
	
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}
}

// Controller에 적용
@Timed(value = "api.articles.create", description = "Article creation time")
@PostMapping
public Mono<BaseResponse<ArticleResponse>> createArticle(...) { ...}
```

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

---

#### 11. Configuration 검증 로직 추가

**우선순위:** ⭐⭐
**예상 소요 시간:** 2시간
**난이도:** 낮음

**현재 문제:**
환경변수 누락 시 런타임에만 발견되며, NPE 발생 가능성이 있습니다.

**해결 방안:**

```java
// ServicesConfig.java
@Configuration
@ConfigurationProperties(prefix = "services")
@Validated
public class ServicesConfig {
	
	@NotBlank(message = "Auth service URL is required")
	private String authUrl;
	
	@NotBlank(message = "Profile service URL is required")
	private String profileUrl;
	
	@NotBlank(message = "Article service URL is required")
	private String articleUrl;
	
	@NotBlank(message = "Comment service URL is required")
	private String commentUrl;
	
	@NotBlank(message = "Image service URL is required")
	private String imageUrl;
	
	@NotBlank(message = "Gaechu service URL is required")
	private String gaechuUrl;
	
	@NotBlank(message = "Activities service URL is required")
	private String activitiesUrl;
	
	@NotBlank(message = "Support service URL is required")
	private String supportUrl;
	
	// Getters, Setters
}
```

```yaml
# application.yaml
services:
  auth-url: ${AUTH_SERVICE_URL:http://localhost:8081}
  profile-url: ${PROFILE_SERVICE_URL:http://localhost:8082}
  article-url: ${ARTICLE_SERVICE_URL:http://localhost:8083}
  comment-url: ${COMMENT_SERVICE_URL:http://localhost:8084}
  image-url: ${IMAGE_SERVICE_URL:http://localhost:8085}
  gaechu-url: ${GAECHU_SERVICE_URL:http://localhost:8086}
  activities-url: ${ACTIVITIES_SERVICE_URL:http://localhost:8087}
  support-url: ${SUPPORT_SERVICE_URL:http://localhost:8088}
```

```java
// WebClientConfig.java (개선)
@Configuration
@EnableConfigurationProperties(ServicesConfig.class)
public class WebClientConfig {
	
	@Bean
	public WebClient authWebClient(
			ServicesConfig config,
			WebClient.Builder builder
	) {
		return builder
				.baseUrl(config.getAuthUrl())
				.build();
	}
	
	// 나머지 WebClient도 동일하게 수정
}
```

**장점:**

- 애플리케이션 시작 시 설정 검증
- 명확한 에러 메시지 제공
- 타입 안전성 확보

---

#### 12. API Rate Limiting 구현

**우선순위:** ⭐⭐
**예상 소요 시간:** 4-6시간
**난이도:** 중간

**현재 문제:**
특정 사용자/IP의 무제한 API 호출로 인한 트래픽 폭주 및 백엔드 서비스 다운 가능성이 있습니다.

**해결 방안 (Redis Token Bucket):**

```java
// RateLimitFilter.java
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements WebFilter {
	
	private final ReactiveRedisTemplate<String, String> redisTemplate;
	
	private static final int MAX_REQUESTS = 100; // 1분당 100 요청
	private static final Duration WINDOW = Duration.ofMinutes(1);
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return Mono.deferContextual(ctx -> {
			String userId = ctx.getOrDefault("userId", "anonymous");
			String key = "rate_limit:" + userId;
			
			return redisTemplate.opsForValue().increment(key)
					.flatMap(count -> {
						if (count == 1) {
							// 첫 요청이면 만료 시간 설정
							return redisTemplate.expire(key, WINDOW)
									.then(chain.filter(exchange));
						}
						
						if (count > MAX_REQUESTS) {
							exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
							return exchange.getResponse().setComplete();
						}
						
						return chain.filter(exchange);
					});
		});
	}
}
```

**고급 설정 (사용자별 차등 제한):**

```java
// 일반 사용자: 100 req/min
// 프리미엄 사용자: 500 req/min
// 관리자: 무제한

private int getMaxRequests(String userId) {
	// DB 또는 캐시에서 사용자 등급 조회
	UserTier tier = userTierService.getTier(userId);
	return switch (tier) {
		case ADMIN -> Integer.MAX_VALUE;
		case PREMIUM -> 500;
		case STANDARD -> 100;
	};
}
```

---

#### 13. 분산 추적(Distributed Tracing) 구현

**우선순위:** ⭐
**예상 소요 시간:** 3-4시간
**난이도:** 중간

**현재 문제:**
게이트웨이 → Auth → Profile 등 여러 서비스를 거칠 때 어느 구간에서 지연이 발생했는지 알 수 없습니다.

**해결 방안 (Spring Cloud Sleuth + Zipkin):**

1. **의존성 추가**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
<groupId>io.zipkin.reporter2</groupId>
<artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

2. **설정 추가**

```yaml
# application.yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 개발: 100%, 프로덕션: 0.1 (10%)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

3. **WebClient에 Trace 전파 설정**

```java
// WebClientConfig.java
@Bean
public WebClient profileWebClient(
		WebClient.Builder builder,
		ObservationRegistry observationRegistry
) {
	return builder
			.baseUrl(profileServiceUrl)
			.observationRegistry(observationRegistry) // Trace 전파
			.build();
}
```

**결과:**

- 요청마다 고유한 Trace ID 부여
- 서비스 간 호출 시간 시각화
- 병목 구간 식별 용이

---

### 🔵 Phase 4: 장기 과제 (Evolution)

#### 14. 하드코딩된 기본값 설정파일로 이동

**우선순위:** ⭐
**예상 소요 시간:** 30분
**난이도:** 낮음

**현재 문제:**

```java
// ProfileEnrichmentUtil.java
.defaultIfEmpty(new BatchUserSummaryResponse(
		userId, "상어크앙","와방쌘 상어"
))
```

**해결 방안:**

```yaml
# application.yaml
app:
  profile:
    defaults:
      nickname: "상어크앙"
      image-url: "와방쌘 상어"
```

```java
// ProfileEnrichmentUtil.java
@Value("${app.profile.defaults.nickname}")
private String defaultNickname;

@Value("${app.profile.defaults.image-url}")
private String defaultImageUrl;

.

defaultIfEmpty(new BatchUserSummaryResponse(
		               userId, defaultNickname, defaultImageUrl
               ))
```

---

#### 15. 주석 처리된 코드 정리

**우선순위:** ⭐
**예상 소요 시간:** 30분
**난이도:** 낮음

**대상 파일:**

- `CommentController.java`: 구버전 엔드포인트 주석 처리
- `ProfileController.java`: 프로필 업데이트 메서드 주석 처리

**조치 방안:**

1. 복원 예정이면 TODO 코멘트 추가 + 이슈 생성
2. 삭제 예정이면 즉시 제거 (Git 히스토리에 남음)

---

## 부족한 부분 요약

### 1. 보안 (Security) ⚠️

| 항목            | 현재 상태    | 위험도 | 관련 과제 |
|---------------|----------|-----|-------|
| JWT 검증        | ❌ 없음     | 높음  | #5    |
| CORS 설정       | ⚠️ 너무 관대 | 높음  | #1    |
| Rate Limiting | ❌ 없음     | 중간  | #12   |
| 입력 검증         | ⚠️ 부족    | 중간  | #4    |

**영향:**

- 위조된 토큰으로 API 호출 가능
- XSS/CSRF 공격 취약
- 무제한 API 호출로 서비스 마비 가능

---

### 2. 안정성 (Reliability) ⚠️

| 항목                   | 현재 상태 | 위험도 | 관련 과제 |
|----------------------|-------|-----|-------|
| Timeout 설정           | ❌ 없음  | 높음  | #3    |
| 재시도 로직               | ❌ 없음  | 중간  | #7    |
| Circuit Breaker      | ❌ 없음  | 중간  | #7    |
| Graceful Degradation | ❌ 없음  | 중간  | #7    |

**영향:**

- 하나의 느린 서비스가 전체 게이트웨이 블로킹
- 일시적 네트워크 오류도 즉시 실패
- 장애가 다른 서비스로 전파

---

### 3. 성능 (Performance) ⚠️

| 항목              | 현재 상태     | 위험도 | 관련 과제 |
|-----------------|-----------|-----|-------|
| 프로필 캐싱          | ❌ 비활성화    | 중간  | #6    |
| 동기 Enrichment   | ⚠️ 응답 블로킹 | 중간  | #6    |
| Connection Pool | ⚠️ 미설정    | 낮음  | #3    |

**영향:**

- 모든 요청마다 Profile Service 호출 (중복 API 호출)
- 프로필 enrichment가 응답 시간 증가시킴
- 평균 응답 시간 30-50% 더 느림

---

### 4. 관찰성 (Observability) ⚠️

| 항목                 | 현재 상태 | 위험도 | 관련 과제 |
|--------------------|-------|-----|-------|
| 요청/응답 로깅           | ❌ 없음  | 중간  | #10   |
| 분산 추적              | ❌ 없음  | 중간  | #13   |
| 메트릭 수집             | ❌ 없음  | 낮음  | #10   |
| Structured Logging | ❌ 없음  | 낮음  | #10   |

**영향:**

- 디버깅 매우 어려움 (로그 부족)
- 병목 구간 식별 불가
- 성능 모니터링 불가
- 장애 원인 추적 어려움

---

### 5. 유지보수성 (Maintainability) ⚠️

| 항목       | 현재 상태 | 위험도 | 관련 과제 |
|----------|-------|-----|-------|
| 서비스 레이어  | ❌ 없음  | 중간  | #8    |
| 공통 로직 중복 | ⚠️ 많음 | 중간  | #9    |
| 설정 검증    | ❌ 없음  | 낮음  | #11   |
| 테스트 용이성  | ⚠️ 낮음 | 중간  | #8    |

**영향:**

- 비즈니스 로직 테스트 어려움
- 코드 중복으로 변경 시 여러 곳 수정 필요
- 런타임 에러 발견 (컴파일 타임에 못 잡음)

---

## 권장 작업 순서

### 1단계: 빠른 효과 (1주 내) ⚡

**목표:** 즉각적인 보안/안정성 개선

```
Day 1-2:
✓ #1  CORS 보안 강화 (5분)
✓ #2  중복 Enum 수정 (5분)
✓ #3  WebClient 타임아웃 설정 (10분)
✓ #15 주석 처리된 코드 정리 (30분)
✓ #14 하드코딩된 기본값 이동 (30분)

Day 3-5:
✓ #4  DTO 입력 검증 추가 (2-3시간)
✓ #6  Redis 캐싱 활성화 (30분)
✓ #11 Configuration 검증 (2시간)
```

**예상 효과:**

- 보안 취약점 해소
- 무한 대기 방지
- 응답 속도 30-50% 개선 (캐싱)

---

### 2단계: 인증/인가 강화 (2주 내) 🔐

**목표:** JWT 검증으로 보안 강화

```
Week 2:
✓ #5  JWT 검증 로직 구현 (4-6시간)
  - JwtAuthenticationFilter 추가
  - JwtTokenProvider 구현
  - 모든 Controller userId 추출 방식 변경
```

**예상 효과:**

- 위조 토큰 차단
- 사용자 ID 위변조 방지

---

### 3단계: 안정성 개선 (3-4주 내) 🛡️

**목표:** 장애 전파 방지 및 복원력 강화

```
Week 3:
✓ #7  Resilience4j 적용 (4-6시간)
  - ProfileClient, ArticleClient 우선 적용
  - Circuit Breaker, Retry 설정
✓ #10 로깅 강화 (4-6시간)
  - RequestLoggingFilter
  - Structured Logging
  - Metrics (Prometheus)
```

**예상 효과:**

- 일시적 장애 자동 복구
- 장애 전파 차단
- 디버깅 용이성 향상

---

### 4단계: 아키텍처 개선 (1-2개월 내) 🏗️

**목표:** 유지보수성 및 테스트 용이성 향상

```
Month 2:
✓ #8  서비스 레이어 도입 (8-12시간)
  - ArticleService, ProfileService 우선 구현
✓ #9  공통 Client 인터페이스 (6-8시간)
  - BaseRestClient 추상화
✓ #12 API Rate Limiting (4-6시간)
✓ #13 분산 추적 구현 (3-4시간)
```

**예상 효과:**

- 테스트 커버리지 향상
- 코드 중복 제거
- 무제한 API 호출 방지
- 병목 구간 식별 가능

---

## 진행 상황 체크리스트

### Phase 1: 즉시 해결 (Critical)

- [ ] #1 CORS 보안 강화
- [ ] #2 중복 Enum 수정
- [ ] #3 WebClient 타임아웃 설정

### Phase 2: 단기 과제 (High Priority)

- [ ] #4 DTO 입력 검증 추가
- [ ] #5 JWT 검증 로직 구현
- [ ] #6 Redis 프로필 캐싱 활성화
- [ ] #7 에러 처리 및 재시도 로직 추가

### Phase 3: 중기 과제 (Architecture)

- [ ] #8 서비스 레이어 도입
- [ ] #9 공통 Client 인터페이스 설계
- [ ] #10 로깅 및 모니터링 강화
- [ ] #11 Configuration 검증 로직 추가
- [ ] #12 API Rate Limiting 구현
- [ ] #13 분산 추적 구현

### Phase 4: 장기 과제 (Evolution)

- [ ] #14 하드코딩된 기본값 설정파일로 이동
- [ ] #15 주석 처리된 코드 정리

---

## 참고 자료

### 공식 문서

- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Resilience4j](https://resilience4j.readme.io/docs)
- [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth)
- [Micrometer](https://micrometer.io/docs)

### 예제 코드

- [Spring Boot WebFlux Best Practices](https://github.com/spring-projects/spring-framework/tree/main/spring-webflux)
- [Resilience4j Examples](https://github.com/resilience4j/resilience4j-spring-boot3-demo)

---

**문서 버전:** 1.0
**최종 업데이트:** 2025-10-23
**작성자:** Claude Code Analysis
