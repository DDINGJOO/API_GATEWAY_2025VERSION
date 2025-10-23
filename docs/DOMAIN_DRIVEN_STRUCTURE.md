# 도메인 중심 패키지 구조 - 실무 가이드

> "한 기능 수정 시 한 폴더만 열면 되는 구조"

작성일: 2025-10-23

---

## 현재 구조의 불편함 (실제 시나리오)

### 시나리오: "로그인 요청에 deviceId 필드 추가"

#### 현재 구조에서의 작업 흐름:

```
1. dto/auth/request/LoginRequest.java 열기
   ↓ (deviceId 필드 추가)

2. client/AuthClient.java 찾아서 열기
   ↓ (파라미터 확인, 필요시 수정)

3. controller/auth/AuthController.java 찾아서 열기
   ↓ (Validation 추가, 로직 수정)

4. service/??? (없음, Controller에 로직 섞여있음)
   ↓

5. dto/auth/response/LoginResponse.java 확인
   ↓

6. 테스트 작성하려면 또 찾아다니기...
```

**탐색 경로:**

```
📁 dto/auth/request/          (1번 폴더)
📁 client/                    (2번 폴더, 12개 파일 중 찾기)
📁 controller/auth/           (3번 폴더)
📁 dto/auth/response/         (4번 폴더)
```

**문제점:**

- 🔴 4개 이상의 폴더를 오가며 작업
- 🔴 IntelliJ에서 탭 10개 이상 열림
- 🔴 "어디까지 수정했지?" 헷갈림
- 🔴 코드 리뷰 시 diff가 여러 곳에 분산
- 🔴 새 팀원은 구조 파악하기 어려움

---

## 해결책: 도메인 중심 구조

### 개선된 작업 흐름:

```
📁 domain/auth/  ← 이 폴더 하나만 열면 됨!
   ├── AuthController.java      (3. 컨트롤러)
   ├── AuthService.java          (4. 서비스 로직)
   ├── AuthClient.java           (5. 외부 API 호출)
   ├── dto/
   │   ├── LoginRequest.java     (1. Request 수정)
   │   ├── LoginResponse.java    (2. Response 확인)
   │   ├── SignupRequest.java
   │   └── ...
   ├── exception/
   │   └── AuthException.java
   └── AuthServiceTest.java      (6. 테스트)
```

**작업 순서:**

```
1. domain/auth/ 폴더 열기
2. LoginRequest.java에서 deviceId 추가
3. AuthController.java에서 검증 로직 추가
4. AuthService.java에서 비즈니스 로직 수정
5. AuthClient.java에서 API 호출 확인
6. 모두 같은 폴더 안! ✅
```

**장점:**

- ✅ **단일 폴더에서 모든 작업 완료**
- ✅ IntelliJ 탭 최소화 (관련 파일만)
- ✅ 코드 리뷰 시 한 폴더만 보면 됨
- ✅ Git diff가 한 곳에 집중
- ✅ 새 팀원 온보딩 쉬움

---

## 권장 구조: 도메인 모듈 (실전형)

### 전체 구조

