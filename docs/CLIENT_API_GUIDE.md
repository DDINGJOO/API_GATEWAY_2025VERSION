# Bander API 사용 가이드 (클라이언트용)

> 신입 개발자를 위한 완벽 가이드: 모든 API 엔드포인트, Request/Response 예시, 필드 설명 포함

## 목차

1. [시작하기](#시작하기)
2. [인증](#인증)
3. [공통 응답 구조](#공통-응답-구조)
4. [게시글 API](#게시글-api)
5. [댓글 API](#댓글-api)
6. [좋아요 (개츄) API](#좋아요-개츄-api)
7. [피드/활동 API](#피드활동-api)
8. [프로필 API](#프로필-api)
9. [장소 (Place) API](#장소-place-api)
10. [룸 (Room) API](#룸-room-api)
11. [룸 예약 API](#룸-예약-api)
12. [예약 가격 API](#예약-가격-api)
13. [에러 처리](#에러-처리)

---

## 시작하기

### 기본 정보

- **Base URL**: `https://api.bander.app` (실제 배포 시)
- **개발 서버**: `http://localhost:8080`
- **모든 엔드포인트**: `/bff/v1/` 경로로 시작
- **응답 형식**: JSON
- **문자 인코딩**: UTF-8

### 필수 요청 헤더

```http
Content-Type: application/json
Authorization: Bearer {access_token}
```

---

## 인증

### JWT 토큰 기반 인증

대부분의 API는 인증이 필요합니다. 요청 헤더에 JWT 토큰을 포함해야 합니다.

```bash
curl -X GET "https://api.bander.app/bff/v1/profiles/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

**토큰에서 자동으로 추출되는 정보**:

- `userId`: 현재 로그인한 사용자 ID
- 게시글/댓글 작성 시 `writerId`는 자동으로 토큰에서 추출되어 설정됩니다

**인증이 필요 없는 API**:

- 게시글 목록 조회 (GET)
- 게시글 단건 조회 (GET)
- 장소 검색/조회
- 룸 조회

---

## 공통 응답 구조

모든 API는 `BaseResponse` 구조로 응답합니다.

### 성공 응답

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    /* 실제 응답 데이터 */
  },
  "request": {
    "path": "/bff/v1/communities/articles/regular/abc-123"
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

### 상태 코드

| 코드  | 의미                    | 설명                           |
|-----|-----------------------|------------------------------|
| 200 | OK                    | 요청 성공                        |
| 201 | Created               | 리소스 생성 성공                    |
| 204 | No Content            | 성공했지만 반환할 데이터 없음 (좋아요, 삭제 등) |
| 400 | Bad Request           | 잘못된 요청 (필수 파라미터 누락, 유효성 오류)  |
| 401 | Unauthorized          | 인증 실패 (토큰 없음, 만료)            |
| 403 | Forbidden             | 권한 없음 (본인만 수정/삭제 가능)         |
| 404 | Not Found             | 리소스를 찾을 수 없음                 |
| 409 | Conflict              | 충돌 (이미 존재하는 리소스)             |
| 500 | Internal Server Error | 서버 오류                        |

---

## 게시글 API

### 1. 게시글 목록 조회

**엔드포인트**: `GET /bff/v1/communities/articles/regular`

**인증**: 불필요 (선택)

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| size | Integer | X | 10 | 한 페이지당 게시글 수 |
| cursorId | String | X | null | 커서 페이징용 마지막 게시글 ID |
| boardIds | Long | X | null | 게시판 ID (1=공지사항, 2=자유게시판 등) |
| keyword | List<Long> | X | null | 키워드 ID 배열 (복수 선택 가능) |
| title | String | X | null | 제목 검색 |
| content | String | X | null | 내용 검색 |
| writerId | String | X | null | 작성자 ID로 필터링 |

**curl 예시**:

```bash
# 기본 목록 조회
curl -X GET "http://localhost:8080/bff/v1/communities/articles/regular?size=10"

# 게시판 ID로 필터링
curl -X GET "http://localhost:8080/bff/v1/communities/articles/regular?boardIds=1&size=20"

# 제목으로 검색
curl -X GET "http://localhost:8080/bff/v1/communities/articles/regular?title=공연&size=10"

# 커서 페이징 (다음 페이지)
curl -X GET "http://localhost:8080/bff/v1/communities/articles/regular?cursorId=article-123&size=10"
```

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
          "content": "같이 즐겁게 공연하실 분을 찾습니다.",
          "writerId": "user_123",
          "writerName": "홍길동",              // ← 프로필에서 자동 주입
          "writerProfileImage": "https://...", // ← 프로필에서 자동 주입
          "board": {
            "1": "공지사항"
          },
          "status": "ACTIVE",
          "viewCount": 42,
          "firstImageUrl": "https://example.com/image.jpg",
          "createdAt": "2025-10-11T17:52:27",
          "updatedAt": "2025-10-11T17:52:27",
          "images": {
            "img-1": "https://example.com/img1.jpg",
            "img-2": "https://example.com/img2.jpg"
          },
          "keywords": {
            "10": "중요",
            "20": "모집"
          },
          "commentCount": 3,   // ← Comment 서비스에서 자동 주입
          "likeCount": 5       // ← Like 서비스에서 자동 주입
        }
      ],
      "nextCursorUpdatedAt": "2025-10-11T17:52:23",
      "nextCursorId": "6ad747b9-0f34-48ad-8dba-5afa2f7b822f",
      "hasNext": true,
      "size": 10
    }
  },
  "request": {
    "path": "/bff/v1/communities/articles/regular?size=10"
  }
}
```

**Response 필드 설명**:
| 필드 | 타입 | 설명 |
|------|------|------|
| items | Array | 게시글 배열 |
| items[].articleId | String | 게시글 고유 ID |
| items[].title | String | 게시글 제목 |
| items[].content | String | 게시글 내용 |
| items[].writerId | String | 작성자 ID |
| items[].writerName | String | 작성자 닉네임 (BFF에서 프로필 서비스 조회 후 주입) |
| items[].writerProfileImage | String | 작성자 프로필 이미지 URL (BFF에서 주입) |
| items[].board | Object | 게시판 정보 (key: 게시판 ID, value: 게시판명) |
| items[].status | String | 게시글 상태 (ACTIVE, DELETED, HIDDEN 등) |
| items[].viewCount | Integer | 조회 수 |
| items[].firstImageUrl | String | 첫 번째 이미지 URL (썸네일용) |
| items[].createdAt | String | 생성 시각 (ISO 8601 형식) |
| items[].updatedAt | String | 수정 시각 |
| items[].images | Object | 이미지 맵 (key: 이미지 ID, value: 이미지 URL) |
| items[].keywords | Object | 키워드 맵 (key: 키워드 ID, value: 키워드명) |
| items[].commentCount | Integer | 댓글 수 (BFF에서 Comment 서비스 조회 후 주입) |
| items[].likeCount | Integer | 좋아요 수 (BFF에서 Like 서비스 조회 후 주입) |
| nextCursorUpdatedAt | String | 다음 페이지 커서 (수정 시각) |
| nextCursorId | String | 다음 페이지 커서 (게시글 ID) |
| hasNext | Boolean | 다음 페이지 존재 여부 |
| size | Integer | 요청한 페이지 크기 |

**다음 페이지 요청 방법**:

```bash
# hasNext가 true이고 nextCursorId가 있으면 다음과 같이 요청
curl -X GET "http://localhost:8080/bff/v1/communities/articles/regular?cursorId={nextCursorId}&size=10"
```

---

### 2. 게시글 단건 조회 (댓글 포함)

**엔드포인트**: `GET /bff/v1/communities/articles/regular/{articleId}`

**인증**: 불필요 (선택)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| articleId | String | O | 게시글 ID |

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "article": {
      "articleId": "42840044-0f3e-482c-b5d5-0883af43e63e",
      "title": "공연 함께 하실 분",
      "content": "같이 즐겁게 공연하실 분을 찾습니다.\n\n장소: 홍대 라이브클럽\n날짜: 2025년 11월 15일",
      "writerId": "user_123",
      "nickname": "홍길동",              // ← 프로필 주입
      "profileImageUrl": "https://...", // ← 프로필 주입
      "board": {
        "1": "공지사항"
      },
      "status": "ACTIVE",
      "viewCount": 42,
      "firstImageUrl": "https://example.com/image.jpg",
      "createdAt": "2025-10-11T17:52:27",
      "updatedAt": "2025-10-11T17:52:27",
      "images": {
        "img-1": "https://example.com/img1.jpg"
      },
      "keywords": {
        "10": "중요"
      }
    },
    "comments": [
      {
        "commentId": "c1",
        "writerId": "user_456",
        "nickname": "김철수",           // ← 프로필 주입
        "profileImageUrl": "https://...", // ← 프로필 주입
        "contents": "저도 참여하고 싶어요!",
        "isOwn": false,                  // ← 본인 댓글 여부 (BFF에서 추가)
        "depth": 0,
        "replyCount": 1,
        "createdAt": "2025-10-11T18:00:00",
        "updatedAt": "2025-10-11T18:00:00",
        "status": "ACTIVE",
        "isDeleted": false,
        "replies": [
          {
            "commentId": "c2",
            "writerId": "user_123",
            "nickname": "홍길동",
            "profileImageUrl": "https://...",
            "contents": "감사합니다! DM 보내주세요",
            "isOwn": false,
            "depth": 1,
            "replyCount": 0,
            "createdAt": "2025-10-11T18:05:00",
            "updatedAt": "2025-10-11T18:05:00",
            "status": "ACTIVE",
            "isDeleted": false,
            "replies": []
          }
        ]
      }
    ],
    "likeDetail": {
      "likeCount": 5,
      "isOwn": false    // ← 내가 좋아요 눌렀는지 여부 (BFF에서 추가)
    }
  },
  "request": {
    "path": "/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e"
  }
}
```

**Response 필드 설명**:

**article 객체**:
| 필드 | 타입 | 설명 |
|------|------|------|
| articleId | String | 게시글 ID |
| title | String | 제목 |
| content | String | 내용 (개행 포함) |
| writerId | String | 작성자 ID |
| nickname | String | 작성자 닉네임 (BFF 자동 주입) |
| profileImageUrl | String | 작성자 프로필 이미지 (BFF 자동 주입) |
| board | Object | 게시판 정보 |
| status | String | 상태 (ACTIVE, DELETED 등) |
| viewCount | Integer | 조회 수 |
| firstImageUrl | String | 첫 이미지 URL |
| createdAt | String | 생성 시각 |
| updatedAt | String | 수정 시각 |
| images | Object | 이미지 맵 |
| keywords | Object | 키워드 맵 |

**comments 배열**:
| 필드 | 타입 | 설명 |
|------|------|------|
| commentId | String | 댓글 ID |
| writerId | String | 작성자 ID |
| nickname | String | 작성자 닉네임 (BFF 자동 주입) |
| profileImageUrl | String | 작성자 프로필 이미지 (BFF 자동 주입) |
| contents | String | 댓글 내용 |
| isOwn | Boolean | 내가 작성한 댓글인지 여부 (BFF에서 토큰과 비교하여 추가) |
| depth | Integer | 댓글 깊이 (0=루트 댓글, 1=대댓글) |
| replyCount | Integer | 대댓글 수 |
| createdAt | String | 생성 시각 |
| updatedAt | String | 수정 시각 |
| status | String | 상태 (ACTIVE, DELETED) |
| isDeleted | Boolean | 삭제 여부 |
| replies | Array | 대댓글 배열 (재귀 구조) |

**likeDetail 객체**:
| 필드 | 타입 | 설명 |
|------|------|------|
| likeCount | Integer | 좋아요 수 |
| isOwn | Boolean | 내가 좋아요를 눌렀는지 여부 (BFF에서 추가) |

**주의사항**:

- `comments`는 첫 10개만 조회됩니다 (추가 댓글은 댓글 API로 페이징 조회)
- `isOwn` 필드는 BFF에서 추가되며, 인증되지 않은 요청에서는 항상 `false`입니다

---

### 3. 게시글 생성

**엔드포인트**: `POST /bff/v1/communities/articles/regular`

**인증**: 필수

**Request Body**:

```json
{
  "title": "공연 함께 하실 분",
  "content": "같이 즐겁게 공연하실 분을 찾습니다.",
  "boardId": 1,
  "imageIds": ["img-1", "img-2"],
  "keywords": [10, 20]
}
```

**Request 필드 설명**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | O | 게시글 제목 |
| content | String | O | 게시글 내용 |
| boardId | Long | O | 게시판 ID (1=공지사항, 2=자유게시판 등) |
| imageIds | Array<String> | X | 이미지 ID 배열 (이미지 업로드 API로 먼저 업로드 후 받은 ID) |
| keywords | Array<Long> | X | 키워드 ID 배열 |

**curl 예시**:

```bash
curl -X POST "http://localhost:8080/bff/v1/communities/articles/regular" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "공연 함께 하실 분",
    "content": "같이 즐겁게 공연하실 분을 찾습니다.",
    "boardId": 1,
    "imageIds": ["img-1"],
    "keywords": [10]
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "articleId": "generated-uuid-123",
    "title": "공연 함께 하실 분",
    "content": "같이 즐겁게 공연하실 분을 찾습니다.",
    "writerId": "user_123",
    "createdAt": "2025-10-11T17:52:27"
  },
  "request": {
    "path": "/bff/v1/communities/articles/regular"
  }
}
```

**자동 처리 사항**:

- `writerId`는 토큰에서 자동 추출 (Request Body에 포함하지 않아도 됨)
- `imageIds`가 있으면 이미지 확정 처리가 자동으로 수행됨

---

### 4. 게시글 수정

**엔드포인트**: `PUT /bff/v1/communities/articles/regular/{articleId}`

**인증**: 필수 (본인만 수정 가능)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| articleId | String | O | 수정할 게시글 ID |

**Request Body**:

```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "boardId": 1,
  "imageIds": ["img-3"],
  "keywords": [15]
}
```

**curl 예시**:

```bash
curl -X PUT "http://localhost:8080/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 제목",
    "content": "수정된 내용",
    "boardId": 1
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "articleId": "42840044-0f3e-482c-b5d5-0883af43e63e",
    "title": "수정된 제목",
    "updatedAt": "2025-10-11T18:30:00"
  },
  "request": {
    "path": "/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e"
  }
}
```

**에러 응답 (본인이 아닌 경우)**:

```json
{
  "isSuccess": false,
  "code": 403,
  "data": "게시글 수정 권한이 없습니다. 본인만 수정할 수 있습니다.",
  "request": {
    "path": "/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e"
  }
}
```

---

### 5. 게시글 삭제

**엔드포인트**: `DELETE /bff/v1/communities/articles/regular/{articleId}`

**인증**: 필수 (본인만 삭제 가능)

**curl 예시**:

```bash
curl -X DELETE "http://localhost:8080/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": "deleted",
  "request": {
    "path": "/bff/v1/communities/articles/regular/42840044-0f3e-482c-b5d5-0883af43e63e"
  }
}
```

---

## 댓글 API

### 1. 댓글/대댓글 생성 (통합)

**엔드포인트**: `POST /bff/v1/communities/comments/create`

**인증**: 필수

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| parentId | String | X | 대댓글인 경우 부모 댓글 ID (없으면 루트 댓글) |

**Request Body**:

```json
{
  "articleId": "article-123",
  "contents": "댓글 내용입니다."
}
```

**Request 필드 설명**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| articleId | String | O (루트 댓글만) | 게시글 ID (대댓글인 경우 불필요) |
| contents | String | O | 댓글 내용 |

**curl 예시**:

```bash
# 루트 댓글 생성
curl -X POST "http://localhost:8080/bff/v1/communities/comments/create" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "articleId": "article-123",
    "contents": "좋은 글이네요!"
  }'

# 대댓글 생성
curl -X POST "http://localhost:8080/bff/v1/communities/comments/create?parentId=c1" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": "저도 동의합니다."
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 201,
  "data": {
    "commentId": "generated-comment-id",
    "articleId": "article-123",
    "writerId": "user_123",
    "nickname": "홍길동",           // ← 프로필 자동 주입
    "profileImageUrl": "https://...", // ← 프로필 자동 주입
    "parentCommentId": null,
    "rootCommentId": "generated-comment-id",
    "depth": 0,
    "contents": "좋은 글이네요!",
    "isDeleted": false,
    "status": "ACTIVE",
    "replyCount": 0,
    "createdAt": "2025-10-11T18:00:00",
    "updatedAt": "2025-10-11T18:00:00"
  },
  "request": {
    "path": "/bff/v1/communities/comments/create"
  }
}
```

---

### 2. 게시글의 댓글 목록 조회

**엔드포인트**: `GET /bff/v1/communities/comments/article`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| articleId | String | O | - | 게시글 ID |
| page | Integer | X | 0 | 페이지 번호 (0부터 시작) |
| mode | String | X | visibleCount | 조회 모드 (all=전체, visibleCount=페이징) |

**curl 예시**:

```bash
# 첫 페이지 조회 (게시글 상세에서 이미 0페이지 조회했으므로, 추가 댓글은 1페이지부터)
curl -X GET "http://localhost:8080/bff/v1/communities/comments/article?articleId=article-123&page=1"

# 전체 댓글 조회
curl -X GET "http://localhost:8080/bff/v1/communities/comments/article?articleId=article-123&mode=all"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": [
    {
      "commentId": "c1",
      "articleId": "article-123",
      "writerId": "user_456",
      "nickname": "김철수",
      "profileImageUrl": "https://...",
      "parentCommentId": null,
      "rootCommentId": "c1",
      "depth": 0,
      "contents": "좋은 글이네요!",
      "isDeleted": false,
      "status": "ACTIVE",
      "replyCount": 2,
      "createdAt": "2025-10-11T18:00:00",
      "updatedAt": "2025-10-11T18:00:00"
    }
  ],
  "request": {
    "path": "/bff/v1/communities/comments/article?articleId=article-123&page=1"
  }
}
```

---

### 3. 댓글 수정

**엔드포인트**: `PATCH /bff/v1/communities/comments/{id}`

**인증**: 필수 (본인만 수정 가능)

**Request Body**:

```json
{
  "contents": "수정된 댓글 내용"
}
```

**curl 예시**:

```bash
curl -X PATCH "http://localhost:8080/bff/v1/communities/comments/c1" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": "수정된 댓글 내용"
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "commentId": "c1",
    "contents": "수정된 댓글 내용",
    "updatedAt": "2025-10-11T18:30:00"
  },
  "request": {
    "path": "/bff/v1/communities/comments/c1"
  }
}
```

---

### 4. 댓글 삭제 (소프트 삭제)

**엔드포인트**: `DELETE /bff/v1/communities/comments/{id}`

**인증**: 필수 (본인만 삭제 가능)

**curl 예시**:

```bash
curl -X DELETE "http://localhost:8080/bff/v1/communities/comments/c1" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 204,
  "data": null,
  "request": {
    "path": "/bff/v1/communities/comments/c1"
  }
}
```

**주의사항**:

- 댓글 삭제는 소프트 삭제입니다 (실제로 삭제되지 않고 상태만 변경)
- 삭제된 댓글은 "삭제된 댓글입니다"로 표시됩니다

---

## 좋아요 (개츄) API

### 좋아요/좋아요 취소 토글

**엔드포인트**: `POST /bff/v1/gaechu/likes/{categoryId}/{referenceId}`

**인증**: 필수

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| categoryId | String | O | 카테고리 (ARTICLE, PROFILE, COMMENT 등) |
| referenceId | String | O | 대상 ID (게시글 ID, 프로필 ID 등) |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| likerId | String | O | 좋아요를 누르는 사용자 ID (토큰의 userId와 일치해야 함) |
| isLike | Boolean | O | true=좋아요 추가, false=좋아요 취소 |

**curl 예시**:

```bash
# 게시글 좋아요
curl -X POST "http://localhost:8080/bff/v1/gaechu/likes/ARTICLE/article-123?likerId=user_123&isLike=true" \
  -H "Authorization: Bearer {token}"

