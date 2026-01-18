# WebFlux 환경에서의 Resilience 패턴 가이드

## 목차

1. [개요](#개요)
2. [왜 상속이 WebFlux에서 덜 적합한가](#왜-상속이-webflux에서-덜-적합한가)
3. [Operator Composition 패턴](#operator-composition-패턴)
4. [Resilience4j와 WebFlux 통합](#resilience4j와-webflux-통합)
5. [구현 패턴 비교](#구현-패턴-비교)
6. [실전 구현 예제](#실전-구현-예제)
7. [테스트 전략](#테스트-전략)
8. [참고 자료](#참고-자료)

---

## 개요

### 문서 목적

이 문서는 Spring WebFlux 기반 API Gateway에서 Circuit Breaker, Timeout, Retry 등의 Resilience 패턴을 구현할 때 **상속(Inheritance) 대신 조합(
Composition)**을 사용해야 하는 이유와 구체적인 구현 방법을 설명한다.

### 핵심 요약

| 접근 방식                      | WebFlux 적합도 | 테스트 용이성 | 유연성 |
|----------------------------|-------------|---------|-----|
| 상속 (AbstractFacadeService) | 낮음          | 낮음      | 낮음  |
| Operator Composition       | 높음          | 높음      | 높음  |
| Decorator 패턴               | 중간          | 중간      | 중간  |

---

## 왜 상속이 WebFlux에서 덜 적합한가

### 1. 리액티브 프로그래밍 패러다임과의 불일치

리액티브 프로그래밍은 **데이터 스트림의 변환(Transformation)**을 핵심으로 한다.

```java
// 리액티브 방식: 연산자 체이닝
Mono.just(data)
    .map(this::transform)
    .filter(this::validate)
    .flatMap(this::save)
    .timeout(Duration.ofSeconds(5))
    .retry(3);
```

상속은 **컴파일 타임에 결정되는 정적 구조**인 반면, 리액티브 연산자는 **런타임에 조합되는 동적 파이프라인**이다.

```java
// 상속 방식: 정적 구조
public class AuthFacadeService extends AbstractFacadeService {
    public Mono<Response> login(Request req) {
        // 부모의 withCircuitBreaker()를 호출해야 함
        return withCircuitBreaker("auth", authClient.login(req));
    }
}
```

**문제점:**

- 부모 클래스의 메서드 호출을 **강제**해야 함
- 호출을 잊으면 Circuit Breaker가 적용되지 않음
- 컴파일러가 이를 검증하지 못함

### 2. 테스트 복잡도 증가

**상속 기반 테스트의 어려움:**

```java
// 상속 기반: 부모 클래스 모킹 필요
@Test
void testLogin() {
    // AbstractFacadeService의 withCircuitBreaker()를 어떻게 모킹할 것인가?
    // 1. 부모 클래스 전체를 모킹? -> 테스트 대상과 모킹 대상이 혼재
    // 2. CircuitBreaker를 주입받도록 수정? -> 상속의 의미 퇴색
}
```

**Composition 기반 테스트:**

```java
// Composition 기반: 의존성 주입으로 간단히 모킹
@Test
void testLogin() {
    CircuitBreakerOperator mockOperator = mock(CircuitBreakerOperator.class);
    when(mockOperator.protect(any())).thenReturn(Function.identity());

    AuthFacadeService service = new AuthFacadeService(mockOperator, authClient);
    // 깔끔한 테스트 가능
}
```

### 3. SOLID 원칙 관점

#### Single Responsibility Principle (SRP) 위반

```java
public abstract class AbstractFacadeService {
    // 책임 1: Circuit Breaker 관리
    protected <T> Mono<T> withCircuitBreaker(...) { }

    // 책임 2: Timeout 관리
    protected <T> Mono<T> withTimeout(...) { }

    // 책임 3: Retry 관리
    protected <T> Mono<T> withRetry(...) { }

    // 책임 4: 에러 핸들링
    protected <T> Mono<T> handleError(...) { }

    // 부모 클래스에 책임이 계속 축적됨
}
```

#### Open-Closed Principle (OCP) 제한

```java
// 새로운 요구사항: 특정 서비스에만 Rate Limiting 추가
// 상속 방식: 부모 클래스 수정 필요 -> 모든 자식에 영향
public abstract class AbstractFacadeService {
    protected <T> Mono<T> withRateLimiting(...) { } // 추가
}
```

### 4. 다중 관심사 조합의 어려움

실제 운영 환경에서는 여러 Cross-Cutting Concern을 조합해야 한다:

```
Request -> Rate Limit -> Circuit Breaker -> Timeout -> Retry -> Logging -> Response
```

**상속으로 이를 구현하면:**

```java
// 깊은 상속 체인 발생
class RateLimitingService { }
class CircuitBreakingService extends RateLimitingService { }
class TimeoutService extends CircuitBreakingService { }
class RetryService extends TimeoutService { }
class LoggingService extends RetryService { }
class AuthFacadeService extends LoggingService { } // 유지보수 악몽
```

**Composition으로 구현하면:**

```java
return authClient.login(req)
    .transform(rateLimiter.protect())
    .transform(circuitBreaker.protect("auth"))
    .timeout(Duration.ofSeconds(30))
    .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
    .doOnNext(response -> log.info("Success"));
```

---

## Operator Composition 패턴

### 핵심 개념

Reactor의 `transform()` 과 `transformDeferred()` 연산자를 활용하여 **재사용 가능한 연산자 체인**을 만든다.

### transform vs transformDeferred

```java
// transform: 구독 시점과 무관하게 동일한 연산자 적용
// 모든 구독자가 같은 연산자 인스턴스 공유
mono.transform(this::addTimeout);

// transformDeferred: 구독 시점마다 새로운 연산자 생성
// 각 요청마다 독립적인 상태 필요할 때 사용 (Circuit Breaker 등)
mono.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
```

**Circuit Breaker는 반드시 `transformDeferred` 사용:**

```java
// 잘못된 사용: 모든 요청이 같은 Circuit Breaker 상태 공유 문제 아님,
// 하지만 decoration이 한 번만 일어남
mono.transform(CircuitBreakerOperator.of(cb));

// 올바른 사용: 각 구독마다 새롭게 decoration
mono.transformDeferred(CircuitBreakerOperator.of(cb));
```

### 기본 구현

```java
@Component
@RequiredArgsConstructor
public class CircuitBreakerOperator {

    private final CircuitBreakerRegistry registry;
    private final FallbackHandler fallbackHandler;

    /**
     * Circuit Breaker + Timeout + Fallback을 조합한 연산자 반환
     */
    public <T> Function<Mono<T>, Mono<T>> protect(String serviceName) {
        CircuitBreaker cb = registry.circuitBreaker(serviceName);

        return mono -> mono
            .transformDeferred(io.github.resilience4j.reactor.circuitbreaker
                .CircuitBreakerOperator.of(cb))
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(throwable -> fallbackHandler.handle(serviceName, throwable));
    }

    /**
     * 커스텀 타임아웃 지원
     */
    public <T> Function<Mono<T>, Mono<T>> protect(String serviceName, Duration timeout) {
        CircuitBreaker cb = registry.circuitBreaker(serviceName);

        return mono -> mono
            .transformDeferred(io.github.resilience4j.reactor.circuitbreaker
                .CircuitBreakerOperator.of(cb))
            .timeout(timeout)
            .onErrorResume(throwable -> fallbackHandler.handle(serviceName, throwable));
    }
}
```

### 사용 예시

```java
@Service
@RequiredArgsConstructor
public class AuthFacadeService {

    private final CircuitBreakerOperator cbOperator;
    private final AuthClient authClient;

    public Mono<LoginResponse> login(LoginRequest request) {
        return authClient.login(request)
            .transform(cbOperator.protect("auth-service"));
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return authClient.refresh(refreshToken)
            .transform(cbOperator.protect("auth-service", Duration.ofSeconds(10)));
    }
}
```

---

## Resilience4j와 WebFlux 통합

### 의존성 설정

```gradle
dependencies {
    implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.2.0'
    implementation 'io.github.resilience4j:resilience4j-reactor:2.2.0'
}
```

### Circuit Breaker 설정

```yaml
# application.yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        slowCallRateThreshold: 80
        slowCallDurationThreshold: 3s
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        recordExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.reactive.function.client.WebClientRequestException
        ignoreExceptions:
          - com.example.BusinessException

    instances:
      auth-service:
        baseConfig: default
      profile-service:
        baseConfig: default
        failureRateThreshold: 30  # 더 민감하게 설정
      place-service:
        baseConfig: default
        slowCallDurationThreshold: 5s  # 더 관대하게 설정
```

### Resilience4j Reactor Operators

Resilience4j는 Reactor용 연산자를 제공한다:

```java
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.reactor.ratelimiter.RateLimiterOperator;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;

// 사용 예시
Mono<Response> resilientCall = client.call()
    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
    .transformDeferred(RetryOperator.of(retry))
    .transformDeferred(TimeLimiterOperator.of(timeLimiter))
    .transformDeferred(RateLimiterOperator.of(rateLimiter))
    .transformDeferred(BulkheadOperator.of(bulkhead));
```

### Fallback Handler 구현

```java
@Component
@Slf4j
public class FallbackHandler {

    public <T> Mono<T> handle(String serviceName, Throwable throwable) {
        log.error("Service {} failed: {}", serviceName, throwable.getMessage());

        if (throwable instanceof CallNotPermittedException) {
            // Circuit Breaker OPEN 상태
            return Mono.error(new ServiceUnavailableException(
                serviceName + " is currently unavailable. Please try again later."
            ));
        }

        if (throwable instanceof TimeoutException) {
            return Mono.error(new GatewayTimeoutException(
                serviceName + " did not respond in time."
            ));
        }

        if (throwable instanceof WebClientRequestException) {
            return Mono.error(new BadGatewayException(
                "Cannot connect to " + serviceName
            ));
        }

        // 기타 에러
        return Mono.error(new InternalServerException(
            "Unexpected error from " + serviceName
        ));
    }
}
```

---

## 구현 패턴 비교

### 패턴 1: 상속 기반 (Legacy)

```java
public abstract class AbstractFacadeService {

    @Autowired
    protected CircuitBreakerRegistry registry;

    protected <T> Mono<T> withCircuitBreaker(String name, Mono<T> mono) {
        CircuitBreaker cb = registry.circuitBreaker(name);
        return mono
            .transformDeferred(CircuitBreakerOperator.of(cb))
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(this::handleError);
    }

    protected <T> Mono<T> handleError(Throwable t) {
        // 에러 처리
    }
}

@Service
public class AuthFacadeService extends AbstractFacadeService {

    private final AuthClient authClient;

    public Mono<LoginResponse> login(LoginRequest req) {
        return withCircuitBreaker("auth", authClient.login(req));
    }
}
```

**평가:**

- 장점: 간단한 구현, 익숙한 패턴
- 단점: 테스트 어려움, 유연성 부족, 리액티브 패러다임 불일치

### 패턴 2: Operator Composition (Recommended)

```java
@Component
@RequiredArgsConstructor
public class ResilienceOperator {

    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;
    private final FallbackHandler fallbackHandler;

    public <T> Function<Mono<T>, Mono<T>> protect(String serviceName) {
        return mono -> mono
            .transformDeferred(CircuitBreakerOperator.of(cbRegistry.circuitBreaker(serviceName)))
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(t -> fallbackHandler.handle(serviceName, t));
    }

    public <T> Function<Mono<T>, Mono<T>> protectWithRetry(String serviceName) {
        return mono -> mono
            .transformDeferred(RetryOperator.of(retryRegistry.retry(serviceName)))
            .transformDeferred(CircuitBreakerOperator.of(cbRegistry.circuitBreaker(serviceName)))
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(t -> fallbackHandler.handle(serviceName, t));
    }
}

@Service
@RequiredArgsConstructor
public class AuthFacadeService {

    private final ResilienceOperator resilience;
    private final AuthClient authClient;

    public Mono<LoginResponse> login(LoginRequest req) {
        return authClient.login(req)
            .transform(resilience.protect("auth-service"));
    }
}
```

**평가:**

- 장점: 테스트 용이, 유연한 조합, 리액티브 패러다임 일치
- 단점: 약간의 학습 곡선

### 패턴 3: Decorator 패턴

```java
public class ResilientWebClient {

    private final WebClient webClient;
    private final ResilienceOperator resilience;
    private final String serviceName;

    public <T> Mono<T> get(String uri, Class<T> responseType) {
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(responseType)
            .transform(resilience.protect(serviceName));
    }

    public <T, R> Mono<R> post(String uri, T body, Class<R> responseType) {
        return webClient.post()
            .uri(uri)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .transform(resilience.protect(serviceName));
    }
}

// Factory로 생성
@Configuration
public class WebClientConfig {

    @Bean
    public ResilientWebClient authClient(WebClient.Builder builder, ResilienceOperator resilience) {
        WebClient webClient = builder.baseUrl("http://auth-service").build();
        return new ResilientWebClient(webClient, resilience, "auth-service");
    }
}
```

**평가:**

- 장점: Client 레벨에서 일괄 적용, 호출부 코드 간결
- 단점: 기존 Client 전면 수정 필요

### 패턴 비교표

| 항목        | 상속 | Operator Composition | Decorator |
|-----------|----|----------------------|-----------|
| 리액티브 적합도  | 낮음 | 높음                   | 중간        |
| 테스트 용이성   | 낮음 | 높음                   | 높음        |
| 유연성       | 낮음 | 높음                   | 중간        |
| 구현 복잡도    | 낮음 | 중간                   | 중간        |
| 기존 코드 수정량 | 중간 | 낮음                   | 높음        |
| SOLID 준수  | 낮음 | 높음                   | 높음        |

---

## 실전 구현 예제

### 프로젝트 구조

```
src/main/java/com/example/gateway/
├── config/
│   └── ResilienceConfig.java
├── resilience/
│   ├── ResilienceOperator.java
│   ├── FallbackHandler.java
│   └── ResilienceProperties.java
├── api/
│   └── auth/
│       ├── controller/
│       │   └── AuthController.java
│       ├── service/
│       │   └── AuthFacadeService.java
│       └── client/
│           └── AuthClient.java
└── exception/
    ├── ServiceUnavailableException.java
    ├── GatewayTimeoutException.java
    └── BadGatewayException.java
```

### ResilienceConfig.java

```java
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(50)
            .slowCallRateThreshold(80)
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();

        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .retryOnException(e -> e instanceof WebClientRequestException)
            .build();

        return RetryRegistry.of(defaultConfig);
    }
}
```

### ResilienceOperator.java

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ResilienceOperator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final FallbackHandler fallbackHandler;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 기본 보호: Circuit Breaker + Timeout + Fallback
     */
    public <T> Function<Mono<T>, Mono<T>> protect(String serviceName) {
        return protect(serviceName, DEFAULT_TIMEOUT);
    }

    /**
     * 커스텀 타임아웃 보호
     */
    public <T> Function<Mono<T>, Mono<T>> protect(String serviceName, Duration timeout) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);

        return mono -> mono
            .doOnSubscribe(s -> log.debug("Calling {} with circuit breaker", serviceName))
            .transformDeferred(CircuitBreakerOperator.of(cb))
            .timeout(timeout)
            .doOnError(e -> log.warn("Error from {}: {}", serviceName, e.getMessage()))
            .onErrorResume(t -> fallbackHandler.handle(serviceName, t));
    }

    /**
     * Retry 포함 보호: Retry -> Circuit Breaker -> Timeout -> Fallback
     */
    public <T> Function<Mono<T>, Mono<T>> protectWithRetry(String serviceName) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);
        Retry retry = retryRegistry.retry(serviceName);

        return mono -> mono
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(CircuitBreakerOperator.of(cb))
            .timeout(DEFAULT_TIMEOUT)
            .onErrorResume(t -> fallbackHandler.handle(serviceName, t));
    }

    /**
     * Flux용 보호
     */
    public <T> Function<Flux<T>, Flux<T>> protectFlux(String serviceName) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);

        return flux -> flux
            .transformDeferred(CircuitBreakerOperator.of(cb))
            .timeout(DEFAULT_TIMEOUT)
            .onErrorResume(t -> fallbackHandler.handleFlux(serviceName, t));
    }
}
```

### FallbackHandler.java

```java
@Component
@Slf4j
public class FallbackHandler {

    public <T> Mono<T> handle(String serviceName, Throwable throwable) {
        ErrorCode errorCode = mapToErrorCode(throwable);
        String message = buildErrorMessage(serviceName, throwable);

        log.error("Fallback triggered for {}: {} - {}",
            serviceName, errorCode, throwable.getMessage());

        return Mono.error(new GatewayException(errorCode, message));
    }

    public <T> Flux<T> handleFlux(String serviceName, Throwable throwable) {
        return handle(serviceName, throwable).flux();
    }

    private ErrorCode mapToErrorCode(Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            return ErrorCode.SERVICE_UNAVAILABLE;
        }
        if (throwable instanceof TimeoutException) {
            return ErrorCode.GATEWAY_TIMEOUT;
        }
        if (throwable instanceof WebClientRequestException
            || throwable instanceof IOException) {
            return ErrorCode.BAD_GATEWAY;
        }
        if (throwable instanceof WebClientResponseException ex) {
            if (ex.getStatusCode().is5xxServerError()) {
                return ErrorCode.BAD_GATEWAY;
            }
        }
        return ErrorCode.INTERNAL_ERROR;
    }

    private String buildErrorMessage(String serviceName, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            return String.format("%s is temporarily unavailable. Please try again later.", serviceName);
        }
        if (throwable instanceof TimeoutException) {
            return String.format("%s did not respond within the expected time.", serviceName);
        }
        if (throwable instanceof WebClientRequestException) {
            return String.format("Unable to connect to %s.", serviceName);
        }
        return String.format("An error occurred while communicating with %s.", serviceName);
    }
}
```

### AuthFacadeService.java

```java
@Service
@RequiredArgsConstructor
public class AuthFacadeService {