```
src/main/java/com/study/api_gateway/

├── ApiGatewayApplication.java
│
├── domain/                          ⭐ 비즈니스 로직의 핵심
│   │
│   ├── auth/                        📁 인증 도메인 (모든 인증 관련 코드)
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── AuthClient.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── SignupRequest.java
│   │   │   ├── TokenRefreshRequest.java
│   │   │   ├── PasswordChangeRequest.java
│   │   │   └── SimpleAuthResponse.java
│   │   ├── enums/
│   │   │   ├── Provider.java
│   │   │   ├── Role.java
│   │   │   └── Status.java
│   │   └── exception/
│   │       └── AuthenticationException.java
│   │
│   ├── profile/                     📁 프로필 도메인
│   │   ├── ProfileController.java
│   │   ├── ProfileService.java
│   │   ├── ProfileEnrichmentService.java  # util에서 이동
│   │   ├── ProfileClient.java
│   │   ├── dto/
│   │   │   ├── ProfileUpdateRequest.java
│   │   │   ├── HistoryUpdateRequest.java
│   │   │   ├── UserResponse.java
│   │   │   ├── UserPageResponse.java
│   │   │   ├── BatchUserSummaryResponse.java
│   │   │   └── ProfileSearchCriteria.java
│   │   ├── enums/
│   │   │   └── City.java
│   │   └── cache/
│   │       ├── ProfileCache.java
│   │       ├── RedisProfileCache.java
│   │       └── NoopProfileCache.java
│   │
│   ├── article/                     📁 게시글 도메인
│   │   ├── controller/              # 여러 컨트롤러가 있는 경우
│   │   │   ├── ArticleController.java
│   │   │   ├── NoticeController.java
│   │   │   └── EventController.java
│   │   ├── service/
│   │   │   ├── ArticleService.java
│   │   │   ├── NoticeService.java
│   │   │   └── EventService.java
│   │   ├── client/
│   │   │   ├── ArticleClient.java
│   │   │   ├── NoticeClient.java
│   │   │   └── EventClient.java
│   │   └── dto/
│   │       ├── ArticleCreateRequest.java
│   │       ├── ArticleUpdateRequest.java
│   │       ├── ArticleResponse.java
│   │       ├── EventArticleCreateRequest.java
│   │       ├── EventArticleResponse.java
│   │       └── ArticleCursorPageResponse.java
│   │
│   ├── comment/                     📁 댓글 도메인
│   │   ├── CommentController.java
│   │   ├── CommentService.java
│   │   ├── CommentClient.java
│   │   └── dto/
│   │       ├── RootCommentCreateRequest.java
│   │       ├── ReplyCreateRequest.java
│   │       ├── CombinedCommentCreateRequest.java
│   │       └── CommentUpdateRequest.java
│   │
│   ├── like/                        📁 좋아요 도메인 (gaechu → like)
│   │   ├── LikeController.java
│   │   ├── LikeService.java
│   │   ├── LikeClient.java
│   │   └── dto/
│   │       ├── LikeCountResponse.java
│   │       └── LikeDetailResponse.java
│   │
│   ├── feed/                        📁 피드 도메인
│   │   ├── FeedController.java
│   │   ├── FeedService.java
│   │   ├── FeedClient.java
│   │   └── dto/
│   │       ├── FeedTotalsRequest.java
│   │       ├── FeedPageResponse.java
│   │       └── FeedTotalsResponse.java
│   │
│   ├── image/                       📁 이미지 도메인
│   │   ├── ImageClient.java
│   │   └── ImageConfirmService.java
│   │
│   └── support/                     📁 고객지원 도메인
│       ├── controller/
│       │   ├── InquiryController.java
│       │   ├── ReportController.java
│       │   └── FaqController.java
│       ├── service/
│       │   ├── InquiryService.java
│       │   └── ReportService.java
│       ├── client/
│       │   ├── InquiryClient.java
│       │   ├── ReportClient.java
│       │   └── FaqClient.java
│       ├── dto/
│       │   ├── inquiry/
│       │   │   ├── InquiryCreateRequest.java
│       │   │   ├── InquiryResponse.java
│       │   │   ├── AnswerCreateRequest.java
│       │   │   └── AnswerResponse.java
│       │   ├── report/
│       │   │   ├── ReportCreateRequest.java
│       │   │   ├── ReportWithdrawRequest.java
│       │   │   ├── ReportResponse.java
│       │   │   └── ReportPageResponse.java
│       │   └── faq/
│       │       └── FaqResponse.java
│       └── enums/
│           ├── InquiryStatus.java
│           ├── InquiryCategory.java
│           ├── ReportStatus.java
│           ├── ReferenceType.java
│           ├── ReportSortType.java
│           ├── SortDirection.java
│           └── FaqCategory.java
│
├── common/                          ⭐ 공통 인프라 코드
│   ├── dto/
│   │   └── BaseResponse.java
│   ├── response/
│   │   └── ResponseFactory.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── ErrorResponse.java
│   ├── filter/
│   │   ├── RequestLoggingFilter.java
│   │   └── JwtAuthenticationFilter.java
│   └── util/
│       └── RequestPathHelper.java
│
├── config/                          ⭐ 설정
│   ├── WebClientConfig.java
│   ├── WebFluxCorsConfig.java
│   ├── RedisConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
│
└── shared/                          ⭐ 도메인 간 공유 컴포넌트
    ├── controller/
    │   ├── HealthCheckController.java
    │   └── EnumsController.java
    └── constants/
        └── ApiConstants.java
```