# 게시글 좋아요 취소
curl -X POST "http://localhost:8080/bff/v1/gaechu/likes/ARTICLE/article-123?likerId=user_123&isLike=false" \
  -H "Authorization: Bearer {token}"

# 프로필 좋아요
curl -X POST "http://localhost:8080/bff/v1/gaechu/likes/PROFILE/user_456?likerId=user_123&isLike=true" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 204,
  "data": null,
  "request": {
    "path": "/bff/v1/gaechu/likes/ARTICLE/article-123?likerId=user_123&isLike=true"
  }
}
```

**카테고리 종류**:
| 카테고리 | 설명 |
|---------|------|
| ARTICLE | 게시글 좋아요 |
| PROFILE | 프로필 좋아요 |
| COMMENT | 댓글 좋아요 |

**주의사항**:

- `likerId`는 토큰의 `userId`와 일치해야 합니다 (다른 사람 대신 좋아요 불가)
- 204 응답이면 성공 (data는 null)

---

## 피드/활동 API

### 1. 피드 활동 총합 조회

**엔드포인트**: `POST /bff/v1/activities/feed`

**인증**: 불필요

**Request Body**:

```json
{
  "targetUserId": "user_123",
  "categories": ["article", "comment", "like"]
}
```

**Request 필드 설명**:
| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| targetUserId | String | O | - | 조회 대상 사용자 ID |
| categories | Array<String> | X | ["article", "comment", "like"] | 조회할 카테고리 배열 |

**curl 예시**:

```bash
curl -X POST "http://localhost:8080/bff/v1/activities/feed" \
  -H "Content-Type: application/json" \
  -d '{
    "targetUserId": "user_123",
    "categories": ["article", "comment", "like"]
  }'
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
  },
  "request": {
    "path": "/bff/v1/activities/feed"
  }
}
```

**Response 필드 설명**:
| 필드 | 타입 | 설명 |
|------|------|------|
| totals | Object | 카테고리별 활동 수 |
| totals.article | Integer | 작성한 게시글 수 |
| totals.comment | Integer | 작성한 댓글 수 |
| totals.like | Integer | 누른 좋아요 수 |
| isOwner | Boolean | 조회자와 대상이 동일한지 여부 |

---

### 2. 카테고리별 피드 조회

**엔드포인트**: `GET /bff/v1/activities/feed/{category}`

**인증**: like 카테고리는 필수 (본인만 조회 가능), 나머지는 불필요

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| category | String | O | article, comment, like 중 하나 |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| targetUserId | String | O | - | 조회 대상 사용자 ID |
| viewerId | String | X | null | 조회하는 사용자 ID |
| cursor | String | X | null | 커서 (다음 페이지용) |
| size | Integer | X | 20 | 페이지 크기 |
| sort | String | X | newest | 정렬 (newest, oldest) |

**curl 예시**:

```bash
# 게시글 피드 조회
curl -X GET "http://localhost:8080/bff/v1/activities/feed/article?targetUserId=user_123&size=20"

