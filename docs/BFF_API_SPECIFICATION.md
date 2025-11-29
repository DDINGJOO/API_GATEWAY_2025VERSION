# BFF (Backend For Frontend) API 스펙 정의서

## 목차

1. [BFF 패턴 개요](#bff-패턴-개요)
2. [아키텍처 설계 원칙](#아키텍처-설계-원칙)
3. [데이터 조합 패턴](#데이터-조합-패턴)
4. [API 카테고리별 스펙](#api-카테고리별-스펙)
5. [공통 응답 구조](#공통-응답-구조)
6. [성능 최적화 전략](#성능-최적화-전략)

---

## BFF 패턴 개요

### BFF의 역할

API Gateway는 **Backend For Frontend** 패턴을 따라 프론트엔드 클라이언트에 최적화된 API를 제공합니다.

#### 핵심 특징

1. **다중 서비스 조합**: 여러 백엔드 도메인 서비스를 호출하여 데이터를 조합
2. **프론트엔드 최적화**: 클라이언트가 필요로 하는 정확한 형태로 데이터 변환
3. **N+1 문제 방지**: 배치 API 호출로 성능 최적화
4. **데이터 보강(Enrichment)**: 프로필, 카운트 등 추가 정보 주입
5. **필드 제어**: 불필요한 필드 제거, 필요한 필드 추가

### BFF vs 단순 프록시

| 패턴               | 특징                    | 예시 컨트롤러                                                    |
|------------------|-----------------------|------------------------------------------------------------|
| **BFF (복잡한 조합)** | 다중 서비스 호출, 데이터 변환, 보강 | `FeedController`, `ArticleController`, `ProfileController` |
| **단순 프록시**       | 1:1 매핑, 최소한의 변환       | `PlaceController`, `RoomReservationController`             |

---

## 아키텍처 설계 원칙

### 1. 반응형 프로그래밍 (Reactive)

```java
// Mono.zip을 사용한 병렬 호출 및 조합
return Mono.zip(
    articleClient.getArticle(articleId),           // 병렬 호출 1
    commentClient.getCommentsByArticle(articleId), // 병렬 호출 2
    likeClient.getLikeDetail(categoryId, articleId) // 병렬 호출 3
)
.flatMap(tuple3 -> {
    // 결과 조합 및 변환
});
```

### 2. 유틸리티 기반 데이터 보강

```java
// ProfileEnrichmentUtil: 프로필 정보 배치 조회 및 주입
// ArticleCountUtil: 좋아요/댓글 수 배치 조회 및 주입

return articleClient.getBulkArticles(articleIds)
    .flatMap(profileEnrichmentUtil::enrichArticleList)
    .flatMap(articles -> articleCountUtil.enrichWithCounts(articles, "ARTICLE"));
```

### 3. 캐시 우선 전략

```java
// ProfileCache: Redis 기반 프로필 캐시
// 1. 캐시에서 먼저 조회
// 2. 미스 발생 시 배치 API 호출
// 3. 조회 결과를 캐시에 저장 (fire-and-forget)
```

---

## 데이터 조합 패턴

### 패턴 1: 단순 조합 (Mono.zip)

**사용 사례**: 2-3개의 독립적인 서비스를 병렬 호출하여 결과를 조합

```java
@GetMapping("/{userId}")
public Mono<ResponseEntity<BaseResponse>> fetchProfile(@PathVariable String userId) {
    return Mono.zip(
        profileClient.fetchProfile(userId),        // 프로필 정보
        likeClient.getUserLikedCounts("PROFILE", userId) // 좋아요 수
    ).map(tuple2 -> responseFactory.ok(Map.of(
        "profile", tuple2.getT1(),
        "liked", tuple2.getT2()
    ), request));
}
```

**응답 구조**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "profile": { "userId": "user123", "nickname": "닉네임" },
    "liked": 15
  }
}
```

### 패턴 2: 다단계 보강 (Chained Enrichment)

**사용 사례**: 기본 데이터를 조회한 후, 순차적으로 추가 정보를 보강

```java
@GetMapping("/{category}")
public Mono<ResponseEntity<BaseResponse>> getFeedByCategory(...) {
    return activityClient.getFeedByCategory(category, viewerId, targetUserId, cursor, size, sort)
        .flatMap(this::enrichFeedResponse) // 1단계: Article 정보 조회 및 보강
        .map(enrichedResponse -> responseFactory.ok(enrichedResponse, req));
}

private Mono<EnrichedFeedPageResponse> enrichFeedResponse(FeedPageResponse feedResponse) {
    return articleClient.getBulkArticles(feedResponse.getArticleIds())          // 1. Article 배치 조회
        .flatMap(profileEnrichmentUtil::enrichArticleList)                      // 2. 프로필 정보 주입
        .flatMap(articles -> articleCountUtil.enrichWithCounts(articles, "ARTICLE")) // 3. 카운트 주입
        .map(enrichedArticles -> EnrichedFeedPageResponse.builder()
            .articles(enrichedArticles)
            .nextCursor(feedResponse.getNextCursor())
            .build());
}
```

**데이터 흐름**:

```
FeedPageResponse (articleIds)
    ↓ articleClient.getBulkArticles
ArticleSimpleResponse[] (title, content, writerId, ...)
    ↓ profileEnrichmentUtil.enrichArticleList
EnrichedArticleResponse[] (+ writerName, writerProfileImage)
    ↓ articleCountUtil.enrichWithCounts
EnrichedArticleResponse[] (+ likeCount, commentCount)
    ↓
EnrichedFeedPageResponse
```

### 패턴 3: 복잡한 구조 변환 (Map 기반 조합)

**사용 사례**: 여러 서비스 결과를 조합하고, 불필요한 필드 제거 및 추가 필드 생성

```java
@GetMapping("/{articleId}")
public Mono<ResponseEntity<BaseResponse>> getArticle(@PathVariable String articleId) {
    return Mono.zip(
        articleClient.getArticle(articleId),
        commentClient.getCommentsByArticle(articleId, 0, 10, "visibleCount"),
        likeClient.getLikeDetail(categoryId, articleId)
    )
    .flatMap(tuple3 -> {
        String currentUserId = resolveCurrentUserId(req);

        // likeDetail 구조 변환 (불필요한 필드 제거)
        LikeDetailResponse ld = tuple3.getT3();
        Map<String, Object> likeDetail = new LinkedHashMap<>();
        likeDetail.put("likeCount", ld.getLikeCount());
        likeDetail.put("isOwn", ld.getLikerIds().contains(currentUserId)); // 추가 필드 생성

        // comments 구조 변환 (referenceId, articleId 제거, isOwn 추가)
        List<Map<String, Object>> comments = tuple3.getT2().stream()
            .map(c -> sanitizeCommentMap(c, currentUserId))
            .toList();

        // article을 Map으로 변환하여 프로필 주입 준비
        Map<String, Object> articleMap = buildArticleMap(tuple3.getT1());

        return profileEnrichmentUtil.enrichArticleAndComments(articleMap, comments)
            .map(ac -> {
                Map<String, Object> data = new LinkedHashMap<>(ac);
                data.put("likeDetail", likeDetail);
                return responseFactory.ok(data, req);
            });
    });
}
```

**응답 구조**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "article": {
      "articleId": "42840044-0f3e-482c-b5d5-0883af43e63e",
      "title": "공연 함께 하실 분",
      "writerId": "user_123",
      "nickname": "작성자닉네임",           // ← ProfileEnrichmentUtil이 주입
      "profileImageUrl": "https://...",   // ← ProfileEnrichmentUtil이 주입
      "board": { "1": "공지사항" },
      "createdAt": "2025-10-11T17:52:27"
    },
    "comments": [
      {
        "commentId": "c1",
        "writerId": "user_456",
        "nickname": "댓글작성자",          // ← ProfileEnrichmentUtil이 주입
        "profileImageUrl": "https://...", // ← ProfileEnrichmentUtil이 주입
        "contents": "첫 댓글",
        "isOwn": false,                   // ← BFF에서 추가 (현재 사용자와 비교)
        "replies": []
        // referenceId, articleId 필드는 제거됨
      }
    ],
    "likeDetail": {
      "likeCount": 5,
      "isOwn": true                      // ← BFF에서 추가
      // referenceId, likerIds 필드는 제거됨
    }
  }
}
```

### 패턴 4: 병렬 처리 (Mono.when)

**사용 사례**: 여러 독립적인 작업을 병렬로 실행 (응답 조합 불필요)

```java
@PutMapping("/me")
public Mono<ResponseEntity<BaseResponse>> updateMyProfile(@RequestBody ProfileUpdateRequest req) {
    return profileClient.updateProfile(userId, req)
        .flatMap(success -> {
            // 캐시 무효화와 이미지 확정을 병렬로 실행
            Mono<Void> cacheEviction = profileCache.evict(userId);
            Mono<Void> imageConfirmation = imageConfirmService.confirmImage(userId, List.of(req.getProfileImageId()));

            return Mono.when(cacheEviction, imageConfirmation)
                .thenReturn(responseFactory.ok(true, request, HttpStatus.OK));
        });
}
```

---

## API 카테고리별 스펙

### 1. Activity & Feed API (`/bff/v1/activities/feed`)

#### 특징

- ActivityClient + ArticleClient + ProfileClient + LikeClient + CommentClient 조합
- 다단계 데이터 보강 (Article → Profile → Counts)
- 배치 조회로 N+1 문제 방지

#### 주요 엔드포인트

##### 1.1 피드 활동 총합 조회

```
POST /bff/v1/activities/feed
```

**Request Body**:

```json
{
  "targetUserId": "user123",
  "categories": ["article", "comment", "like"]
}
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "totals": {
      "article": 12,
      "comment": 34,
      "like": 5
    },
    "isOwner": false
  }
}
```

##### 1.2 카테고리별 피드 조회 (보강된 데이터)

```
GET /bff/v1/activities/feed/{category}?targetUserId=user123&size=20
```

**데이터 조합 과정**:

1. ActivityClient: `articleIds` 조회
2. ArticleClient: Article 상세 정보 배치 조회
3. ProfileEnrichmentUtil: 프로필 정보 주입 (캐시 우선)
4. ArticleCountUtil: 좋아요/댓글 수 주입

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "articles": [
      {
        "articleId": "a-100",
        "title": "제목",
        "content": "내용",
        "writerId": "user123",
        "writerName": "작성자닉네임",        // ← 프로필 보강
        "writerProfileImage": "https://...", // ← 프로필 보강
        "likeCount": 15,                     // ← 카운트 보강
        "commentCount": 8                    // ← 카운트 보강
      }
    ],
    "nextCursor": "a-98"
  }
}
```

### 2. Article API (`/bff/v1/communities/articles/regular`)

#### 특징

- ArticleClient + CommentClient + LikeClient + ProfileClient 조합
- 단건 조회 시 Mono.zip으로 병렬 호출
- 목록 조회 시 다단계 보강
- 불필요한 필드 제거 및 권한 필드 추가 (isOwn)

#### 주요 엔드포인트

##### 2.1 게시글 단건 조회 (댓글 포함)

```
GET /bff/v1/communities/articles/regular/{articleId}
```

**데이터 조합 과정**:

```java
Mono.zip(
    articleClient.getArticle(articleId),           // Article 정보
    commentClient.getCommentsByArticle(articleId), // 댓글 목록
    likeClient.getLikeDetail(categoryId, articleId) // 좋아요 상세
)
→ sanitizeCommentMap() // referenceId, articleId 제거 + isOwn 추가
→ profileEnrichmentUtil.enrichArticleAndComments() // 프로필 주입
```

**Response 특징**:

- `likeDetail`: `referenceId`, `likerIds` 제거, `isOwn` 추가
- `comments`: `referenceId`, `articleId` 제거, `isOwn` 추가, `nickname`, `profileImageUrl` 추가
- `article`: `nickname`, `profileImageUrl` 추가

##### 2.2 게시글 목록 조회

```
GET /bff/v1/communities/articles/regular?size=10&boardIds=1
```

**데이터 조합 과정**:

1. ArticleClient: 게시글 목록 조회 (커서 페이징)
2. ProfileEnrichmentUtil: 프로필 정보 배치 주입
3. LikeClient + CommentClient: 카운트 배치 조회 (Mono.zip)
4. Map 구조로 변환하여 카운트 주입

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "page": {
      "items": [
        {
          "articleId": "42840044-0f3e-482c-b5d5-0883af43e63e",
          "title": "공연 함께 하실 분",
          "writerId": "user_123",
          "writerName": "작성자",            // ← 프로필 보강
          "writerProfileImage": "https://...", // ← 프로필 보강
          "commentCount": 3,                  // ← 카운트 보강
          "likeCount": 5,                     // ← 카운트 보강
          "board": { "1": "공지사항" },
          "createdAt": "2025-10-11T17:52:27"
        }
      ],
      "nextCursorUpdatedAt": "2025-10-11T17:52:23",
      "nextCursorId": "6ad747b9-0f34-48ad-8dba-5afa2f7b822f",
      "hasNext": false,
      "size": 10
    }
  }
}
```

##### 2.3 게시글 생성/수정

```
POST /bff/v1/communities/articles/regular
PUT /bff/v1/communities/articles/regular/{articleId}
```

**추가 처리**:

- 토큰에서 `userId` 추출하여 `writerId` 설정
- 수정 시 권한 검증 (실제 작성자 확인)
- 이미지 확정 처리 (ImageConfirmService)

### 3. Profile API (`/bff/v1/profiles`)

#### 특징

- ProfileClient + LikeClient 조합
- 프로필 조회 시 좋아요 수 함께 반환
- 프로필 수정 시 캐시 무효화 + 이미지 확정 병렬 처리

#### 주요 엔드포인트

##### 3.1 내 프로필 조회

```
GET /bff/v1/profiles/me
```

**데이터 조합**:

```java
Mono.zip(
    profileClient.fetchProfile(userId),               // 프로필 정보
    likeClient.getUserLikedCounts("PROFILE", userId)  // 받은 좋아요 수
)
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "profile": {
      "userId": "user123",
      "nickname": "닉네임",
      "profileImageUrl": "https://..."
    },
    "liked": 10  // ← LikeClient에서 조회
  }
}
```

##### 3.2 프로필 수정 + 병렬 처리

```
PUT /bff/v1/profiles/me
```

**병렬 처리**:

```java
Mono.when(
    profileCache.evict(userId),                      // 캐시 무효화
    imageConfirmService.confirmImage(userId, imageId) // 이미지 확정
)
```

### 4. Room & Place API (단순 프록시)

#### 특징

- 1:1 매핑, 데이터 조합 없음
- 도메인 서비스의 응답을 그대로 전달
- BaseResponse 래핑만 수행

#### 엔드포인트 예시

```
GET /bff/v1/places/{placeId}
GET /bff/v1/places/search?keyword=홍대
GET /bff/v1/room-reservations/available-slots?roomId=101&date=2025-01-16
POST /bff/v1/room-reservations/setup
```

**구현 패턴**:

```java
@GetMapping("/{placeId}")
public Mono<ResponseEntity<BaseResponse>> getPlaceById(@PathVariable String placeId) {
    return placeClient.getPlaceById(placeId)
        .map(response -> responseFactory.ok(response, req));
}
```

### 5. Reservation Price API (`/bff/v1/reservations`)

#### 특징

- YeYakHaeYoClient 단일 호출
- 가격 계산 로직은 도메인 서비스에 위임
- BFF는 라우팅만 담당

#### 주요 엔드포인트

```
POST /bff/v1/reservations/preview          # 가격 미리보기
PUT /bff/v1/reservations/{id}/confirm      # 예약 확정
PUT /bff/v1/reservations/{id}/cancel       # 예약 취소
PUT /bff/v1/reservations/{id}/products     # 상품 수정
```

---

## 공통 응답 구조

### BaseResponse

모든 API 응답은 `BaseResponse`로 래핑됩니다.

```json
{
  "isSuccess": true,
  "code": 200,
  "data": { /* 실제 응답 데이터 */ },
  "request": {
    "path": "/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e"
  }
}
```

### 에러 응답

```json
{
  "isSuccess": false,
  "code": 400,
  "data": "targetUserId is required",
  "request": {
    "path": "/bff/v1/activities/feed"
  }
}
```

### 페이징 응답 (Cursor 기반)

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "items": [ /* 데이터 배열 */ ],
    "nextCursor": "eyJpZCI6MSwidXBkYXRlZCI6IjIwMjUtMDEtMTAifQ==",
    "hasNext": true,
    "size": 20,
    "totalCount": 150  // optional
  }
}
```

---

## 성능 최적화 전략

### 1. 배치 API 호출

**문제**: N+1 쿼리 문제
**해결**: 배치 API로 한 번에 조회

```java
// 나쁜 예: N+1 문제 발생
for (Article article : articles) {
    Profile profile = profileClient.getProfile(article.getWriterId()).block();
}