---

## 실전 예시: Article 기능 수정

### 시나리오: "게시글 작성 시 태그 기능 추가"

#### Before (현재 구조):

```
1. IntelliJ에서 "ArticleCreateRequest" 검색
   → dto/Article/request/ArticleCreateRequest.java 열기

2. tags 필드 추가:
   private List<String> tags;

3. "ArticleClient" 검색
   → client/ArticleClient.java 찾아서 열기
   → postArticle() 메서드 확인

4. "ArticleController" 검색
   → controller/article/ArticleController.java 찾아서 열기
   → Validation 로직 추가

5. "ArticleResponse" 검색
   → dto/Article/response/ArticleResponse.java 열기
   → tags 필드 추가

6. ProfileEnrichmentUtil은 어디지?
   → util/ProfileEnrichmentUtil.java 찾기

7. 테스트 코드는?
   → test 폴더에서 또 찾기...
```

**IntelliJ 탭 상황:**

```
[ArticleCreateRequest] [ArticleClient] [ArticleController]
[ArticleResponse] [ProfileEnrichmentUtil] [ArticleService?] ...
```

**시간 낭비:**

- 파일 찾기: 2분
- 컨텍스트 스위칭: 5분
- "어디 수정했더라?" 확인: 3분
- **총 10분 낭비** 😫

---

#### After (도메인 구조):

```
1. domain/article/ 폴더 열기 (Project 탭에서 한 번에 보임)

domain/article/
├── controller/
│   └── ArticleController.java     ← 3. 여기서 Validation
├── service/
│   └── ArticleService.java        ← 4. 여기서 비즈니스 로직
├── client/
│   └── ArticleClient.java         ← 5. 여기서 API 호출 확인
└── dto/
    ├── ArticleCreateRequest.java  ← 1. tags 필드 추가
    └── ArticleResponse.java       ← 2. tags 필드 추가

2. 순서대로 수정:
   1) ArticleCreateRequest.java: tags 필드 추가
   2) ArticleResponse.java: tags 필드 추가
   3) ArticleController.java: @Valid 확인
   4) ArticleService.java: 태그 처리 로직 추가
   5) ArticleClient.java: API 스펙 확인

3. 모두 같은 폴더! IntelliJ 왼쪽 Project 탭만 봐도 한눈에 파악!
```

**IntelliJ 탭 상황:**

```
[ArticleCreateRequest] [ArticleResponse] [ArticleController] [ArticleService]
← 모두 domain/article/ 안! 찾기 쉬움!
```

**시간 절약:**

- 파일 찾기: 10초 (같은 폴더 안)
- 컨텍스트 스위칭: 최소화
- **총 9분 절약** ✅

---

## 마이그레이션 가이드 (단계별)

### Phase 1: 준비 단계 (1시간)

#### 1-1. 새 패키지 구조 생성

```
src/main/java/com/study/api_gateway/
└── domain/
    ├── auth/
    │   ├── dto/
    │   └── enums/
    ├── profile/
    │   ├── dto/
    │   ├── enums/
    │   └── cache/
    ├── article/
    │   ├── controller/
    │   ├── service/
    │   ├── client/
    │   └── dto/
    └── ... (나머지 도메인)
```

**IntelliJ 작업:**

1. `com.study.api_gateway` 우클릭
2. New → Package
3. `domain.auth.dto` 입력 (한 번에 생성됨)
4. 반복

---

### Phase 2: 도메인별 이동 (우선순위별)

#### 2-1. Auth 도메인 먼저 (가장 독립적)

**이동 순서:**

```
1. Controller 이동
   controller/auth/AuthController.java
   → domain/auth/AuthController.java

2. Client 이동
   client/AuthClient.java
   → domain/auth/AuthClient.java

3. DTO 이동
   dto/auth/request/*
   dto/auth/response/*
   → domain/auth/dto/*
   (request/response 폴더는 제거하고 평탄화)

4. Enum 이동
   dto/auth/enums/*
   → domain/auth/enums/*

5. Service 신규 생성
   → domain/auth/AuthService.java
```

**IntelliJ Refactor 기능 사용:**

1. `AuthController.java` 선택
2. `F6` (Move) 또는 우클릭 → Refactor → Move
3. `domain.auth` 선택
4. Refactor 버튼
5. IntelliJ가 모든 import 자동 수정! ✅

