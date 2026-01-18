# Bander 프로젝트 ERD 문서

## 문서 구조

### 마크다운 문서 (상세 설명)

| 파일명                         | 도메인      | 설명                   |
|-----------------------------|----------|----------------------|
| `01_auth.md`                | 인증/사용자   | 로그인, 회원가입, 사용자 상태 관리 |
| `02_profile.md`             | 프로필      | 사용자 프로필, 장르, 악기 정보   |
| `03_place_room.md`          | 업체/객실    | 장소 정보, 객실 정보, 운영 정책  |
| `04_reservation_payment.md` | 예약/결제    | 예약 프로세스, 결제, 쿠폰      |
| `05_board_community.md`     | 게시판/커뮤니티 | 게시글, 댓글, 좋아요, 활동 기록  |
| `06_chat_notification.md`   | 채팅/알림    | 실시간 채팅, 푸시 알림        |
| `07_review_support.md`      | 리뷰/지원    | 리뷰 작성, 별점 통계         |

### DBML 파일 (dbdiagram.io용)

| 파일명                           | 도메인      | 용도      |
|-------------------------------|----------|---------|
| `01_auth.dbml`                | 인증/사용자   | ERD 시각화 |
| `02_profile.dbml`             | 프로필      | ERD 시각화 |
| `03_place_room.dbml`          | 업체/객실    | ERD 시각화 |
| `04_reservation_payment.dbml` | 예약/결제    | ERD 시각화 |
| `05_board_community.dbml`     | 게시판/커뮤니티 | ERD 시각화 |
| `06_chat_notification.dbml`   | 채팅/알림    | ERD 시각화 |
| `07_review_support.dbml`      | 리뷰/지원    | ERD 시각화 |

> DBML 파일은 [dbdiagram.io](https://dbdiagram.io)에 붙여넣어 ERD를 시각화할 수 있습니다.

---

## 핵심 개념

### 1. MSA (Microservice Architecture)

```
이 프로젝트는 여러 개의 독립된 서비스로 구성되어 있습니다.
각 서비스는 자체 데이터베이스를 가지고 있어요.

예:
- Auth 서비스 → auth_db
- Place 서비스 → place_db
- Reservation 서비스 → reservation_db
```

### 2. ID 체계

```
대부분의 ID는 Snowflake ID를 사용합니다.
- 문자열(String)로 전달됨
- 예: "1234567890123456789"
- 시간순 정렬 가능
```

### 3. 서비스 간 참조

```
MSA에서는 서비스 간 직접 조인이 불가능합니다.
대신 ID를 저장하고, API Gateway에서 조합해서 응답합니다.

예: reservation 테이블에 user_id, place_id, room_id가 있지만
    실제 데이터는 각각 Auth, Place, Room 서비스에서 가져옵니다.
```

---

## 공통 필드 패턴

### 시간 관련

| 필드명          | 타입        | 설명                                       |
|--------------|-----------|------------------------------------------|
| `created_at` | DateTime  | 생성 시각 (ISO 8601: `2026-01-12T10:30:00Z`) |
| `updated_at` | DateTime  | 수정 시각                                    |
| `deleted_at` | DateTime? | 삭제 시각 (Soft Delete, null이면 미삭제)          |

### 상태 관련

| 필드명          | 타입           | 예시 값                            |
|--------------|--------------|---------------------------------|
| `status`     | String(Enum) | `ACTIVE`, `INACTIVE`, `DELETED` |
| `is_active`  | Boolean      | `true`, `false`                 |
| `is_deleted` | Boolean      | `true`, `false`                 |

### 페이지네이션 (API 응답)

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## API Response 패턴

### 단일 조회

```json
{
  "success": true,
  "data": {
    "id": "1234567890",
    "name": "..."
  },
  "message": null
}
```

### 목록 조회

```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100
  },
  "message": null
}
```

### 에러 응답

```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "errorCode": "AUTH_001"
}
```

---

## 서비스 흐름도

---

## 자주 사용하는 Enum 값들

### UserRole (사용자 역할)

| 값             | 설명     |
|---------------|--------|
| `USER`        | 일반 사용자 |
| `PLACE_OWNER` | 업체 소유자 |
| `ADMIN`       | 관리자    |

### Provider (로그인 제공자)

| 값        | 설명          |
|----------|-------------|
| `LOCAL`  | 이메일/비밀번호 가입 |
| `KAKAO`  | 카카오 소셜 로그인  |
| `GOOGLE` | 구글 소셜 로그인   |
| `APPLE`  | 애플 소셜 로그인   |

### ReservationStatus (예약 상태)

| 값                    | 설명            | 다음 가능 상태                      |
|----------------------|---------------|-------------------------------|
| `AWAITING_USER_INFO` | 사용자 정보 입력 대기  | PENDING, REJECTED             |
| `PENDING`            | 예약 대기 (결제 대기) | PENDING_CONFIRMED, REJECTED   |
| `PENDING_CONFIRMED`  | 영업장 승인 대기     | CONFIRMED, REJECTED, REFUNDED |
| `CONFIRMED`          | 예약 확정         | REJECTED, REFUNDED            |
| `REJECTED`           | 예약 거절         | REFUNDED                      |
| `REFUNDED`           | 환불 완료         | (종료 상태)                       |

### PaymentStatus (결제 상태)

| 값           | 설명    |
|-------------|-------|
| `PREPARED`  | 결제 대기 |
| `COMPLETED` | 결제 완료 |
| `FAILED`    | 결제 실패 |
| `CANCELLED` | 결제 취소 |

### SlotStatus (타임슬롯 상태)

| 값           | 설명             |
|-------------|----------------|
| `AVAILABLE` | 예약 가능          |
| `PENDING`   | 예약 진행중 (결제 대기) |
| `RESERVED`  | 예약 확정 (결제 완료)  |
| `CLOSED`    | 운영하지 않음 (휴무일)  |

### ChatRoomType (채팅방 유형)

| 값               | 설명        |
|-----------------|-----------|
| `DM`            | 1:1 개인 대화 |
| `GROUP`         | 그룹 채팅     |
| `PLACE_INQUIRY` | 공간 문의     |
| `SUPPORT`       | 고객 상담     |

---
