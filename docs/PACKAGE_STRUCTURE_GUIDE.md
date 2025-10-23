# 패키지 구조 개선 가이드

> API Gateway의 패키지 구조 분석 및 개선 방안

작성일: 2025-10-23

---

## 목차

1. [현재 패키지 구조 분석](#현재-패키지-구조-분석)
2. [문제점 분석](#문제점-분석)
3. [권장 패키지 구조 (3가지 옵션)](#권장-패키지-구조-3가지-옵션)
4. [마이그레이션 가이드](#마이그레이션-가이드)
5. [IntelliJ IDEA 활용 팁](#intellij-idea-활용-팁)

---

## 현재 패키지 구조 분석

### 현재 구조 (As-Is)

```
com.study.api_gateway
├── ApiGatewayApplication.java
├── client/                           # 12개 클라이언트 (평면 구조)
│   ├── AuthClient.java
│   ├── ProfileClient.java
│   ├── ArticleClient.java
│   ├── CommentClient.java
│   ├── ImageClient.java
│   ├── GaechuClient.java
│   ├── ActivityClient.java
│   ├── InquiryClient.java
│   ├── ReportClient.java
│   ├── FaqClient.java
│   ├── NoticeClient.java
│   └── EventClient.java
│
├── config/                           # 설정 클래스
│   ├── WebClientConfig.java
│   ├── WebFluxCorsConfig.java
│   ├── RedisConfig.java
│   ├── OpenApiConfig.java
│   ├── CustomConfig.java
│   └── GlobalExceptionHandler.java
│
├── controller/                       # 도메인별 하위 패키지
│   ├── auth/
│   │   └── AuthController.java
│   ├── profile/
│   │   └── ProfileController.java
│   ├── article/
│   │   ├── ArticleController.java
│   │   ├── NoticeController.java
│   │   └── EventController.java
│   ├── comment/
│   │   └── CommentController.java
│   ├── gaechu/
│   │   └── GaechuController.java
│   ├── activity/
│   │   └── FeedController.java
│   ├── support/
│   │   ├── InquiryController.java
│   │   └── ReportController.java
│   └── enums/
│       └── EnumsController.java
│
├── dto/                              # 도메인별 깊은 계층 구조
│   ├── BaseResponse.java
│   ├── auth/
│   │   ├── enums/
│   │   │   ├── Provider.java
│   │   │   ├── Role.java
│   │   │   └── Status.java
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   ├── TokenRefreshRequest.java
│   │   │   ├── PasswordChangeRequest.java
│   │   │   ├── ConsentRequest.java
│   │   │   └── SuspendRequest.java
│   │   └── response/
│   │       ├── LoginResponse.java
│   │       ├── SimpleAuthResponse.java
│   │       └── ConsentsTable.java
│   ├── profile/
│   │   ├── ProfileSearchCriteria.java
│   │   ├── enums/
│   │   │   └── City.java
│   │   ├── request/
│   │   │   ├── ProfileUpdateRequest.java
│   │   │   └── HistoryUpdateRequest.java
│   │   └── response/
│   │       ├── UserResponse.java
│   │       ├── UserPageResponse.java
│   │       └── BatchUserSummaryResponse.java
│   ├── Article/                      # ❌ 대문자 시작 (불일치)
│   │   ├── request/
│   │   │   └── EventArticleCreateRequest.java
│   │   └── response/
│   │       ├── ArticleResponse.java
│   │       ├── ArticleCursorPageResponse.java
│   │       └── EventArticleResponse.java
│   ├── comment/
│   │   └── request/
│   │       ├── RootCommentCreateRequest.java
│   │       ├── ReplyCreateRequest.java
│   │       ├── CombinedCommentCreateRequest.java
│   │       └── CommentUpdateRequest.java
│   ├── gaechu/
│   │   ├── LikeCountResponse.java
│   │   └── LikeDetailResponse.java
│   ├── activity/
│   │   ├── request/
│   │   │   └── FeedTotalsRequest.java
│   │   └── response/
│   │       ├── FeedPageResponse.java
│   │       └── FeedTotalsResponse.java
│   └── support/
│       ├── faq/
│       │   ├── FaqCategory.java
│       │   └── response/
│       │       └── FaqResponse.java
│       ├── inquiry/
│       │   ├── InquiryCategory.java
│       │   ├── InquiryStatus.java      # ✅ 올바른 위치
│       │   ├── request/
│       │   │   ├── InquiryCreateRequest.java
│       │   │   └── AnswerCreateRequest.java
│       │   └── response/
│       │       ├── InquiryResponse.java
│       │       └── AnswerResponse.java
│       └── report/
│           ├── InquiryStatus.java      # ❌ 중복! (삭제 필요)
│           ├── ReportStatus.java
│           ├── ReferenceType.java
│           ├── ReportSortType.java
│           ├── SortDirection.java
│           ├── request/
│           │   ├── ReportCreateRequest.java
│           │   └── ReportWithdrawRequest.java
│           └── response/
│               ├── ReportResponse.java
│               └── ReportPageResponse.java
│
├── service/                          # 거의 비어있음
│   └── ImageConfirmService.java
│
└── util/
    ├── ProfileEnrichmentUtil.java
    ├── ResponseFactory.java
    ├── RequestPathHelper.java
    └── cache/
        ├── ProfileCache.java
        ├── RedisProfileCache.java
        └── NoopProfileCache.java
```

---

## 문제점 분석

### 1. ❌ 일관성 없는 구조

- **Controller**: 도메인별 하위 패키지 (O)
- **Client**: 평면 구조 (X)
- **DTO**: 도메인별 + request/response 분리 (O)
- **Service**: 거의 없음 (X)

### 2. ❌ 탐색의 어려움

```
"ArticleController를 찾고 싶다"
→ controller/article/ArticleController.java (OK)

"ArticleClient를 찾고 싶다"
→ client/ArticleClient.java (찾기 쉬움)
→ BUT: Auth, Profile, Comment 등 12개가 한 폴더에 섞여있음

"ArticleRequest를 찾고 싶다"
→ dto/Article/request/EventArticleCreateRequest.java
→ dto/Article이 대문자로 시작 (다른 패키지와 불일치)
```

### 3. ❌ 비즈니스 로직의 분산

- Controller에 비즈니스 로직 집중
- Service 레이어 부재
- Util에 중요한 로직 (ProfileEnrichmentUtil)

### 4. ❌ 계층이 너무 깊음

```
dto/support/inquiry/request/InquiryCreateRequest.java
└─ 5단계 깊이 (찾기 어려움)
```

### 5. ❌ 공통 코드 위치 불명확

- `BaseResponse.java`: dto/ 루트
- `ResponseFactory.java`: util/
- Enum 클래스들: dto/{domain}/enums/

---

## 권장 패키지 구조 (3가지 옵션)

### 옵션 1: 도메인 기반 모듈 구조 (추천 ⭐⭐⭐)

**특징:** 도메인별로 모든 관련 클래스를 한 곳에 모음

```
com.study.api_gateway
├── ApiGatewayApplication.java
│
├── domain/                          # 도메인별 패키지
│   ├── auth/
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── client/
│   │   │   └── AuthClient.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── SimpleAuthResponse.java
│   │   │   └── ConsentsTable.java
│   │   ├── service/                 # 신규 추가
│   │   │   └── AuthService.java
│   │   └── enums/
│   │       ├── Provider.java
│   │       ├── Role.java
│   │       └── Status.java
│   │
│   ├── profile/
│   │   ├── controller/
│   │   │   └── ProfileController.java
│   │   ├── client/
│   │   │   └── ProfileClient.java
│   │   ├── dto/
│   │   │   ├── ProfileUpdateRequest.java
│   │   │   ├── UserResponse.java
│   │   │   ├── UserPageResponse.java
│   │   │   └── BatchUserSummaryResponse.java
│   │   ├── service/
│   │   │   ├── ProfileService.java
│   │   │   └── ProfileEnrichmentService.java  # util에서 이동
│   │   ├── enums/
│   │   │   └── City.java
│   │   └── cache/
│   │       ├── ProfileCache.java
│   │       ├── RedisProfileCache.java
│   │       └── NoopProfileCache.java
│   │
│   ├── article/
│   │   ├── controller/
│   │   │   ├── ArticleController.java
│   │   │   ├── NoticeController.java
│   │   │   └── EventController.java
│   │   ├── client/
│   │   │   ├── ArticleClient.java
│   │   │   ├── NoticeClient.java
│   │   │   └── EventClient.java
│   │   ├── dto/
│   │   │   ├── ArticleCreateRequest.java
│   │   │   ├── EventArticleCreateRequest.java
│   │   │   ├── ArticleResponse.java
│   │   │   ├── EventArticleResponse.java
│   │   │   └── ArticleCursorPageResponse.java
│   │   └── service/
│   │       └── ArticleService.java
│   │
│   ├── comment/
│   │   ├── controller/
│   │   │   └── CommentController.java
│   │   ├── client/
│   │   │   └── CommentClient.java
│   │   ├── dto/
│   │   │   ├── RootCommentCreateRequest.java
│   │   │   ├── ReplyCreateRequest.java
│   │   │   ├── CombinedCommentCreateRequest.java
│   │   │   └── CommentUpdateRequest.java
│   │   └── service/
│   │       └── CommentService.java
│   │
│   ├── like/                        # gaechu → like로 명확하게
│   │   ├── controller/
│   │   │   └── LikeController.java
│   │   ├── client/
│   │   │   └── LikeClient.java
│   │   ├── dto/
│   │   │   ├── LikeCountResponse.java
│   │   │   └── LikeDetailResponse.java
│   │   └── service/
│   │       └── LikeService.java
│   │
│   ├── feed/                        # activity → feed로 변경
│   │   ├── controller/
│   │   │   └── FeedController.java
│   │   ├── client/
│   │   │   └── FeedClient.java
│   │   ├── dto/
│   │   │   ├── FeedTotalsRequest.java
│   │   │   ├── FeedPageResponse.java
│   │   │   └── FeedTotalsResponse.java
│   │   └── service/
│   │       └── FeedService.java
│   │
│   ├── image/
│   │   ├── client/
│   │   │   └── ImageClient.java
│   │   └── service/
│   │       └── ImageConfirmService.java
│   │
│   └── support/
│       ├── controller/
│       │   ├── InquiryController.java
│       │   └── ReportController.java
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
│       ├── service/
│       │   ├── InquiryService.java
│       │   └── ReportService.java
│       └── enums/
│           ├── InquiryStatus.java
│           ├── InquiryCategory.java
│           ├── ReportStatus.java
│           ├── ReferenceType.java
│           ├── ReportSortType.java
│           ├── SortDirection.java
│           └── FaqCategory.java
│
├── common/                          # 공통 코드
│   ├── dto/
│   │   └── BaseResponse.java
│   ├── factory/
│   │   └── ResponseFactory.java
│   ├── filter/
│   │   └── RequestLoggingFilter.java  # 신규 추가 예정
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   └── util/
│       └── RequestPathHelper.java
│
├── config/                          # 설정 클래스
│   ├── WebClientConfig.java
│   ├── WebFluxCorsConfig.java
│   ├── RedisConfig.java
│   ├── OpenApiConfig.java
│   └── CustomConfig.java
│
└── controller/                      # 공통 컨트롤러
    ├── HealthCheckController.java
    └── EnumsController.java
```

**장점:**

- ✅ 도메인별로 모든 것이 한 곳에 (Controller, Service, Client, DTO)
- ✅ 탐색 용이: "Article 관련 코드 찾기" → `domain/article/` 한 곳만 보면 됨
- ✅ MSA 전환 시 유리: 각 도메인을 독립 서비스로 분리 쉬움
- ✅ 팀 분업 용이: 도메인별로 작업 영역 명확

**단점:**

- ⚠️ 마이그레이션 범위가 큼 (전체 패키지 재구성)

---

### 옵션 2: 레이어 기반 구조 (현재 구조 개선)

**특징:** 현재 구조 유지하되 일관성 개선

```
com.study.api_gateway
├── ApiGatewayApplication.java
│
├── controller/                      # 도메인별 하위 패키지 유지
│   ├── auth/
│   │   └── AuthController.java
│   ├── profile/
│   │   └── ProfileController.java
│   ├── article/
│   │   ├── ArticleController.java
│   │   ├── NoticeController.java
│   │   └── EventController.java
│   ├── comment/
│   │   └── CommentController.java
│   ├── like/                        # gaechu → like
│   │   └── LikeController.java
│   ├── feed/                        # activity → feed
│   │   └── FeedController.java
│   ├── support/
│   │   ├── InquiryController.java
│   │   └── ReportController.java
│   ├── common/
│   │   ├── HealthCheckController.java
│   │   └── EnumsController.java
│   └── package-info.java            # 패키지 설명
│
├── service/                         # 도메인별 서비스 (신규)
│   ├── auth/
│   │   └── AuthService.java
│   ├── profile/
│   │   ├── ProfileService.java
│   │   └── ProfileEnrichmentService.java
│   ├── article/
│   │   └── ArticleService.java
│   ├── comment/
│   │   └── CommentService.java
│   ├── like/
│   │   └── LikeService.java
│   ├── feed/
│   │   └── FeedService.java
│   ├── image/
│   │   └── ImageConfirmService.java
│   └── support/
│       ├── InquiryService.java
│       └── ReportService.java
│
├── client/                          # 도메인별 클라이언트
│   ├── auth/
│   │   └── AuthClient.java
│   ├── profile/
│   │   └── ProfileClient.java
│   ├── article/
│   │   ├── ArticleClient.java
│   │   ├── NoticeClient.java
│   │   └── EventClient.java
│   ├── comment/
│   │   └── CommentClient.java
│   ├── like/
│   │   └── LikeClient.java
│   ├── feed/
│   │   └── FeedClient.java
│   ├── image/
│   │   └── ImageClient.java
│   └── support/
│       ├── InquiryClient.java
│       ├── ReportClient.java
│       └── FaqClient.java
│
├── dto/                             # 계층 축소
│   ├── common/
│   │   └── BaseResponse.java
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   ├── SignupRequest.java
│   │   ├── TokenRefreshRequest.java
│   │   ├── PasswordChangeRequest.java
│   │   ├── LoginResponse.java
│   │   ├── SimpleAuthResponse.java
│   │   └── ConsentsTable.java
│   ├── profile/
│   │   ├── ProfileUpdateRequest.java
│   │   ├── HistoryUpdateRequest.java
│   │   ├── UserResponse.java
│   │   ├── UserPageResponse.java
│   │   ├── BatchUserSummaryResponse.java
│   │   └── ProfileSearchCriteria.java
│   ├── article/                     # Article → article (소문자)
│   │   ├── ArticleCreateRequest.java
│   │   ├── EventArticleCreateRequest.java
│   │   ├── ArticleResponse.java
│   │   ├── EventArticleResponse.java
│   │   └── ArticleCursorPageResponse.java
│   ├── comment/
│   │   ├── RootCommentCreateRequest.java
│   │   ├── ReplyCreateRequest.java
│   │   ├── CombinedCommentCreateRequest.java
│   │   └── CommentUpdateRequest.java
│   ├── like/
│   │   ├── LikeCountResponse.java
│   │   └── LikeDetailResponse.java
│   ├── feed/
│   │   ├── FeedTotalsRequest.java
│   │   ├── FeedPageResponse.java
│   │   └── FeedTotalsResponse.java
│   └── support/
│       ├── inquiry/
│       │   ├── InquiryCreateRequest.java
│       │   ├── InquiryResponse.java
│       │   ├── AnswerCreateRequest.java
│       │   └── AnswerResponse.java
│       ├── report/
│       │   ├── ReportCreateRequest.java
│       │   ├── ReportWithdrawRequest.java
│       │   ├── ReportResponse.java
│       │   └── ReportPageResponse.java
│       └── faq/
│           └── FaqResponse.java
│
├── enums/                           # 모든 Enum 한 곳에
│   ├── auth/
│   │   ├── Provider.java
│   │   ├── Role.java
│   │   └── Status.java
│   ├── profile/
│   │   └── City.java
│   └── support/
│       ├── InquiryStatus.java
│       ├── InquiryCategory.java
│       ├── ReportStatus.java
│       ├── ReferenceType.java
│       ├── ReportSortType.java
│       ├── SortDirection.java
│       └── FaqCategory.java
│
├── cache/                           # 캐시 구현
│   ├── ProfileCache.java
│   ├── RedisProfileCache.java
│   └── NoopProfileCache.java
│
├── util/                            # 유틸리티
│   ├── ResponseFactory.java
│   └── RequestPathHelper.java
│
├── exception/                       # 예외 처리
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   └── ErrorCode.java
│
└── config/                          # 설정
    ├── WebClientConfig.java
    ├── WebFluxCorsConfig.java
    ├── RedisConfig.java
    ├── OpenApiConfig.java
    └── CustomConfig.java
```

**장점:**

- ✅ 현재 구조와 유사하여 마이그레이션 쉬움
- ✅ 레이어별 책임 명확 (Controller, Service, Client, DTO 분리)
- ✅ Spring 공식 문서 구조와 일치

**단점:**

- ⚠️ 도메인 관련 코드가 여러 패키지에 분산
- ⚠️ "Article 관련 모든 코드 보기" → 4개 패키지 탐색 필요

---

### 옵션 3: 하이브리드 구조 (도메인 + 레이어)

**특징:** 주요 도메인은 독립 패키지, 공통/설정은 레이어 분리

```
com.study.api_gateway
├── ApiGatewayApplication.java
│
├── feature/                         # 주요 기능별 패키지
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── AuthClient.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── SignupRequest.java
│   │       └── LoginResponse.java
│   │
│   ├── profile/
│   │   ├── ProfileController.java
│   │   ├── ProfileService.java
│   │   ├── ProfileEnrichmentService.java
│   │   ├── ProfileClient.java
│   │   ├── dto/
│   │   │   ├── ProfileUpdateRequest.java
│   │   │   └── UserResponse.java
│   │   └── cache/
│   │       ├── ProfileCache.java
│   │       └── RedisProfileCache.java
│   │
│   ├── article/
│   │   ├── ArticleController.java
│   │   ├── NoticeController.java
│   │   ├── EventController.java
│   │   ├── ArticleService.java
│   │   ├── ArticleClient.java
│   │   └── dto/
│   │       ├── ArticleRequest.java
│   │       └── ArticleResponse.java
│   │
│   └── ...
│
├── common/
│   ├── dto/
│   │   └── BaseResponse.java
│   ├── factory/
│   │   └── ResponseFactory.java
│   ├── filter/
│   └── util/
│
├── config/
└── exception/
```

**장점:**

- ✅ 도메인별 응집도 높음
- ✅ 공통 코드는 별도 관리
- ✅ 유연한 확장 가능

**단점:**

- ⚠️ 구조가 다소 복잡
- ⚠️ 팀 내 컨벤션 정립 필요

---

## 추천 사항

### 🥇 1순위: 옵션 2 (레이어 기반 개선)

**이유:**

- 현재 구조와 유사하여 점진적 개선 가능
- Spring Boot 표준 구조
- 팀원들이 이해하기 쉬움
- 마이그레이션 비용 최소

**단계적 적용:**

1. Week 1: Client 패키지만 도메인별 분류
2. Week 2: DTO의 `Article` → `article` 소문자 변경
3. Week 3: Service 레이어 추가
4. Week 4: Enum 통합

---

### 🥈 2순위: 옵션 1 (도메인 모듈)

**이유:**

- MSA 전환 계획이 있다면 최적
- 도메인별 독립성 높음
- 대규모 프로젝트에 적합

**적용 시기:**

- 팀 규모 확대 시
- 마이크로서비스 분리 계획 시
- 리팩토링 시간 확보 시

---

## 마이그레이션 가이드

### 1단계: Client 패키지 구조화 (가장 쉬움)

**현재:**

```
client/
├── AuthClient.java
├── ProfileClient.java
├── ArticleClient.java
└── ... (12개 파일)
```

**개선:**

```
client/
├── auth/
│   └── AuthClient.java
├── profile/
│   └── ProfileClient.java
├── article/
│   ├── ArticleClient.java
│   ├── NoticeClient.java
│   └── EventClient.java
└── ...
```

**IntelliJ 작업 방법:**

1. `client` 패키지 우클릭 → New → Package
2. `auth` 입력 → Enter
3. `AuthClient.java` 드래그 앤 드롭으로 `client/auth/`로 이동
4. IntelliJ가 자동으로 import 수정

**예상 소요 시간:** 10분

---

### 2단계: DTO 패키지 이름 통일

**문제:**

```
dto/Article/  ← 대문자 (X)
dto/auth/     ← 소문자 (O)
```

**해결:**

1. `dto/Article` 우클릭 → Refactor → Rename
2. `article` 입력 (소문자)
3. "Search in comments and strings" 체크
4. Refactor 버튼 클릭
5. IntelliJ가 모든 import 자동 수정

**예상 소요 시간:** 2분

---

### 3단계: request/response 계층 축소 (선택적)

**현재:**

```
dto/auth/request/LoginRequest.java
dto/auth/response/LoginResponse.java
```

**개선 (선택적):**

```
dto/auth/LoginRequest.java
dto/auth/LoginResponse.java
```

**장점:**

- 탐색 단계 1단계 감소
- Request/Response는 이름으로 구분 가능

**단점:**

- 파일이 많을 경우 한 폴더에 섞임

**추천:**

- DTO가 5개 이하인 도메인: request/response 폴더 제거
- DTO가 많은 도메인 (auth, article): 폴더 유지

---

### 4단계: Enum 패키지 정리

**옵션 A: 현재 위치 유지 (도메인별 enum)**

```
dto/auth/enums/Provider.java
dto/profile/enums/City.java
```

**옵션 B: 최상위 enums 패키지로 통합**

```
enums/auth/Provider.java
enums/profile/City.java
```

**추천:** 옵션 B (Enum은 DTO가 아니므로 분리)

**작업 방법:**

1. `com.study.api_gateway` 하위에 `enums` 패키지 생성
2. `enums/auth`, `enums/profile` 하위 패키지 생성
3. 각 Enum 파일을 드래그 앤 드롭으로 이동

**예상 소요 시간:** 15분

---

### 5단계: Service 레이어 추가 (점진적)

**우선순위:**

1. ArticleService (가장 복잡한 로직)
2. ProfileService
3. AuthService
4. 나머지...

**예시: ArticleService 추가**

```java
// service/article/ArticleService.java
package com.study.api_gateway.service.article;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleClient articleClient;
    private final ProfileEnrichmentService profileEnrichmentService;
    private final LikeClient likeClient;

    public Mono<ArticleResponse> createArticle(
        String userId,
        ArticleCreateRequest request
    ) {
        return articleClient.postArticle(request)
            .flatMap(article -> enrichWithUserData(article, userId));
    }

    private Mono<ArticleResponse> enrichWithUserData(
        ArticleResponse article,
        String userId
    ) {
        return profileEnrichmentService.enrichArticle(article)
            .flatMap(enriched ->
                likeClient.getLikedIdsByUser(userId, ReferenceType.ARTICLE)
                    .map(likedIds -> {
                        enriched.setLiked(likedIds.contains(article.getId()));
                        return enriched;
                    })
            );
    }
}
```

**Controller 수정:**

```java
// Before
@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleClient articleClient;
    private final ProfileEnrichmentUtil profileEnrichmentUtil;

    @PostMapping
    public Mono<BaseResponse<ArticleResponse>> create(...) {
        return articleClient.postArticle(...)
            .flatMap(profileEnrichmentUtil::enrichArticle)
            .map(ResponseFactory::success);
    }
}

// After
@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;  // Service만 주입

    @PostMapping
    public Mono<BaseResponse<ArticleResponse>> create(
        @RequestBody ArticleCreateRequest request,
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

---

### 6단계: 중복 파일 제거

**즉시 삭제:**

```bash
# InquiryStatus 중복 제거
rm src/main/java/com/study/api_gateway/dto/support/report/InquiryStatus.java
```

**또는 IntelliJ에서:**

1. `dto/support/report/InquiryStatus.java` 우클릭
2. Delete
3. Safe delete 체크 (참조 확인)

---

## IntelliJ IDEA 활용 팁

### 1. 빠른 클래스 찾기

```
Ctrl + N (Windows/Linux)
Cmd + O (Mac)
→ 클래스 이름 입력 (예: ArticleController)
```

### 2. 파일 이름으로 찾기

```
Ctrl + Shift + N (Windows/Linux)
Cmd + Shift + O (Mac)
→ 파일 이름 입력 (예: ArticleRequest.java)
```

### 3. 전체 검색 (코드 + 파일명)

```
Shift + Shift (모든 OS)
→ 검색어 입력
```

### 4. 패키지 구조 보기

```
Alt + 1 (Windows/Linux)
Cmd + 1 (Mac)
→ Project 탭 활성화
→ 톱니바퀴 아이콘 → Tree Appearance → Flatten Packages 해제
```

### 5. 최근 파일 보기

```
Ctrl + E (Windows/Linux)
Cmd + E (Mac)
→ 최근 열었던 파일 목록
```

### 6. 같은 타입 파일 그룹으로 보기

```
Project 탭에서 톱니바퀴 아이콘
→ Group by Type
→ Controller, Service, DTO별로 그룹화
```

### 7. 패키지별 색상 구분 (선택적)

```
Settings → Appearance & Behavior → File Colors
→ Add
→ Scope: "client/*" → Color: 파랑
→ Scope: "controller/*" → Color: 초록
→ Scope: "dto/*" → Color: 노랑
```

---

## 현실적인 마이그레이션 계획

### Week 1: 즉시 개선 (영향 최소)

- [ ] DTO `Article` → `article` 소문자 변경
- [ ] 중복 `InquiryStatus` 삭제
- [ ] Client 패키지를 도메인별 하위 패키지로 분류

**예상 소요 시간:** 30분
**영향 범위:** Import 문만 자동 수정됨

---

### Week 2-3: 구조 개선 (중간 영향)

- [ ] Service 레이어 추가 (ArticleService, ProfileService 우선)
- [ ] ProfileEnrichmentUtil → ProfileEnrichmentService로 이동
- [ ] Enum 패키지 통합 (선택적)

**예상 소요 시간:** 4-6시간
**영향 범위:** Controller 일부 수정 필요

---

### Week 4: 정리 및 문서화

- [ ] 주석 처리된 코드 제거
- [ ] 패키지별 package-info.java 추가 (설명)
- [ ] 팀 컨벤션 문서 작성

**예상 소요 시간:** 2시간

---

## 패키지별 설명 (package-info.java)

### 예시: controller/package-info.java

```java
/**
 * REST API 컨트롤러 패키지
 *
 * <p>클라이언트 요청을 받아 Service를 호출하고 응답을 반환합니다.
 * 모든 컨트롤러는 BaseResponse로 응답을 래핑합니다.
 *
 * <p>패키지 구조:
 * <ul>
 *   <li>auth/ - 인증/인가 관련 컨트롤러</li>
 *   <li>profile/ - 프로필 관련 컨트롤러</li>
 *   <li>article/ - 게시글 관련 컨트롤러</li>
 *   <li>...</li>
 * </ul>
 *
 * @see com.study.api_gateway.dto.BaseResponse
 * @see com.study.api_gateway.util.ResponseFactory
 */
package com.study.api_gateway.controller;
```

---

## 체크리스트

### 즉시 개선 (비용 낮음, 효과 높음)

- [ ] Client 패키지 도메인별 분류
- [ ] DTO `Article` → `article` 변경
- [ ] 중복 `InquiryStatus` 삭제
- [ ] IntelliJ 검색 단축키 팀원 공유

### 단기 개선 (1-2주)

- [ ] Service 레이어 추가 (주요 도메인)
- [ ] Enum 패키지 정리
- [ ] 주석 코드 제거
- [ ] package-info.java 추가

### 중기 개선 (1개월)

- [ ] 전체 패키지 구조 최종 결정 (옵션 1/2/3 선택)
- [ ] 팀 컨벤션 문서화
- [ ] 코드 리뷰 체크리스트에 패키지 규칙 추가

---

## 요약

### 현재 문제점

1. Client가 평면 구조 (12개 한 폴더)
2. DTO 계층 너무 깊음 (5단계)
3. Service 레이어 부재
4. 일관성 부족 (Article vs article)

### 추천 구조: 옵션 2 (레이어 기반)

```
controller/{domain}/
service/{domain}/
client/{domain}/
dto/{domain}/
enums/{domain}/
```

### 우선 작업

1. ✅ Client 패키지 도메인별 분류 (10분)
2. ✅ Article → article 변경 (2분)
3. ✅ 중복 파일 삭제 (1분)
4. ⏳ Service 레이어 추가 (점진적)

### IntelliJ 활용

- `Shift + Shift`: 전체 검색
- `Ctrl/Cmd + N`: 클래스 찾기
- `Ctrl/Cmd + E`: 최근 파일

---

**문서 버전:** 1.0
**최종 업데이트:** 2025-10-23
**작성자:** Claude Code Analysis