# 댓글 피드 조회
curl -X GET "http://localhost:8080/bff/v1/activities/feed/comment?targetUserId=user_123&size=20"

# 좋아요 피드 조회 (본인만 가능)
curl -X GET "http://localhost:8080/bff/v1/activities/feed/like?targetUserId=user_123&size=20" \
  -H "Authorization: Bearer {token}"
```

**Response** (article 카테고리):

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
        "writerId": "user_123",
        "writerName": "홍길동",        // ← 프로필 보강
        "writerProfileImage": "https://...", // ← 프로필 보강
        "likeCount": 15,                // ← 카운트 보강
        "commentCount": 8,              // ← 카운트 보강
        "createdAt": "2025-10-11T17:52:27"
      }
    ],
    "nextCursor": "a-98"
  },
  "request": {
    "path": "/bff/v1/activities/feed/article?targetUserId=user_123&size=20"
  }
}
```

**주의사항**:

- `like` 카테고리는 본인만 조회 가능 (타인의 좋아요 목록은 비공개)
- 응답에는 게시글 상세 정보 + 프로필 정보 + 카운트 정보가 모두 포함됩니다

---

## 프로필 API

### 1. 프로필 목록 조회

**엔드포인트**: `GET /bff/v1/profiles`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| city | String | X | 도시 (서울, 부산 등) |
| nickName | String | X | 닉네임 검색 |
| genres | List<Integer> | X | 장르 ID 배열 |
| instruments | List<Integer> | X | 악기 ID 배열 |
| sex | Character | X | 성별 (M, F) |
| cursor | String | X | 커서 |
| size | Integer | X | 페이지 크기 |