**예상 소요 시간:** 10분

---

#### 2-2. Profile 도메인 (캐시 포함)

```
domain/profile/
├── ProfileController.java            ← controller/profile/
├── ProfileService.java                ← 신규 생성
├── ProfileEnrichmentService.java      ← util/ProfileEnrichmentUtil 이동+이름변경
├── ProfileClient.java                 ← client/
├── dto/
│   ├── ProfileUpdateRequest.java      ← dto/profile/request/
│   ├── UserResponse.java              ← dto/profile/response/
│   └── ...
├── enums/
│   └── City.java                      ← dto/profile/enums/
└── cache/
    ├── ProfileCache.java              ← util/cache/
    ├── RedisProfileCache.java         ← util/cache/
    └── NoopProfileCache.java          ← util/cache/
```

**특별 작업: Util → Service 전환**

```java
// Before: util/ProfileEnrichmentUtil.java
@Component
public class ProfileEnrichmentUtil {
    // 로직...
}

// After: domain/profile/ProfileEnrichmentService.java
@Service
public class ProfileEnrichmentService {
    // 동일한 로직, 더 명확한 이름
}
```

**예상 소요 시간:** 15분

---

#### 2-3. Article 도메인 (복잡한 구조)

```
domain/article/
├── controller/                        # 여러 컨트롤러
│   ├── ArticleController.java
│   ├── NoticeController.java
│   └── EventController.java
├── service/
│   ├── ArticleService.java
│   ├── NoticeService.java
│   └── EventService.java
├── client/
│   ├── ArticleClient.java
│   ├── NoticeClient.java
│   └── EventClient.java
└── dto/
    ├── ArticleCreateRequest.java
    ├── ArticleResponse.java
    ├── EventArticleCreateRequest.java
    └── ...
```

**주의사항:**

- `dto/Article/` (대문자) → `domain/article/dto/` (소문자)
- Notice, Event도 article 도메인에 포함

**예상 소요 시간:** 20분

---

#### 2-4. 나머지 도메인 (일괄 처리)

**순서:**

1. Comment (단순) - 5분
2. Like (단순) - 5분
3. Feed (단순) - 5분
4. Image (단순) - 5분
5. Support (복잡) - 15분

**총 예상 시간:** 35분

---

### Phase 3: 공통 코드 정리 (30분)

#### 3-1. common 패키지 생성

```
common/
├── dto/
│   └── BaseResponse.java             ← dto/
├── response/
│   └── ResponseFactory.java          ← util/
├── exception/
│   ├── GlobalExceptionHandler.java   ← config/
│   ├── BusinessException.java        ← 신규
│   └── ErrorCode.java                ← 신규
├── filter/
│   └── RequestLoggingFilter.java     ← 신규 (향후 추가)
└── util/
    └── RequestPathHelper.java        ← util/
```

---

#### 3-2. shared 패키지 생성

```
shared/
├── controller/
│   ├── HealthCheckController.java    ← controller/
│   └── EnumsController.java          ← controller/enums/
└── constants/
    └── ApiConstants.java             ← 신규
```

---

### Phase 4: Service 레이어 추가 (점진적, 별도 작업)

각 도메인에 Service 신규 생성:

```java
// domain/auth/AuthService.java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthClient authClient;

    public Mono<LoginResponse> login(LoginRequest request) {
        // 비즈니스 로직
        return authClient.login(request);
    }
}
```

---

## 마이그레이션 체크리스트

### Week 1: 구조 생성 및 Auth 마이그레이션

- [ ] `domain/` 패키지 생성
- [ ] Auth 도메인 이동 (10분)
	- [ ] AuthController
	- [ ] AuthClient
	- [ ] DTO 이동
	- [ ] Enum 이동
- [ ] 빌드 및 테스트 확인
- [ ] Git commit: `refactor: migrate auth domain to new structure`

### Week 2: Profile, Article 마이그레이션

- [ ] Profile 도메인 이동 (15분)
	- [ ] ProfileEnrichmentUtil → ProfileEnrichmentService
	- [ ] Cache 패키지 이동
- [ ] Article 도메인 이동 (20분)
	- [ ] Notice, Event 포함
