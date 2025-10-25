# Authorization (인가) 가이드

API Gateway에서 역할 기반 인가(Role-Based Authorization)를 설정하는 방법입니다.

## 개요

- **인증(Authentication)**: 사용자가 누구인지 확인 (JWT 토큰 검증)
- **인가(Authorization)**: 인증된 사용자가 특정 리소스에 접근할 권한이 있는지 확인

현재 시스템은 **경로 기반 역할 검증**을 지원합니다.

## 역할 종류

`com.study.api_gateway.dto.auth.enums.Role`에 정의된 역할:

- `USER`: 일반 사용자
- `ADMIN`: 관리자
- `GUEST`: 게스트 (미인증 사용자)
- `PLACE_OWNER`: 장소 소유자

## 인가 규칙 설정 방법

### 1. AuthorizationConfig.java 수정

파일 위치: `src/main/java/com/study/api_gateway/config/AuthorizationConfig.java`

`configureAuthorization()` 메서드에서 인가 규칙을 설정합니다.

```java
private void configureAuthorization() {
	// ADMIN 전용 경로
	requireRole("/bff/v1/admin/**", Role.ADMIN);
	
	// PLACE_OWNER 전용 경로
	requireRole("/bff/v1/place/owner/**", Role.PLACE_OWNER);
	
	// ADMIN 또는 PLACE_OWNER 접근 가능
	requireAnyRole("/bff/v1/management/**", Role.ADMIN, Role.PLACE_OWNER);
	
	// 특수 케이스: 모든 역할 필요 (향후 다중 역할 지원 시)
	requireAllRoles("/bff/v1/special/**", Role.ADMIN, Role.USER);
}
```

### 2. 메서드 설명

#### `requireRole(pathPattern, role)`

특정 역할만 접근 가능

```java
requireRole("/bff/v1/admin/**",Role.ADMIN);
// -> ADMIN 역할만 /bff/v1/admin/** 경로 접근 가능
```

#### `requireAnyRole(pathPattern, roles...)`

여러 역할 중 하나라도 있으면 접근 가능

```java
requireAnyRole("/bff/v1/shared/**",Role.ADMIN, Role.PLACE_OWNER);
// -> ADMIN 또는 PLACE_OWNER 중 하나만 있어도 접근 가능
```

#### `requireAllRoles(pathPattern, roles...)`

모든 역할이 필요 (향후 다중 역할 지원 시 활용)

```java
requireAllRoles("/bff/v1/special/**",Role.ADMIN, Role.USER);
// -> ADMIN과 USER 역할을 모두 가져야 접근 가능 (현재는 단일 역할만 지원)
```

## 경로 패턴 문법

- `**`: 모든 경로 (하위 경로 포함)
	- 예: `/bff/v1/admin/**` → `/bff/v1/admin/users`, `/bff/v1/admin/settings/all`
- `*`: 단일 경로 세그먼트
	- 예: `/bff/v1/user/*/profile` → `/bff/v1/user/123/profile`
- `?`: 단일 문자
	- 예: `/bff/v1/item?` → `/bff/v1/item1`, `/bff/v1/item2`

## 동작 방식

### 1. 인가 규칙이 없는 경로

인증만 통과하면 접근 가능 (역할 무관)

```
GET /bff/v1/articles
Authorization: Bearer <valid-token>

→ ✅ 200 OK (모든 역할 허용)
```

### 2. 인가 규칙이 있는 경로

```java
// AuthorizationConfig.java
requireRole("/bff/v1/admin/users",Role.ADMIN);
```

**ADMIN 역할로 접근:**

```
GET /bff/v1/admin/users
Authorization: Bearer <token-with-ADMIN-role>

→ ✅ 200 OK
```

**USER 역할로 접근:**

```
GET /bff/v1/admin/users
Authorization: Bearer <token-with-USER-role>

→ ❌ 403 Forbidden
{
  "isSuccess": false,
  "code": 403,
  "data": "ADMIN 권한이 필요합니다",
  "request": null
}
```

### 3. 인증 실패 vs 인가 실패

**인증 실패 (401 Unauthorized):**

- 토큰이 없음
- 토큰이 만료됨
- 토큰 서명이 올바르지 않음
- 토큰 형식 오류

**인가 실패 (403 Forbidden):**

- 인증은 성공했지만 역할이 부족함
- 예: USER 역할로 ADMIN 전용 경로 접근

## HTTP 상태 코드