**curl 예시**:

```bash
# 기본 목록 조회
curl -X GET "http://localhost:8080/bff/v1/profiles?size=10"

# 서울 지역 프로필 검색
curl -X GET "http://localhost:8080/bff/v1/profiles?city=서울&size=10"

# 닉네임으로 검색
curl -X GET "http://localhost:8080/bff/v1/profiles?nickName=홍길동"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "content": [
      {
        "userId": "user_123",
        "nickname": "홍길동",
        "profileImageUrl": "https://...",
        "city": "서울",
        "sex": "M",
        "genres": [1, 2],
        "instruments": [3, 4]
      }
    ],
    "pageable": { /* 페이징 정보 */ },
    "size": 10,
    "numberOfElements": 5,
    "first": true,
    "last": false
  },
  "request": {
    "path": "/bff/v1/profiles?size=10"
  }
}
```

---

### 2. 내 프로필 조회

**엔드포인트**: `GET /bff/v1/profiles/me`

**인증**: 필수

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/profiles/me" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "profile": {
      "userId": "user_123",
      "nickname": "홍길동",
      "profileImageUrl": "https://...",
      "city": "서울",
      "sex": "M",
      "genres": [1, 2],
      "instruments": [3, 4],
      "introduction": "안녕하세요 기타리스트입니다."
    },
    "liked": 10  // ← 받은 좋아요 수 (BFF에서 Like 서비스 조회)
  },
  "request": {
    "path": "/bff/v1/profiles/me"
  }
}
```

---

### 3. 특정 사용자 프로필 조회

**엔드포인트**: `GET /bff/v1/profiles/{userId}`

**인증**: 불필요

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/profiles/user_456"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "profile": {
      "userId": "user_456",
      "nickname": "김철수",
      "profileImageUrl": "https://...",
      "city": "부산",
      "sex": "M"
    },
    "liked": 5  // ← 받은 좋아요 수
  },
  "request": {
    "path": "/bff/v1/profiles/user_456"
  }
}
```