// 좋은 예: 배치 조회
Set<String> writerIds = articles.stream().map(Article::getWriterId).collect(Collectors.toSet());
Map<String, Profile> profiles = profileClient.getBatchProfiles(writerIds).block();
```

### 2. 캐시 활용 (ProfileCache)

**캐시 전략**: Cache-Aside Pattern

```
1. 캐시 조회 시도 (Redis)
2. 캐시 미스 → 배치 API 호출
3. 조회 결과를 캐시에 저장 (비동기, fire-and-forget)
4. 프로필 수정 시 캐시 무효화
```

**구현**:

```java
return profileCache.getAll(userIds)  // 1. 캐시 조회
    .flatMap(cached -> {
        Set<String> missing = userIds - cached.keySet();
        return profileClient.fetchUserSummariesBatch(missing)  // 2. 미스분만 API 호출
            .doOnNext(map -> profileCache.putAll(map)          // 3. 캐시 저장 (비동기)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe())
            .map(fetched -> merge(cached, fetched));           // 4. 결과 병합
    });
```

### 3. 병렬 호출 (Mono.zip)

**독립적인 서비스 호출은 병렬로 실행**

```java
// 순차 호출 (느림)
Article article = articleClient.getArticle(id).block();
List<Comment> comments = commentClient.getComments(id).block();
LikeDetail likes = likeClient.getLikes(id).block();

// 병렬 호출 (빠름)
Mono.zip(
    articleClient.getArticle(id),
    commentClient.getComments(id),
    likeClient.getLikes(id)
).map(tuple3 -> combine(tuple3));
```

### 4. 필드 선택적 조회

**불필요한 필드를 도메인 서비스에서부터 제외**

```java
// 도메인 서비스에 필요한 필드만 요청
articleClient.getArticle(id, fields = ["title", "content", "writerId"])

// BFF에서 추가 필드 제거
comments.stream().map(c -> sanitizeCommentMap(c, currentUserId))
```

### 5. 프로필 보강 최적화

**ProfileEnrichmentUtil 최적화 전략**:

- 중복 userId 제거 (LinkedHashSet 사용)
- 배치 크기 제한 (BATCH_SIZE = 200)
- 소프트 상한선 (SOFT_CAP = 5000)
- 캐시 우선 조회
- 기본값 설정 (DEFAULT_NICKNAME, DEFAULT_PROFILE_IMAGE_URL)

```java
// 중복 제거 및 배치 분할
Set<String> userIds = extractUniqueUserIds(articles);  // 중복 제거
List<List<String>> batches = partition(userIds, 200);   // 200개씩 분할
Flux.fromIterable(batches)
    .concatMap(profileClient::fetchUserSummariesBatch)  // 순차 배치 호출
    .collectList();
```

---

## BFF 구현 체크리스트

### API 설계 시 고려사항

- [ ] 프론트엔드가 필요로 하는 데이터 구조 확인
- [ ] 여러 서비스 호출이 필요한지 판단
- [ ] 독립적인 호출은 병렬 처리 (Mono.zip)
- [ ] 순차적 보강이 필요한 경우 flatMap 체인 구성
- [ ] 불필요한 필드 제거
- [ ] 추가 필드 생성 (isOwn, 카운트 등)
- [ ] 프로필 정보 주입 필요 여부 확인
- [ ] N+1 문제 발생 가능성 검토

### 성능 최적화 체크리스트

- [ ] 배치 API 사용 (단건 API 반복 호출 금지)
- [ ] 캐시 활용 (ProfileCache)
- [ ] 병렬 호출 활용 (Mono.zip, Mono.when)
- [ ] 불필요한 필드 조회 방지
- [ ] 페이징 적용 (Cursor 기반)
- [ ] 타임아웃 설정

### 코드 작성 가이드

```java
// 1. 단순 프록시 패턴
@GetMapping("/{id}")
public Mono<ResponseEntity<BaseResponse>> getItem(@PathVariable String id) {
    return domainClient.getItem(id)
        .map(response -> responseFactory.ok(response, req));
}

// 2. 병렬 조합 패턴
@GetMapping("/{id}")
public Mono<ResponseEntity<BaseResponse>> getItem(@PathVariable String id) {
    return Mono.zip(
        service1.getData1(id),
        service2.getData2(id)
    ).map(tuple2 -> responseFactory.ok(Map.of(
        "data1", tuple2.getT1(),
        "data2", tuple2.getT2()
    ), req));
}

// 3. 다단계 보강 패턴
@GetMapping
public Mono<ResponseEntity<BaseResponse>> getItems() {
    return domainClient.getItems()
        .flatMap(profileEnrichmentUtil::enrichItemList)  // 프로필 주입
        .flatMap(items -> countUtil.enrichWithCounts(items)) // 카운트 주입
        .map(enrichedItems -> responseFactory.ok(enrichedItems, req));
}

// 4. 병렬 처리 패턴 (응답 조합 불필요)
@PutMapping("/{id}")
public Mono<ResponseEntity<BaseResponse>> updateItem(@PathVariable String id) {
    return domainClient.updateItem(id, request)
        .flatMap(success -> {
            Mono<Void> task1 = cache.evict(id);
            Mono<Void> task2 = imageService.confirmImage(id);
            return Mono.when(task1, task2)
                .thenReturn(responseFactory.ok(success, req));
        });
}
```

---

## 부록: 주요 유틸리티 클래스

### ProfileEnrichmentUtil

**역할**: 프로필 정보 배치 조회 및 주입

**주요 메서드**:

- `enrichItemList()`: Map 리스트 보강
- `enrichArticle()`: 단일 게시글 보강
- `enrichArticleAndComments()`: 게시글 + 댓글 트리 보강
- `enrichArticleList()`: ArticleSimpleResponse 리스트 보강
- `enrichArticleResponseList()`: ArticleResponse 리스트 보강
- `enrichAny()`: 임의의 객체 구조 보강

**캐시 전략**: Cache-Aside Pattern (Redis)

### ArticleCountUtil

**역할**: 게시글 리스트에 댓글/좋아요 수 배치 조회 및 주입

**주요 메서드**:

- `enrichWithCounts()`: EnrichedArticleResponse 리스트에 카운트 주입

**처리 과정**:

1. articleIds 추출
2. LikeClient + CommentClient 병렬 배치 조회 (Mono.zip)
3. Map으로 변환하여 빠른 룩업
4. 각 article에 카운트 설정

### ResponseFactory

**역할**: BaseResponse 래핑

**주요 메서드**:

- `ok(data, request)`: 성공 응답
- `error(message, status, request)`: 에러 응답

### UserIdValidator

**역할**: 토큰에서 userId 추출 및 권한 검증

**주요 메서드**:

- `extractTokenUserId(request)`: 토큰에서 userId 추출
- `validateOwnership(request, ownerId, resourceName)`: 소유자 검증
- `validateReactive(request, targetUserId)`: 반응형 검증

---

## 문서 버전

- **버전**: 1.0
- **최종 수정일**: 2025-01-25
- **작성자**: API Gateway Team