    private final ResilienceOperator resilience;
    private final AuthClient authClient;

    private static final String SERVICE_NAME = "auth-service";

    public Mono<LoginResponse> login(LoginRequest request) {
        return authClient.login(request)
            .transform(resilience.protect(SERVICE_NAME));
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return authClient.refresh(refreshToken)
            .transform(resilience.protect(SERVICE_NAME, Duration.ofSeconds(10)));
    }

    public Mono<Void> logout(String userId) {
        return authClient.logout(userId)
            .transform(resilience.protect(SERVICE_NAME));
    }
}
```

### AuthController.java

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacadeService authFacadeService;
    private final ResponseFactory responseFactory;

    @PostMapping("/login")
    public Mono<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            ServerHttpRequest httpRequest) {

        return authFacadeService.login(request)
            .map(response -> responseFactory.ok(response, httpRequest));
    }

    @PostMapping("/refresh")
    public Mono<ApiResponse<TokenResponse>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken,
            ServerHttpRequest httpRequest) {

        return authFacadeService.refreshToken(refreshToken)
            .map(response -> responseFactory.ok(response, httpRequest));
    }
}
```

---

## 테스트 전략

### Unit Test: ResilienceOperator

```java
@ExtendWith(MockitoExtension.class)
class ResilienceOperatorTest {

    @Mock
    private CircuitBreakerRegistry cbRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private FallbackHandler fallbackHandler;

    private ResilienceOperator operator;

    @BeforeEach
    void setUp() {
        CircuitBreaker cb = CircuitBreaker.ofDefaults("test");
        when(cbRegistry.circuitBreaker("test-service")).thenReturn(cb);

        operator = new ResilienceOperator(cbRegistry, retryRegistry, fallbackHandler);
    }

    @Test
    void protect_shouldApplyCircuitBreaker() {
        // Given
        Mono<String> source = Mono.just("success");

        // When
        Mono<String> result = source.transform(operator.protect("test-service"));

        // Then
        StepVerifier.create(result)
            .expectNext("success")
            .verifyComplete();
    }

    @Test
    void protect_shouldTriggerFallbackOnError() {
        // Given
        Mono<String> source = Mono.error(new RuntimeException("fail"));
        when(fallbackHandler.handle(eq("test-service"), any()))
            .thenReturn(Mono.error(new GatewayException(ErrorCode.INTERNAL_ERROR, "Fallback")));

        // When
        Mono<String> result = source.transform(operator.protect("test-service"));

        // Then
        StepVerifier.create(result)
            .expectError(GatewayException.class)
            .verify();

        verify(fallbackHandler).handle(eq("test-service"), any());
    }

    @Test
    void protect_shouldTimeoutAfterDuration() {
        // Given
        Mono<String> slowSource = Mono.delay(Duration.ofSeconds(5)).map(l -> "late");
        when(fallbackHandler.handle(eq("test-service"), any(TimeoutException.class)))
            .thenReturn(Mono.error(new GatewayException(ErrorCode.GATEWAY_TIMEOUT, "Timeout")));

        // When
        Mono<String> result = slowSource.transform(
            operator.protect("test-service", Duration.ofMillis(100))
        );

        // Then
        StepVerifier.create(result)
            .expectError(GatewayException.class)
            .verify(Duration.ofSeconds(1));
    }
}
```