---

### 4. 내 프로필 수정

**엔드포인트**: `PUT /bff/v1/profiles/me`

**인증**: 필수

**Request Body**:

```json
{
  "nickname": "새닉네임",
  "profileImageId": "img-123",
  "city": "서울",
  "sex": "M",
  "genres": [1, 2],
  "instruments": [3],
  "introduction": "새로운 소개"
}
```

**curl 예시**:

```bash
curl -X PUT "http://localhost:8080/bff/v1/profiles/me" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "새닉네임",
    "city": "서울"
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": true,
  "request": {
    "path": "/bff/v1/profiles/me"
  }
}
```

**자동 처리 사항**:

- 프로필 캐시 자동 무효화
- 이미지 확정 처리 자동 수행

---

### 5. 프로필 필드 검증

**엔드포인트**: `GET /bff/v1/profiles/validate`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| type | String | O | 검증 타입 (nickname 등) |
| value | String | O | 검증할 값 |

**curl 예시**:

```bash
# 닉네임 중복 검사
curl -X GET "http://localhost:8080/bff/v1/profiles/validate?type=nickname&value=홍길동"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": false,  // false = 사용 가능, true = 중복
  "request": {
    "path": "/bff/v1/profiles/validate?type=nickname&value=홍길동"
  }
}
```

---

## 장소 (Place) API

### 1. 장소 상세 조회

**엔드포인트**: `GET /bff/v1/places/{placeId}`

**인증**: 불필요

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/places/1"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "id": "1",
    "userId": "owner_123",
    "placeName": "밴더 홍대점",
    "description": "프리미엄 음악 연습실",
    "category": "MUSIC_STUDIO",
    "placeType": "RENTAL",
    "contact": {
      "contact": "02-1234-5678",
      "email": "contact@example.com",
      "websites": ["https://bander.com"],
      "socialLinks": ["https://instagram.com/bander"]
    },
    "location": {
      "address": {
        "province": "서울특별시",
        "city": "마포구",
        "district": "서교동",
        "fullAddress": "서울 마포구 서교동 123-45",
        "addressDetail": "3층",
        "postalCode": "04001",
        "shortAddress": "서울 마포구"
      },
      "latitude": 37.5556,
      "longitude": 126.9233,
      "locationGuide": "홍대입구역 9번 출구에서 도보 5분"
    },
    "parking": {
      "available": true,
      "parkingType": "FREE",
      "description": "건물 지하 주차장 이용 가능"
    },
    "imageUrls": ["https://example.com/place1.jpg"],
    "keywords": [
      {
        "id": 1,
        "name": "연습실",
        "type": "SPACE_TYPE",
        "description": "음악 연습 공간",
        "displayOrder": 1
      }
    ],
    "isActive": true,
    "approvalStatus": "APPROVED",
    "ratingAverage": 4.5,
    "reviewCount": 42,
    "roomCount": 5,
    "roomIds": [101, 102, 103, 104, 105],
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-10T15:30:00"
  },
  "request": {
    "path": "/bff/v1/places/1"
  }
}
```

---

### 2. 장소 통합 검색

**엔드포인트**: `GET /bff/v1/places/search`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| keyword | String | X | 검색 키워드 |
| placeName | String | X | 장소명 |
| category | String | X | 카테고리 |
| placeType | String | X | 장소 타입 |
| keywordIds | List<Long> | X | 키워드 ID 배열 |
| parkingAvailable | Boolean | X | 주차 가능 여부 |
| latitude | Double | X | 위도 (위치 기반 검색) |
| longitude | Double | X | 경도 |
| radius | Integer | X | 검색 반경 (미터) |
| province | String | X | 시/도 |
| city | String | X | 시/군/구 |
| district | String | X | 동/읍/면 |
| sortBy | String | X | 정렬 기준 (DISTANCE, RATING 등) |
| sortDirection | String | X | 정렬 방향 (ASC, DESC) |
| cursor | String | X | 커서 |
| size | Integer | X | 20 | 페이지 크기 |

**curl 예시**:

```bash
# 키워드 검색
curl -X GET "http://localhost:8080/bff/v1/places/search?keyword=홍대&size=10"