| 상태 코드            | 의미    | 발생 상황              |
|------------------|-------|--------------------|
| 200 OK           | 성공    | 인증 + 인가 모두 성공      |
| 401 Unauthorized | 인증 실패 | 토큰 없음, 만료, 서명 오류 등 |
| 403 Forbidden    | 인가 실패 | 토큰은 유효하지만 권한 부족    |

## 실제 사용 예시

### 예시 1: 관리자 전용 API

```java
// AuthorizationConfig.java
requireRole("/bff/v1/admin/**",Role.ADMIN);
```

```bash
# ✅ 성공: ADMIN 역할
curl -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/bff/v1/admin/users

# ❌ 실패: USER 역할
curl -H "Authorization: Bearer <user-token>" \
  http://localhost:8080/bff/v1/admin/users
# Response: 403 Forbidden - "ADMIN 권한이 필요합니다"
```

### 예시 2: 장소 소유자 전용 API

```java
// AuthorizationConfig.java
requireRole("/bff/v1/place/owner/**",Role.PLACE_OWNER);
```

```bash
# ✅ 성공: PLACE_OWNER 역할
curl -H "Authorization: Bearer <place-owner-token>" \
  http://localhost:8080/bff/v1/place/owner/dashboard

# ❌ 실패: USER 역할
curl -H "Authorization: Bearer <user-token>" \
  http://localhost:8080/bff/v1/place/owner/dashboard
# Response: 403 Forbidden - "PLACE_OWNER 권한이 필요합니다"
```

### 예시 3: 다중 역할 허용

```java
// AuthorizationConfig.java
requireAnyRole("/bff/v1/management/**",Role.ADMIN, Role.PLACE_OWNER);
```

```bash
# ✅ 성공: ADMIN 역할
curl -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/bff/v1/management/dashboard

# ✅ 성공: PLACE_OWNER 역할
curl -H "Authorization: Bearer <place-owner-token>" \
  http://localhost:8080/bff/v1/management/dashboard

# ❌ 실패: USER 역할
curl -H "Authorization: Bearer <user-token>" \
  http://localhost:8080/bff/v1/management/dashboard
# Response: 403 Forbidden - "ADMIN, PLACE_OWNER 중 하나의 권한이 필요합니다"
```

### 예시 4: 인가 규칙 없는 경로 (인증만 필요)

```java
// AuthorizationConfig.java에 규칙 없음
```

```bash
# ✅ 모든 역할 허용 (USER, ADMIN, GUEST, PLACE_OWNER 모두 OK)
curl -H "Authorization: Bearer <any-valid-token>" \
  http://localhost:8080/bff/v1/articles

# ❌ 토큰 없으면 실패
curl http://localhost:8080/bff/v1/articles
# Response: 401 Unauthorized - "Missing or invalid Authorization header"
```

## 디버깅

### 로그 확인

인가 관련 로그는 `JwtAuthenticationFilter`에서 출력됩니다:

```
# 인가 규칙 찾기
DEBUG - No authorization rule for path: /bff/v1/articles - allowing access

# 인가 성공
DEBUG - Authorization check for path: /bff/v1/admin/users, userRole: ADMIN, authorized: true

# 인가 실패
WARN - Authorization failed for path: /bff/v1/admin/users, userId: 12345, role: USER
WARN - Forbidden access - path: /bff/v1/admin/users, userRole: USER, message: ADMIN 권한이 필요합니다
```

### 공통 문제 해결

1. **403 Forbidden이 계속 발생**
	- 토큰의 `role` 클레임 확인 (대소문자 정확히 일치해야 함)
	- AuthorizationConfig에서 경로 패턴 확인
	- 로그에서 `userRole` 값 확인

2. **인가 규칙이 적용되지 않음**
	- 경로 패턴이 정확한지 확인 (`/bff/v1/admin/**` vs `/bff/v1/admin`)
	- AuthorizationConfig의 `configureAuthorization()` 메서드가 호출되는지 확인

3. **역할 파싱 오류**
	- Role enum에 정의된 값인지 확인
	- 토큰의 `role` 클레임이 대문자인지 확인 (`USER`, `ADMIN` 등)

## 향후 확장 가능성

- [ ] 다중 역할 지원 (사용자가 여러 역할을 동시에 가짐)
- [ ] 동적 역할 권한 설정 (DB 기반)
- [ ] 리소스별 세밀한 권한 제어 (예: 본인의 게시글만 수정)
- [ ] 역할 계층 구조 (ADMIN > PLACE_OWNER > USER > GUEST)

## 참고

- 인증 필터: `JwtAuthenticationFilter.java`
- 인가 설정: `AuthorizationConfig.java`
- 역할 정의: `dto/auth/enums/Role.java`
- 토큰 검증: `util/JwtTokenValidator.java`