### Unit Test: FacadeService

```java
@ExtendWith(MockitoExtension.class)
class AuthFacadeServiceTest {

    @Mock
    private ResilienceOperator resilience;

    @Mock
    private AuthClient authClient;

    private AuthFacadeService service;

    @BeforeEach
    void setUp() {
        // ResilienceOperator가 identity 함수 반환하도록 설정 (통과)
        when(resilience.protect(anyString()))
            .thenReturn(Function.identity());

        service = new AuthFacadeService(resilience, authClient);
    }

    @Test
    void login_shouldReturnLoginResponse() {
        // Given
        LoginRequest request = new LoginRequest("user@test.com", "password");
        LoginResponse expected = new LoginResponse("token123", "refresh456");
        when(authClient.login(request)).thenReturn(Mono.just(expected));

        // When
        Mono<LoginResponse> result = service.login(request);

        // Then
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete();

        verify(authClient).login(request);
        verify(resilience).protect("auth-service");
    }
}
```

### Integration Test: Circuit Breaker 동작

```java
@SpringBootTest
@AutoConfigureWebTestClient
class CircuitBreakerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CircuitBreakerRegistry registry;

    @MockBean
    private AuthClient authClient;

    @Test
    void shouldOpenCircuitAfterFailures() {
        // Given: Auth client가 계속 실패하도록 설정
        when(authClient.login(any()))
            .thenReturn(Mono.error(new WebClientRequestException(
                new RuntimeException("Connection refused"),
                HttpMethod.POST,
                URI.create("http://auth-service"),
                HttpHeaders.EMPTY
            )));

        // When: 여러 번 요청
        for (int i = 0; i < 10; i++) {
            webTestClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(new LoginRequest("test@test.com", "pass"))
                .exchange()
                .expectStatus().is5xxServerError();
        }

        // Then: Circuit Breaker가 OPEN 상태
        CircuitBreaker cb = registry.circuitBreaker("auth-service");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
```

