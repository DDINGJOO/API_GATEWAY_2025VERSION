# 분산 추적 (Distributed Tracing) 구현 가이드

## 개요

분산 추적은 마이크로서비스 아키텍처에서 하나의 요청이 여러 서비스를 거쳐 처리될 때, 전체 흐름을 추적하고 모니터링하는 기술입니다.

```
[Client] → [API Gateway] → [Auth Service] → [Profile Service] → [Image Service]
              │                  │                 │                  │
              └──────────────────┴─────────────────┴──────────────────┘
                            동일한 Trace ID로 연결
```

## 왜 필요한가?

### 문제 상황

```
사용자: "프로필 조회가 느려요"
개발자: "어디서 느린지 모르겠는데..."
```

### 분산 추적으로 해결

```
Trace ID: abc-123-def

[API Gateway]  ████░░░░░░░░░░░░░░░░░░░░░░░░░░  50ms
[Auth Service]      ███░░░░░░░░░░░░░░░░░░░░░░░  30ms
[Profile]               █████░░░░░░░░░░░░░░░░░  100ms
[Image Service]              ████████████████████  2000ms ← 병목!

총 소요시간: 2180ms
```

## 핵심 개념

### 1. Trace

- 하나의 요청에 대한 전체 여정
- 고유한 Trace ID로 식별

### 2. Span

- 하나의 작업 단위 (서비스 호출, DB 쿼리 등)
- Span ID로 식별
- 부모 Span ID로 계층 구조 표현

### 3. 전파 (Propagation)

- 서비스 간 호출 시 Trace 정보를 HTTP 헤더로 전달
- B3 형식 또는 W3C Trace Context 형식 사용

```
HTTP Headers:
X-B3-TraceId: 463ac35c9f6413ad48485a3953bb6124
X-B3-SpanId: 0020000000000001
X-B3-ParentSpanId: 0000000000000000
X-B3-Sampled: 1
```

---

## 구현 방법 (Spring Boot 3.x)

### 1. 의존성 추가

**모든 서비스**의 `build.gradle`에 추가:

```groovy
dependencies {
    // Micrometer Tracing (Spring Cloud Sleuth 대체)
    implementation 'io.micrometer:micrometer-tracing-bridge-brave'

    // Zipkin Reporter
    implementation 'io.zipkin.reporter2:zipkin-reporter-brave'

    // 이미 있다면 유지
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

### 2. 설정 추가

**모든 서비스**의 `application.yaml`에 추가:

```yaml
spring:
  application:
    name: api-gateway  # 각 서비스별로 다르게 설정

# Tracing 설정
management:
  tracing:
    sampling:
      probability: 1.0  # 1.0 = 100% 샘플링, 운영에서는 0.1 ~ 0.5 권장
    propagation:
      type: b3  # B3 형식 사용 (Zipkin 호환)

# Zipkin 설정
zipkin:
  tracing:
    endpoint: http://zipkin-server:9411/api/v2/spans
```

### 3. 각 서비스별 application name 설정

```yaml
# API Gateway
spring:
  application:
    name: api-gateway

# Auth Service
spring:
  application:
    name: auth-service

# Profile Service
spring:
  application:
    name: profile-service

# Image Service
spring:
  application:
    name: image-service
```

---

## Zipkin 서버 설치

### Docker로 실행 (권장)

```bash
# 단독 실행
docker run -d -p 9411:9411 openzipkin/zipkin

# Docker Compose
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  zipkin:
    image: openzipkin/zipkin
    container_name: zipkin
    ports:
      - "9411:9411"
    environment:
      - STORAGE_TYPE=mem  # 메모리 저장 (개발용)
    restart: unless-stopped
```

### 프로덕션 환경 (Elasticsearch 연동)

```yaml
# docker-compose-prod.yml
version: '3.8'
services:
  zipkin:
    image: openzipkin/zipkin
    container_name: zipkin
    ports:
      - "9411:9411"
    environment:
      - STORAGE_TYPE=elasticsearch
      - ES_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    restart: unless-stopped

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.17.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    restart: unless-stopped

volumes:
  es-data:
```

---

## WebClient 설정 (중요!)

WebClient 사용 시 자동으로 Trace 헤더가 전파되도록 설정:

```java
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	
	@Bean
	public WebClient.Builder webClientBuilder(ObservationRegistry observationRegistry) {
		return WebClient.builder()
				.observationRegistry(observationRegistry);
	}
}
```

또는 기존 WebClient에 적용:

```java