- [ ] 빌드 및 테스트 확인
- [ ] Git commit: `refactor: migrate profile and article domains`

### Week 3: 나머지 도메인 마이그레이션

- [ ] Comment 도메인 (5분)
- [ ] Like 도메인 (5분)
- [ ] Feed 도메인 (5분)
- [ ] Image 도메인 (5분)
- [ ] Support 도메인 (15분)
- [ ] 빌드 및 테스트 확인
- [ ] Git commit: `refactor: migrate remaining domains`

### Week 4: 공통 코드 정리

- [ ] common 패키지 생성 및 이동
- [ ] shared 패키지 생성 및 이동
- [ ] 기존 빈 폴더 삭제
	- [ ] `controller/` (shared로 이동한 것 제외)
	- [ ] `client/` (전체 삭제)
	- [ ] `dto/` (common 제외 전체 삭제)
	- [ ] `util/` (common으로 이동)
- [ ] 빌드 및 전체 테스트
- [ ] Git commit: `refactor: organize common and shared packages`

### Week 5: Service 레이어 추가 (점진적)

- [ ] AuthService 생성 및 적용
- [ ] ProfileService 생성 및 적용
- [ ] ArticleService 생성 및 적용
- [ ] Git commit: `feat: add service layer for auth, profile, article`

---

## 실제 파일 이동 예시 (IntelliJ)

### 예시 1: AuthController 이동

**Before:**

```
controller/auth/AuthController.java
package com.study.api_gateway.controller.auth;
```

**이동 방법:**

1. `AuthController.java` 선택
2. `F6` (Move) 또는 우클릭 → Refactor → Move
3. "To package" 입력: `com.study.api_gateway.domain.auth`
4. "Search in comments and strings" 체크
5. Refactor 버튼 클릭

**After:**

```
domain/auth/AuthController.java
package com.study.api_gateway.domain.auth;
```

**IntelliJ가 자동 처리:**

- ✅ 패키지 선언 변경
- ✅ 모든 파일의 import 문 변경
- ✅ 테스트 코드의 참조 변경

---

### 예시 2: DTO 일괄 이동

**Before:**

```
dto/auth/request/LoginRequest.java
dto/auth/request/SignupRequest.java
dto/auth/response/LoginResponse.java
```

**이동 방법:**

1. `dto/auth/request/` 폴더의 모든 파일 선택 (Ctrl/Cmd + A)
2. `F6` (Move)
3. "To package": `com.study.api_gateway.domain.auth.dto`
4. Refactor
5. `dto/auth/response/` 파일도 동일하게 이동

**After:**

```
domain/auth/dto/LoginRequest.java
domain/auth/dto/SignupRequest.java
domain/auth/dto/LoginResponse.java
```

**결과:**

- request/response 폴더 구분 제거 (평탄화)
- 모든 DTO가 `domain/auth/dto/` 한 곳에

---

## 새 구조에서의 작업 흐름

### 시나리오 1: 새 API 추가 "비밀번호 재설정"

**작업 위치:** `domain/auth/` 폴더 하나만!

```
1. domain/auth/dto/PasswordResetRequest.java 생성
   @NotBlank String email;
   @NotBlank String resetCode;
   @NotBlank String newPassword;

2. domain/auth/dto/PasswordResetResponse.java 생성
   boolean success;
   String message;

3. domain/auth/AuthService.java에 메서드 추가
   public Mono<PasswordResetResponse> resetPassword(
       PasswordResetRequest request
   ) { ... }

4. domain/auth/AuthClient.java에 메서드 추가
   public Mono<PasswordResetResponse> resetPassword(
       PasswordResetRequest request
   ) { ... }

5. domain/auth/AuthController.java에 엔드포인트 추가
   @PostMapping("/password/reset")
   public Mono<BaseResponse<PasswordResetResponse>> resetPassword(
       @Valid @RequestBody PasswordResetRequest request
   ) { ... }
```

**IntelliJ Project 탭 상황:**

```
📁 domain/auth/  ← 펼쳐놓기만 하면 됨
  ├── AuthController.java
  ├── AuthService.java
  ├── AuthClient.java
  └── dto/
      ├── PasswordResetRequest.java   ← 여기 추가
      ├── PasswordResetResponse.java  ← 여기 추가
      ├── LoginRequest.java
      └── ...
```