---

## 참고 자료

### 공식 문서

1. **Resilience4j**
	- [Resilience4j Documentation](https://resilience4j.readme.io/)
	- [Reactor Integration](https://resilience4j.readme.io/docs/getting-started-6)
	- [Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)

2. **Project Reactor**
	- [Reactor Reference Guide](https://projectreactor.io/docs/core/release/reference/)
	- [Transform Operators](https://projectreactor.io/docs/core/release/reference/#_transform)

3. **Spring WebFlux**
	- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)

### 추천 도서

1. **Reactive Programming with RxJava** - Tomasz Nurkiewicz
	- 리액티브 패러다임의 근본 이해

2. **Spring in Action (6th Edition)** - Craig Walls
	- Chapter 11: Introducing Reactor

3. **Release It! (2nd Edition)** - Michael Nygard
	- Stability Patterns (Circuit Breaker, Bulkhead, Timeout)

### 관련 아티클

1. [Netflix Hystrix to Resilience4j Migration](https://resilience4j.readme.io/docs/migration-guide-from-hystrix)
2. [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)
3. [Reactive Manifesto](https://www.reactivemanifesto.org/)

### 예제 프로젝트

1. [Resilience4j Demo](https://github.com/resilience4j/resilience4j-spring-boot2-demo)
2. [Spring Cloud Gateway Sample](https://github.com/spring-cloud-samples/spring-cloud-gateway-sample)

---

## 결론

WebFlux 환경에서 Resilience 패턴을 구현할 때:

1. **상속보다 Composition을 선택**하라
2. **Resilience4j의 Reactor Operators**를 활용하라
3. **transform/transformDeferred**로 재사용 가능한 연산자를 만들어라
4. **FallbackHandler를 분리**하여 에러 처리를 일관되게 유지하라
5. **테스트 용이성**을 항상 고려하라

이 패턴을 따르면 유지보수가 쉽고, 테스트 가능하며, 리액티브 패러다임에 충실한 코드를 작성할 수 있다.