@Bean
public WebClient authWebClient(WebClient.Builder builder) {
	return builder
			.baseUrl("http://auth-service:8081")
			.build();
}
```

---

## 커스텀 Span 추가

중요한 작업에 수동으로 Span 추가:

```java
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
	
	private final Tracer tracer;
	
	public UserProfile getProfile(String userId) {
		// 새 Span 시작
		Span span = tracer.nextSpan().name("get-profile-from-cache");
		
		try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
			// 캐시 조회 로직
			span.tag("userId", userId);
			span.tag("cache.type", "redis");
			
			// ... 실제 로직
			
			return profile;
		} catch (Exception e) {
			span.error(e);
			throw e;
		} finally {
			span.end();
		}
	}
}
```

---

## Zipkin UI 사용법

### 1. 접속

```
http://localhost:9411
```

### 2. 검색 옵션

- **Service Name**: 특정 서비스의 trace만 필터
- **Span Name**: 특정 작업만 필터
- **Tags**: 커스텀 태그로 검색 (예: userId=123)
- **Duration**: 특정 시간 이상 걸린 요청만 필터

### 3. 화면 구성

```
┌─────────────────────────────────────────────────────────────┐
│ Trace ID: abc123                                             │
├─────────────────────────────────────────────────────────────┤
│ api-gateway     ████████████░░░░░░░░░░░░░░░░░░  200ms       │
│   └ auth        ░░░░████████░░░░░░░░░░░░░░░░░░  100ms       │
│   └ profile     ░░░░░░░░░░░░████████████████░░  300ms       │
│      └ redis    ░░░░░░░░░░░░░░░░█████░░░░░░░░░  50ms        │
│      └ image    ░░░░░░░░░░░░░░░░░░░░░████████░  150ms       │
└─────────────────────────────────────────────────────────────┘
```

---

## 로그에 Trace ID 포함

### logback-spring.xml 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 로그 출력 예시

```
2024-01-15 10:30:45.123 [reactor-http-nio-1] [abc123def456,span789] INFO  ProfileController - Fetching profile for userId: user123
2024-01-15 10:30:45.234 [reactor-http-nio-1] [abc123def456,span790] INFO  ProfileService - Cache miss, calling profile-service
2024-01-15 10:30:45.567 [reactor-http-nio-1] [abc123def456,span791] INFO  ProfileService - Profile fetched successfully
```

---

## 샘플링 전략

### 개발 환경

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% - 모든 요청 추적
```

### 운영 환경

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10% - 트래픽이 많을 때
      # 또는
      probability: 0.5  # 50% - 적당한 샘플링
```

### 조건부 샘플링 (고급)

```java
import brave.sampler.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {
	
	@Bean
	public Sampler customSampler() {
		return new Sampler() {
			@Override
			public boolean isSampled(long traceId) {
				// 에러가 발생한 요청은 항상 샘플링
				// 또는 특정 조건에 따라 샘플링 결정
				return true;
			}
		};
	}
}
```

---

## 체크리스트

### 서비스별 적용 체크리스트

| 서비스             | 의존성 추가 | application.yaml | WebClient 설정 | 테스트 |
|-----------------|--------|------------------|--------------|-----|
| API Gateway     | ☐      | ☐                | ☐            | ☐   |
| Auth Service    | ☐      | ☐                | ☐            | ☐   |
| Profile Service | ☐      | ☐                | ☐            | ☐   |
| Image Service   | ☐      | ☐                | ☐            | ☐   |
| Article Service | ☐      | ☐                | ☐            | ☐   |
| ...             | ☐      | ☐                | ☐            | ☐   |

### 인프라 체크리스트

| 항목                 | 상태 |
|--------------------|----|
| Zipkin 서버 배포       | ☐  |
| 네트워크 연결 확인         | ☐  |
| 저장소 설정 (ES/Memory) | ☐  |
| 샘플링 비율 결정          | ☐  |
| 모니터링 대시보드 연동       | ☐  |

---

## Jaeger 대안

Zipkin 대신 Jaeger를 사용할 수도 있습니다:

```groovy
// build.gradle
implementation 'io.opentelemetry:opentelemetry-exporter-jaeger'
```

```yaml
# application.yaml
management:
  tracing:
    sampling:
      probability: 1.0

otel:
  exporter:
    jaeger:
      endpoint: http://jaeger:14250
```

---

## 참고 자료

- [Micrometer Tracing 공식 문서](https://micrometer.io/docs/tracing)
- [Zipkin 공식 문서](https://zipkin.io/)
- [Spring Boot 3 Observability](https://spring.io/blog/2022/10/12/observability-with-spring-boot-3)
- [B3 Propagation 스펙](https://github.com/openzipkin/b3-propagation)