**시간 절약:**

- 파일 찾기: 0초 (같은 폴더)
- 전체 작업 시간: 10분 → 5분

---

### 시나리오 2: 버그 수정 "게시글 수정 API 에러"

**Before (현재 구조):**

```
1. "어디가 문제지?" → 여러 폴더 뒤지기
   - controller/article/ArticleController.java 확인
   - client/ArticleClient.java 확인
   - dto/Article/request/ArticleUpdateRequest.java 확인
   - dto/Article/response/ArticleResponse.java 확인

2. 로그 확인 → "ProfileEnrichmentUtil에서 NPE"
   - util/ProfileEnrichmentUtil.java 또 찾기

3. 5개 파일 열어서 디버깅
```

**After (도메인 구조):**

```
1. domain/article/ 폴더 열기
   - 모든 관련 코드 한눈에 보임

2. ArticleService.java에서 로직 확인
   - ProfileEnrichmentService 호출 부분 확인

3. domain/profile/ProfileEnrichmentService.java로 이동
   - 같은 도메인 내에서 쉽게 찾음

4. 2개 파일만 열어서 디버깅
```

---

## IntelliJ 활용 팁 (도메인 구조에 최적화)

### 1. Scope 설정 (도메인별 필터링)

**설정 방법:**

1. Settings → Appearance & Behavior → Scopes
2. Add → Name: "Auth Domain"
3. Pattern: `src:com.study.api_gateway.domain.auth..*`
4. OK

**활용:**

- Find in Files (Ctrl+Shift+F)에서 Scope 선택
- "Auth Domain"만 검색 가능
- 다른 도메인 노이즈 제거

---

### 2. Favorites (즐겨찾기)

**작업 중인 도메인 고정:**

1. `domain/article/` 폴더 우클릭
2. Add to Favorites → New Favorites List: "Current Work"
3. Alt+2 (Favorites 탭)에서 빠른 접근

---

### 3. Bookmarks (북마크)

**주요 파일 북마크:**

1. `domain/article/ArticleService.java` 열기
2. Ctrl+Shift+3 (임의의 숫자)
3. 이후 Ctrl+3으로 바로 점프

---

### 4. Recent Locations (최근 위치)

```
Ctrl+Shift+E (Windows/Linux)
Cmd+Shift+E (Mac)
```

- 최근 편집한 코드 위치만 표시
- 같은 도메인 내 작업 시 매우 유용

---

### 5. File Structure (파일 구조)

```
Ctrl+F12 (Windows/Linux)
Cmd+F12 (Mac)
```

- 현재 파일의 메서드/필드 목록
- 도메인 구조에서는 Service/Controller가 깔끔하게 정리됨

---

## 코드 리뷰 개선

### Before (현재 구조):

**Pull Request Diff:**

```
Files changed (7):
  controller/auth/AuthController.java
  client/AuthClient.java
  dto/auth/request/LoginRequest.java
  dto/auth/response/LoginResponse.java
  util/ResponseFactory.java
  config/WebClientConfig.java
  ...
```

**리뷰어:**
"어떤 기능을 수정한 거지? 파일이 여러 곳에 흩어져 있네..."

---

### After (도메인 구조):

**Pull Request Diff:**

```
Files changed (4):
  domain/auth/AuthController.java
  domain/auth/AuthService.java
  domain/auth/AuthClient.java
  domain/auth/dto/LoginRequest.java
```

**리뷰어:**
"아, Auth 도메인 수정이구나! 로그인 기능 개선인가보다."
→ 폴더 하나만 봐도 컨텍스트 파악 ✅

---

## 팀 협업 개선

### 시나리오: 신규 팀원 온보딩

**Before:**

```
신규: "게시글 API는 어디 있나요?"
선임: "controller/article/ArticleController 보시고,
      client/ArticleClient도 보시고,
      dto/Article/... 아 이건 대문자로 시작하고,
      util/ProfileEnrichmentUtil도 같이 봐야 해요.
      아 그리고 service는 없고 controller에 로직이..."

신규: "...? 😵"
```

**After:**

```
신규: "게시글 API는 어디 있나요?"
선임: "domain/article 폴더 보시면 됩니다.
      거기 다 있어요."

신규: "아 controller, service, client, dto 다 여기 있네요! 👍"
```