# 위치 기반 검색 (반경 5km)
curl -X GET "http://localhost:8080/bff/v1/places/search?latitude=37.5556&longitude=126.9233&radius=5000&sortBy=DISTANCE"

# 주차 가능한 장소만
curl -X GET "http://localhost:8080/bff/v1/places/search?parkingAvailable=true"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "items": [
      {
        "id": "1",
        "placeName": "밴더 홍대점",
        "description": "프리미엄 음악 연습실",
        "category": "MUSIC_STUDIO",
        "placeType": "RENTAL",
        "fullAddress": "서울 마포구 양화로 123",
        "latitude": 37.5556,
        "longitude": 126.9233,
        "distance": 1200.0,  // ← 위치 기반 검색 시에만
        "ratingAverage": 4.5,
        "reviewCount": 42,
        "parkingAvailable": true,
        "parkingType": "FREE",
        "thumbnailUrl": "https://example.com/place1.jpg",
        "keywords": ["연습실", "악기대여"],
        "contact": "02-1234-5678",
        "isActive": true,
        "approvalStatus": "APPROVED",
        "roomCount": 5,
        "roomIds": [101, 102, 103, 104, 105]
      }
    ],
    "nextCursor": "eyJpZCI6MSwidXBkYXRlZCI6IjIwMjUtMDEtMTAifQ==",
    "hasNext": true,
    "count": 1,
    "totalCount": 15,
    "metadata": {
      "searchTime": 150,
      "sortBy": "DISTANCE",
      "sortDirection": "ASC",
      "centerLat": 37.5556,
      "centerLng": 126.9233,
      "radiusInMeters": 5000,
      "appliedFilters": "keyword=홍대, parkingAvailable=true"
    }
  },
  "request": {
    "path": "/bff/v1/places/search?keyword=홍대&latitude=37.5556&longitude=126.9233"
  }
}
```

---

### 3. 주변 장소 조회

**엔드포인트**: `GET /bff/v1/places/nearby`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| latitude | Double | O | - | 위도 |
| longitude | Double | O | - | 경도 |
| radius | Integer | X | 5000 | 검색 반경 (미터) |
| keyword | String | X | null | 키워드 |
| keywordIds | List<Long> | X | null | 키워드 ID 배열 |
| parkingAvailable | Boolean | X | null | 주차 가능 여부 |
| cursor | String | X | null | 커서 |
| size | Integer | X | 20 | 페이지 크기 |

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/places/nearby?latitude=37.5556&longitude=126.9233&radius=5000&size=10"
```

---

### 4. 인기 장소 조회

**엔드포인트**: `GET /bff/v1/places/popular`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| size | Integer | X | 10 | 조회 개수 |

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/places/popular?size=10"
```

---

### 5. 최신 장소 조회

**엔드포인트**: `GET /bff/v1/places/recent`

**인증**: 불필요

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/places/recent?size=10"
```

---

## 룸 (Room) API

### 1. 룸 상세 조회 (장소 정보 + 가격 정책 포함)

**엔드포인트**: `GET /bff/v1/rooms/{roomId}`

**인증**: 불필요

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/rooms/101"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "room": {
      "roomId": 101,
      "roomName": "A룸",
      "placeId": 1,
      "status": "OPEN",
      "timeSlot": "HOUR",
      "furtherDetails": ["방음 시설 완비", "24시간 이용 가능"],
      "cautionDetails": ["흡연 금지", "음식물 반입 금지"],
      "imageUrls": ["https://example.com/room1.jpg"],
      "keywordIds": [1, 2, 3]
    },
    "place": {
      "id": "1",
      "placeName": "밴더 홍대점",
      "fullAddress": "서울 마포구 서교동 123-45",
      /* 장소 상세 정보 */
    },
    "pricingPolicy": {
      "roomId": 101,
      "placeId": 1,
      "timeSlot": "1시간",
      "defaultPrice": 15000,
      "timeRangePrices": [
        {
          "dayOfWeek": "MONDAY",
          "startTime": "09:00",
          "endTime": "18:00",
          "price": 15000
        },
        {
          "dayOfWeek": "SATURDAY",
          "startTime": "09:00",
          "endTime": "22:00",
          "price": 20000
        }
      ]
    },
    "availableProducts": [
      {
        "productId": 1,
        "scope": "ROOM",
        "name": "1시간 이용권",
        "pricingStrategy": {
          "pricingType": "FIXED",
          "initialPrice": 15000
        },
        "totalQuantity": 10
      }
    ]
  },
  "request": {
    "path": "/bff/v1/rooms/101"
  }
}
```

**주의사항**:

- 이 API는 **여러 서비스를 조합**하여 응답합니다 (Room + Place + Pricing + Product)
- 일부 서비스 조회 실패 시에도 다른 정보는 반환됩니다 (null 또는 빈 배열)

---

### 2. 룸 검색

**엔드포인트**: `GET /bff/v1/rooms/search`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roomName | String | X | 룸 이름 |
| keywordIds | List<Long> | X | 키워드 ID 배열 |
| placeId | Long | X | 장소 ID |

**curl 예시**:

```bash
# 장소별 검색
curl -X GET "http://localhost:8080/bff/v1/rooms/search?placeId=1"

# 키워드로 검색
curl -X GET "http://localhost:8080/bff/v1/rooms/search?keywordIds=1,2,3"
```

---

### 3. 장소별 룸 목록 조회

**엔드포인트**: `GET /bff/v1/rooms/place/{placeId}`

**인증**: 불필요

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/rooms/place/1"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": [
    {
      "roomId": 101,
      "roomName": "A룸",
      "placeId": 1,
      "timeSlot": "HOUR",
      "imageUrls": ["https://example.com/room1.jpg"],
      "keywordIds": [1, 2, 3]
    },
    {
      "roomId": 102,
      "roomName": "B룸",
      "placeId": 1,
      "timeSlot": "HALFHOUR",
      "imageUrls": ["https://example.com/room2.jpg"],
      "keywordIds": [1, 4]
    }
  ],
  "request": {
    "path": "/bff/v1/rooms/place/1"
  }
}
```

---

### 4. 여러 룸 일괄 조회 (가격 정책 포함)

**엔드포인트**: `GET /bff/v1/rooms/batch`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| ids | List<Long> | O | 룸 ID 배열 (쉼표로 구분) |

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/rooms/batch?ids=101,102,103"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": [
    {
      "room": {
        "roomId": 101,
        "roomName": "A룸",
        "placeId": 1,
        "status": "OPEN",
        "timeSlot": "HOUR",
        "furtherDetails": ["방음 시설 완비"],
        "cautionDetails": ["흡연 금지"],
        "imageUrls": ["https://example.com/room101.jpg"],
        "keywordIds": [1, 2, 3]
      },
      "pricingPolicy": {
        "roomId": 101,
        "placeId": 1,
        "timeSlot": "1시간",
        "defaultPrice": 15000,
        "timeRangePrices": [...]
      }
    }
  ],
  "request": {
    "path": "/bff/v1/rooms/batch?ids=101,102,103"
  }
}
```

---

## 룸 예약 API

### 1. 룸 운영 정책 설정

**엔드포인트**: `POST /bff/v1/room-reservations/setup`

**인증**: 필수 (장소 소유자만)

**Request Body**:

```json
{
  "roomId": 101,
  "operatingHours": {
    "monday": { "open": "09:00", "close": "22:00" },
    "tuesday": { "open": "09:00", "close": "22:00" },
    "wednesday": { "open": "09:00", "close": "22:00" },
    "thursday": { "open": "09:00", "close": "22:00" },
    "friday": { "open": "09:00", "close": "23:00" },
    "saturday": { "open": "10:00", "close": "23:00" },
    "sunday": { "open": "10:00", "close": "22:00" }
  },
  "slotDurationMinutes": 60,
  "advanceBookingDays": 30
}
```

**curl 예시**:

```bash
curl -X POST "http://localhost:8080/bff/v1/room-reservations/setup" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 101,
    "operatingHours": {...},
    "slotDurationMinutes": 60,
    "advanceBookingDays": 30
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 202,
  "data": {
    "requestId": "setup-req-123",
    "status": "PROCESSING"
  },
  "request": {
    "path": "/bff/v1/room-reservations/setup"
  }
}
```

---

### 2. 슬롯 생성 상태 조회

**엔드포인트**: `GET /bff/v1/room-reservations/setup/{requestId}/status`

**인증**: 필수

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/room-reservations/setup/setup-req-123/status" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "requestId": "setup-req-123",
    "status": "COMPLETED",
    "progress": 100,
    "message": "슬롯 생성이 완료되었습니다."
  },
  "request": {
    "path": "/bff/v1/room-reservations/setup/setup-req-123/status"
  }
}
```

---

### 3. 예약 가능 슬롯 조회

**엔드포인트**: `GET /bff/v1/room-reservations/available-slots`

**인증**: 불필요

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roomId | Long | O | 룸 ID |
| date | String | O | 날짜 (yyyy-MM-dd) |

**curl 예시**:

```bash
curl -X GET "http://localhost:8080/bff/v1/room-reservations/available-slots?roomId=101&date=2025-01-16"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "roomId": 101,
    "date": "2025-01-16",
    "slots": [
      {
        "slotId": "slot-1",
        "startTime": "09:00",
        "endTime": "10:00",
        "status": "AVAILABLE",
        "price": 15000
      },
      {
        "slotId": "slot-2",
        "startTime": "10:00",
        "endTime": "11:00",
        "status": "BOOKED",
        "price": 15000
      },
      {
        "slotId": "slot-3",
        "startTime": "11:00",
        "endTime": "12:00",
        "status": "AVAILABLE",
        "price": 15000
      }
    ]
  },
  "request": {
    "path": "/bff/v1/room-reservations/available-slots?roomId=101&date=2025-01-16"
  }
}
```

**슬롯 상태 종류**:
| 상태 | 설명 |
|------|------|
| AVAILABLE | 예약 가능 |
| BOOKED | 예약됨 |
| PENDING | 예약 대기 중 |
| CLOSED | 휴무 |

---

### 4. 다중 슬롯 예약

**엔드포인트**: `POST /bff/v1/room-reservations/multi`

**인증**: 필수

**Request Body**:

```json
{
  "roomId": 101,
  "slotDate": "2025-01-16",
  "slotTimes": ["09:00", "10:00", "11:00"],
  "userId": "user_123"
}
```

**curl 예시**:

```bash
curl -X POST "http://localhost:8080/bff/v1/room-reservations/multi" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 101,
    "slotDate": "2025-01-16",
    "slotTimes": ["09:00", "10:00", "11:00"],
    "userId": "user_123"
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "reservationId": "reservation-123",
    "roomId": 101,
    "slotDate": "2025-01-16",
    "slotTimes": ["09:00", "10:00", "11:00"],
    "status": "PENDING",
    "createdAt": "2025-01-10T10:00:00"
  },
  "request": {
    "path": "/bff/v1/room-reservations/multi"
  }
}
```