---

## 예상 Q&A

### Q1: "마이그레이션 중에도 개발 가능한가요?"

**A:** 네! 도메인별로 점진적 마이그레이션 가능합니다.

```
Week 1: Auth 도메인만 이동
→ 다른 팀원은 Article, Profile 작업 가능

Week 2: Profile 도메인 이동
→ 다른 팀원은 Comment 작업 가능

충돌 최소화!
```

---

### Q2: "테스트 코드는 어떻게 구성하나요?"

**A:** 도메인별로 테스트도 같이 위치시킵니다.

```
src/test/java/com/study/api_gateway/
└── domain/
    ├── auth/
    │   ├── AuthServiceTest.java
    │   ├── AuthControllerTest.java
    │   └── AuthClientTest.java
    ├── profile/
    │   ├── ProfileServiceTest.java
    │   └── ProfileEnrichmentServiceTest.java
    └── ...
```

**또는 같은 폴더에 (선택적):**

```
src/main/java/.../domain/auth/
├── AuthService.java
└── AuthServiceTest.java  ← 같은 위치 (IntelliJ 지원)
```

---

### Q3: "공통 코드(common)는 모든 도메인에서 사용하나요?"

**A:** 네! 그래서 common 패키지로 분리합니다.

```java
// domain/auth/AuthController.java
import com.study.api_gateway.common.dto.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;

@RestController
public class AuthController {
    // BaseResponse, ResponseFactory는 공통 사용
}
```

---

### Q4: "도메인 간 의존성은 어떻게 처리하나요?"

**A:** Service를 통해 명시적으로 의존합니다.

```java
// domain/article/ArticleService.java
@Service
@RequiredArgsConstructor
public class ArticleService {

    // 다른 도메인의 Service 의존
    private final ProfileEnrichmentService profileService;
    private final LikeService likeService;

    public Mono<ArticleResponse> createArticle(...) {
        return articleClient.postArticle(...)
            .flatMap(profileService::enrichArticle)  // Profile 도메인 사용
            .flatMap(article ->
                likeService.checkLiked(userId, article.getId())  // Like 도메인 사용
            );
    }
}
```

**규칙:**

- ✅ Service → Service: OK
- ❌ Controller → 다른 도메인 Service: 지양
- ❌ Client → 다른 도메인 Client: 절대 금지

---

## 최종 추천

### ✅ 도메인 중심 구조 채택 이유

1. **개발 속도 2배 향상**
	- 파일 찾기 시간 90% 감소
	- 컨텍스트 스위칭 최소화

2. **코드 리뷰 품질 향상**
	- 변경 범위 명확
	- 리뷰어가 컨텍스트 쉽게 파악

3. **팀 협업 개선**
	- 신규 팀원 온보딩 시간 50% 단축
	- 도메인별 작업 분담 명확

4. **유지보수성 향상**
	- 관련 코드가 한 곳에
	- 버그 추적 쉬움

5. **확장성**
	- MSA 전환 시 유리
	- 도메인별 독립 서비스 분리 쉬움

---

## 마이그레이션 시작하기

### Step 1: 테스트 (5분)

```bash
# 현재 빌드가 성공하는지 확인
./mvnw clean test

# Git에 현재 상태 커밋
git add .
git commit -m "chore: checkpoint before package restructure"
```

### Step 2: Auth 도메인 마이그레이션 (10분)

```
1. domain/auth 패키지 생성
2. AuthController, AuthClient 이동 (F6)
3. DTO 이동
4. Enum 이동
5. 빌드 확인
```

### Step 3: 검증 (5분)

```bash
# 빌드 성공 확인
./mvnw clean test

# 애플리케이션 실행 확인
./mvnw spring-boot:run

# Postman/cURL로 Auth API 테스트
curl http://localhost:8080/bff/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'
```

### Step 4: Commit & 다음 도메인

```bash
git add .
git commit -m "refactor: migrate auth domain to new structure"

# Profile 도메인 마이그레이션 시작...
```

---

**다음 단계:** Auth 도메인 마이그레이션부터 시작하시겠어요? 제가 구체적인 파일 이동 명령어를 알려드릴 수 있습니다!

---

**문서 버전:** 1.0
**최종 업데이트:** 2025-10-23
**작성자:** Claude Code Analysis