---

## 예약 가격 API

### 1. 예약 가격 미리보기

**엔드포인트**: `POST /bff/v1/reservations/preview`

**인증**: 불필요

**Request Body**:

```json
{
  "roomId": 101,
  "timeSlots": [
    "2025-01-16T14:00:00",
    "2025-01-16T15:00:00",
    "2025-01-16T16:00:00"
  ],
  "products": [
    {
      "productId": 1,
      "quantity": 1
    },
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

**curl 예시**:

```bash
curl -X POST "http://localhost:8080/bff/v1/reservations/preview" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 101,
    "timeSlots": ["2025-01-16T14:00:00", "2025-01-16T15:00:00"],
    "products": [{"productId": 1, "quantity": 1}]
  }'
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "roomId": 101,
    "placeId": 1,
    "basePrice": 45000,
    "productPrice": 30000,
    "totalPrice": 75000,
    "timeSlots": [
      "2025-01-16T14:00:00",
      "2025-01-16T15:00:00",
      "2025-01-16T16:00:00"
    ],
    "products": [
      {
        "productId": 1,
        "productName": "빔 프로젝터",
        "quantity": 1,
        "unitPrice": 10000,
        "totalPrice": 10000
      },
      {
        "productId": 2,
        "productName": "화이트보드",
        "quantity": 2,
        "unitPrice": 5000,
        "totalPrice": 10000
      }
    ]
  },
  "request": {
    "path": "/bff/v1/reservations/preview"
  }
}
```

---

### 2. 예약 확정

**엔드포인트**: `PUT /bff/v1/reservations/{reservationId}/confirm`

**인증**: 필수

**curl 예시**:

```bash
curl -X PUT "http://localhost:8080/bff/v1/reservations/1/confirm" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "reservationId": 1,
    "roomId": 101,
    "placeId": 1,
    "basePrice": 45000,
    "productPrice": 30000,
    "totalPrice": 75000,
    "status": "CONFIRMED",
    "confirmedAt": "2025-01-16T10:30:00"
  },
  "request": {
    "path": "/bff/v1/reservations/1/confirm"
  }
}
```

---

### 3. 예약 취소

**엔드포인트**: `PUT /bff/v1/reservations/{reservationId}/cancel`

**인증**: 필수

**curl 예시**:

```bash
curl -X PUT "http://localhost:8080/bff/v1/reservations/1/cancel" \
  -H "Authorization: Bearer {token}"
```

**Response**:

```json
{
  "isSuccess": true,
  "code": 200,
  "data": {
    "reservationId": 1,
    "roomId": 101,
    "placeId": 1,
    "originalPrice": 75000,
    "refundAmount": 60000,
    "cancellationFee": 15000,
    "status": "CANCELLED",
    "cancelledAt": "2025-01-16T11:00:00"
  },
  "request": {
    "path": "/bff/v1/reservations/1/cancel"
  }
}
```

---

## 에러 처리

### 일반적인 에러 응답 형식

```json
{
  "isSuccess": false,
  "code": 400,
  "data": "에러 메시지",
  "request": {
    "path": "/bff/v1/..."
  }
}
```

### 주요 에러 코드 및 처리 방법

#### 400 Bad Request

```json
{
  "isSuccess": false,
  "code": 400,
  "data": "targetUserId is required"
}
```

**원인**: 필수 파라미터 누락, 유효성 검증 실패
**해결**: 요청 파라미터/바디 확인

#### 401 Unauthorized

```json
{
  "isSuccess": false,
  "code": 401,
  "data": "토큰이 만료되었습니다"
}
```

**원인**: 토큰 없음, 만료, 유효하지 않음
**해결**: 토큰 갱신 후 재요청

#### 403 Forbidden

```json
{
  "isSuccess": false,
  "code": 403,
  "data": "본인만 수정할 수 있습니다"
}
```

**원인**: 권한 없음 (다른 사용자의 리소스 수정 시도)
**해결**: 본인의 리소스인지 확인

#### 404 Not Found

```json
{
  "isSuccess": false,
  "code": 404,
  "data": "게시글을 찾을 수 없습니다"
}
```

**원인**: 존재하지 않는 리소스
**해결**: ID 확인

#### 409 Conflict

```json
{
  "isSuccess": false,
  "code": 409,
  "data": "슬롯이 이미 예약되었습니다"
}
```

**원인**: 리소스 충돌 (이미 예약된 슬롯 등)
**해결**: 최신 상태 확인 후 재시도

---

## 부록: 자주 사용하는 패턴

### 1. 커서 페이징 처리

```javascript
let cursor = null;
let hasNext = true;
const allArticles = [];

while (hasNext) {
  const url = cursor
    ? `http://localhost:8080/bff/v1/communities/articles/regular?cursorId=${cursor}&size=10`
    : `http://localhost:8080/bff/v1/communities/articles/regular?size=10`;

  const response = await fetch(url);
  const data = await response.json();

  allArticles.push(...data.data.page.items);
  cursor = data.data.page.nextCursorId;
  hasNext = data.data.page.hasNext;
}
```

### 2. 토큰 포함 요청

```javascript
const token = localStorage.getItem('access_token');

const response = await fetch('http://localhost:8080/bff/v1/profiles/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
```

### 3. 에러 처리

```javascript
try {
  const response = await fetch(url, options);
  const data = await response.json();

  if (!data.isSuccess) {
    // 에러 응답
    switch (data.code) {
      case 401:
        // 토큰 갱신
        break;
      case 403:
        // 권한 없음 알림
        break;
      case 404:
        // 리소스 없음
        break;
      default:
        // 일반 에러
    }
  } else {
    // 성공 처리
    console.log(data.data);
  }
} catch (error) {
  // 네트워크 에러
  console.error('네트워크 오류:', error);
}
```

---

## 문서 버전

- **버전**: 1.0
- **최종 수정일**: 2025-01-25
- **작성자**: DDING
- **문의**: ddingsha9@teabind.co.kr
